package com.genie.shop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import com.genie.shop.vo.AppInfoVO;
import com.genie.shop.vo.ChannelVO;
import com.genie.shop.vo.MediaInfoVO;
import com.genie.shop.vo.SongVO;
import com.genie.shop.vo.UserVO;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;

@EnableAsync
@Service
public class ShopNGenieService {
	
	static Logger logger = LoggerFactory.getLogger(ShopNGenieService.class);
	
	@Autowired
	public ShopAudioPlayerListener apl;
	
	@Autowired
	private ShopHttpClient shopHttpClient;
	
	@Autowired
	private ShopDownloadManager shopDownloadManager;
	
	private String shopId="";
	private String shopPasswd = "";
	

	public boolean isPlayEnd = false;
	
	UserVO userAccountInfo = null;
	
	//
	ChannelVO lastChannelInfo = null;

	AppInfoVO appInfo = null;
	
	@Value("#{config['basic.id']}")
	private String basicId ="";
	
	@Value("#{config['basic.passwd']}")
	private String basicPasswd = "";
	
	@Value("#{config['api.host']}")
	public String apiHost = "";
	
	@Value("#{config['cdn.host']}")
	public String cdnHost = "";

	
	@Value("#{config['api.url']}")
	public String apiUrl = "";
	
	@Value("#{config['cdn.url']}")
	public String cdnUrl = "";
	
	@Value("#{config['login.url']}")
	public String loginUrl = "";
	
	@Value("#{config['userinfo.url']}")
	public String userInfoUrl = "";
	
	@Value("#{config['lastschedules.url']}")
	public String lastSchedulesUrl = "";
	
	@Value("#{config['appinfo.url']}")
	public String appInfoUrl = "";
	
	
	@Value("#{config['schedules.url']}")
	public String schedulesUrl = "";
	
	@Value("#{config['currentsong.url']}")
	public String currentSongUrl = "";
	
	@Value("#{config['download.url']}")
	public String downloadUrl = "";
	
	
	@Value("#{config['local.download.path']}")
	public String localDownloadPath = "";
	
	@Value("#{config['max.download.count']}")
	public Integer maxDownloadCount = 0;
	
	@Value("#{config['emergency.download.path']}")
	public String emergencyDownloadPath = "";
	
	
	@Value("#{config['emergency.download.count']}")
	public Integer emergencyDownloadCount = 0;
	
	
	@Value("#{config['aod.file.type']}")
	public String aodFileType = "";
	
	@Value("#{config['json.file.type']}")
	public String jsonFileType = "";
	
	
	@Value("#{config['local.log.path']}")
	public String localLogPath = "";
	
	
	@Value("#{config['remove.song.gap']}")
	public String removeSongGap = "";
	
	/**
	 * 
	 */
	public ShopNGenieAudioPlayer player = null;
	
	
	public static void main(String[] args) throws Exception {
		
		if (args.length <2){
			System.out.println("required shopId , shopPassword!!");
			System.exit(0);
		}
		
		ShopNGenieService c = new ShopNGenieService();
		
		try{
			c.shopId = args[0];
			c.shopPasswd = args[1];
			
			c.run();
			logger.info("is service expired");
		}catch(Exception e){
			logger.warn("main Exception",e);
		}
	}
	
	
	
	
	/**
	 * 
	 * 1.network check ( establish network )
	 * 	- api domain check 
	 *  - cdn domain check
	 * 2. disk check
	 *  - first cache clean (disk delete)
	 * 3. notice service ( may be )
	 *  - SMS, LMS, TTS 
	 */
	public void checkConfiguration(){
		

		boolean apiReachable = false;
		boolean cdnReachable = false;
		
		try{
			apiReachable = InetAddress.getByName(apiHost).isReachable(2*1000);
			cdnReachable = InetAddress.getByName(cdnHost).isReachable(2*1000);
			
			
			if (apiReachable &&  cdnReachable){
				logger.info("\napi is reacheable!\tcdn is reacheable!");
			}else{
				logger.warn("##############network is checking please...!!!!##############");
			}
			
		}catch(Exception e){
			logger.warn("",e);
		}
		
	}
	
	/**
	 * 비상용 음원 로드 및 과적된 음원은 삭제
	 * @throws Exception
	 */
	public void loadForEmergency() throws Exception {
		/**
		 * 임시 파일을 로드 로직
		 */
		File eCleanFile = new File(emergencyDownloadPath);
		File[] eCleanfileList =  eCleanFile.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name){
			return name.endsWith("." + aodFileType); 
			}
		});
		
		
		//비상용 음원이 많을경우 삭제 로직
		if ( emergencyDownloadCount < eCleanfileList.length ){
			int gap = eCleanfileList.length - emergencyDownloadCount;
			
			for(int i=0,j = gap; i<j ; i++){
				
				String filePath = eCleanfileList[i].getPath();
				String jsonFilePath = StringUtils.replace(filePath, aodFileType, jsonFileType);
				
				File jsonFile = new File(jsonFilePath);				
				jsonFile.delete();
				eCleanfileList[i].delete();
				
			}
		}
		
		
		//비상용 믕원에 대한 loading
		if (eCleanfileList != null){
			for(File cacheFile : eCleanfileList ){
				
				String filePath = cacheFile.getPath();
				String[] songSplit = StringUtils.split(filePath, File.separator);
				String[] songUid = StringUtils.split(songSplit[songSplit.length -1],".");
				
				MediaInfoVO media = null;
				try{
					media = shopDownloadManager.jsonToMedia(Long.parseLong(songUid[0]));
					media.setFile(cacheFile);
					shopDownloadManager.emeAodPool.add(media);
				}catch(Exception e){
					logger.warn("",e);
				}
			}
		}
		
		logger.info("####load after emergency mediainfo total (" + shopDownloadManager.emeAodPool.size() +")####");
		
	}
	
	/**
	 * configuration load
	 * create dir
	 * delete cache mp3 files
	 * disk check for free disk and delete log files
	 */
	public void loadConfig() throws Exception {
		
		//load configurations
		File file = new File(localDownloadPath);
		if (!file.exists()){
			file.mkdirs();
		}
		
		File[] fileList =  file.listFiles();
		if (fileList != null){
			for(File cacheFile : fileList ){
				cacheFile.delete();
			}
		}
		
		//load configuration emergency dir
		File eFile = new File(emergencyDownloadPath);
		if (!eFile.exists()){
			eFile.mkdirs();
		}
		
		loadForEmergency();
		
		//delete log files
		if(isDiskClean()){
			File logFile = new File(localLogPath);
			File[] logFileList =  logFile.listFiles();
			if (logFileList != null){
				for(File logFileD : logFileList ){
					logFileD.delete();
				}
			}
		}		
	}
	
	/**
	 * disk check for 10% under free disk space
	 * and clean true, false
	 * @return
	 */
	public boolean isDiskClean(){
		
		boolean ret = false;
		String os = System.getProperty("os.name");
		String userDir = System.getProperty("user.dir"); 
		String diskRoot = "";
		File diskFile = null; 
		
		if(os.toLowerCase().contains("linux") ){
			diskFile = new File("/");
		}else{
			diskFile = new File(userDir.substring(0, 3));
		}
		
		float free = diskFile.getFreeSpace();
		float total = diskFile.getTotalSpace();
		
		float percentage = (free/total)*100;
		
		if ( percentage < 15) {
			ret= true;
		}
		
		return ret;
	}
	
	
	
	
	public void run() {
		
		Long seq = -1L;		
		boolean isFirst = true;		
		SongVO songInfo = null;
		ArrayList<SongVO> daySongInfoList = null;
		ChannelVO songChannel = null;
		ChannelVO lastChannelInfo = null;

		//네트워크 offline 및 장애처리 로직 없음
		ArrayList<SongVO> songInfoList = null;
		ArrayList<ChannelVO> arrChannelList = null;
		int eIdx =0;
		//구동 시간 정리
		String startingYmd   = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
		String runningYmd   = "";
		
		try{
			
			loadConfig();
			checkConfiguration();
			
			shopHttpClient.setShopId(shopId);
			shopHttpClient.setShopPasswd(shopPasswd);
			
			//1. login process
			userAccountInfo = shopHttpClient.loginUser();			
			//2. last channel info 
			lastChannelInfo = shopHttpClient.getChannelInfo(shopId);			
			//3. app login update information 
			appInfo = shopHttpClient.getAppInfo(userAccountInfo);
		}catch(Exception e){			
			logger.error("Exception is:",e);			
		}
		
		
		while(true){
			
			try{
				
				runningYmd   = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
				
				arrChannelList = shopHttpClient.getChannelList(userAccountInfo,lastChannelInfo);
				
				if(arrChannelList != null && arrChannelList.size() > 0){
				
					songChannel = arrChannelList.get(0);
					songInfoList = shopHttpClient.getCurrentSong(userAccountInfo, songChannel);
				
					//증복 로그인 시 size = 0  리턴
					if (  songInfoList != null && songInfoList.size() != 0 ){
						songInfo = songInfoList.get(0);
						
						if( "true".equals(removeSongGap)){
							shopDownloadManager.removeQueueGap(songInfo.getSeq());
						}
						
						shopDownloadManager.addQueueMedia(userAccountInfo, songChannel,songInfo.getSeq());
						//shopDownloadManager.addQueueMedia(userAccountInfo, songChannel,songInfo);
						
						playMusicFromBasicPlayer(songInfo);
						
						seq = songInfo.getSeq();
						
						Thread.sleep(3 * 1000);
					}else{//중복 로그인 과 PM 진행 2가지 케이스 발생
						
						logger.info("force duplicate login start~!!");
						
						//1. app 로그인
						userAccountInfo.setSessionKey(shopHttpClient.forceLogin());				
						Thread.sleep(1*1000);
						
						logger.info("force duplicate login end~!!");
						
						continue;
					}
					
				}
			}catch(Exception e){				
				logger.error("Exception is:",e);
				
				try{
					playEmergencyMusic(eIdx);
					
					if(shopDownloadManager.emeAodPool.size() > eIdx-1){
						eIdx++;
					}else{
						eIdx = 0;
					}
					
					logger.info("[Exception]force duplicate login start~!!");
					
					//강제 로그인 후 sessionkey 업데이트
					userAccountInfo.setSessionKey(shopHttpClient.forceLogin());					
					Thread.sleep(1*1000);				
					logger.info("[Exception]force duplicate login end~!!");
					
					continue;
				}catch(Exception ee){
					logger.error("",ee);
					try{Thread.sleep(1*1000);}catch(Exception eee){}	
				}finally{
					logger.info("exception block finally execute");
				}
			}
		}
	}
		
	
	public synchronized void playEmergencyMusic(int eIdx) throws Exception {
		
		logger.info("emergency AOD Play logic start~!!!");
		BasicPlayer player = null;
		BasicController control = null;
	
		//Player player = null;
		MediaInfoVO media = null;
		
		player = new BasicPlayer();
		control = (BasicController)player;
		
		media = shopDownloadManager.emeAodPool.get(eIdx);
		
		control.open(media.getFile());
		apl.setMedia(media);
	
		player.addBasicPlayerListener(apl);
	
		control.play();
		
		control.setGain(1.0);
		control.setPan(0.0);
		
		//Thread wait
		//EventListener에서 음악이 종료 시, thread notify를 한다.
		wait();
		
		
		logger.info("emergency AOD Play logic end~!!!");
	}
	
	
	/**
	 * song play
	 * 
	 * @param songInfo
	 */
	public void playMusic(SongVO songInfo){
		
		HttpClient client = new DefaultHttpClient();
		
		Long starttime = 0L;
		Long startByte = 0L;
		
		
		
		HttpGet httpget = new HttpGet(songInfo.getStreamUrl());
		httpget.removeHeaders("Authorization");
		httpget.removeHeaders("X-AuthorityKey");
		
		if ( songInfo.getPlayStartRunTime() != 0){
			
			if (songInfo.getPlayStartRunTime() < 20){
				startByte = 0L;
			}else{
				starttime = songInfo.getPlayStartRunTime();
				startByte = (starttime*128*1024)/8;
			}
		
			httpget.addHeader("Range", "bytes=" + startByte + "-");		
		}

		HttpResponse response =null;
		ShopNGenieAudioPlayer player = null;
	
		//Player player = null;
		MediaInfoVO media = null;
		try{
			
			
			logger.info("download queue size=" + shopDownloadManager.queue.size());

			//queue 사이즈가 0보다 크면, 해당 queue의 제일 첫 음원을 재생한다.
			if ( shopDownloadManager.queue.size() > 0 ){
				
				media = shopDownloadManager.poll();
				logger.info("is queue poll seq(" + media.getSeq()+")");
				InputStream stream = new FileInputStream(media.getFile());
				
				//player = new BasicPlayer(stream);
				player = new ShopNGenieAudioPlayer(stream);
				
			}else{
				response = client.execute(httpget);		
				int statusCode = response.getStatusLine().getStatusCode();			
				
				HttpEntity httpEntity = response.getEntity();			
		        
				logger.info("is http progressive streaming!!");
				//player = new ShopNGenieAudioPlayer(httpEntity.getContent());
				player = new ShopNGenieAudioPlayer(httpEntity.getContent());
				
			}
			
			player.play();
			
			if ( media != null){
				media.file.delete();
			}
			
						
       }catch (JavaLayerException je) {
    	   //Cannot create AudioDevice 
    	   //logger.warn(je.toString());
    	   logger.info(je.toString(), je);
    	   //logger.warn(je);
       }
		catch (Exception e) {
			logger.info(e.toString(), e);
			//logger.warn(e.toString());
       }
	          
	}
	

	/**
	 * basicPlayer의 경우 Thread base로 thread notify와 sleep등의 thread handling이 필요
	 * basicPlayer의 경우 status에 따른 상세 조절 가능
	 * @param songInfo
	 */
	public void playNewMusic(SongVO songInfo){
	
	HttpClient client = new DefaultHttpClient();
	
	Long starttime = 0L;
	Long startByte = 0L;
	
	
	
	HttpGet httpget = new HttpGet(songInfo.getStreamUrl());
	httpget.removeHeaders("Authorization");
	httpget.removeHeaders("X-AuthorityKey");
	
	if ( songInfo.getPlayStartRunTime() != 0){
		
		if (songInfo.getPlayStartRunTime() < 20){
			startByte = 0L;
		}else{
			starttime = songInfo.getPlayStartRunTime();
			startByte = (starttime*128*1024)/8;
		}
	
		httpget.addHeader("Range", "bytes=" + startByte + "-");		
	}

	HttpResponse response =null;
	BasicPlayer player = null;
	
	BasicController control = null; 
	
	
	//Player player = null;
	MediaInfoVO media = null;
	try{
		
		
	logger.info("download queue size=" + shopDownloadManager.queue.size());
		//queue 사이즈가 0보다 크면...
		if ( shopDownloadManager.queue.size() > 0 ){
			
			media = shopDownloadManager.poll();
			logger.info("is queue poll seq(" + media.getSeq()+")");
			InputStream stream = new FileInputStream(media.getFile());
			
			//player = new BasicPlayer(stream);
			player = new BasicPlayer();
			control = (BasicController) player;
			control.open(stream);
		}else{
			response = client.execute(httpget);		
			int statusCode = response.getStatusLine().getStatusCode();			
			
			HttpEntity httpEntity = response.getEntity();			
	        
			
			logger.info("is http progressive streaming!!");
			//player = new ShopNGenieAudioPlayer(httpEntity.getContent());
			player = new BasicPlayer();
			control = (BasicController) player;
			control.open(httpEntity.getContent());
		}
			
		//player = new BasicPlayer();
		//player.open(httpEntity.getContent());
		//logger.info("\nplayer position:"+ String.format("%,d", Integer.parseInt(""+startByte)) + " bytes");
		//player.seek(startByte);
		
		ShopAudioPlayerListener apl = new ShopAudioPlayerListener();
		apl.setController(control);
		
		player.addBasicPlayerListener(apl);
		
		logger.info("is playing!!");
		
		control.play();
		
		control.setGain(0.85);
		
		// Set Pan (-1.0 to 1.0).
		// setPan should be called after control.play().
		control.setPan(0.0);
		
		if ( media != null){
			media.file.delete();
		}
		
		while(true){
			
		}
		
   }/*catch (JavaLayerException je) {
	   //Cannot create AudioDevice 
	   //logger.warn(je.toString());
	   logger.info(je.toString(), je);
	   //logger.warn(je);
   }*/
	catch (Exception e) {
		logger.info(e.toString(), e);
		//logger.warn(e.toString());
   }
          
	}
	
	/**
	 * song play
	 * 
	 * @param songVO
	 */
	public synchronized  void  playMusicFromBasicPlayer(SongVO songVO){
		
		

		HttpResponse response =null;
		BasicPlayer player = null;
		BasicController control = null;
	
		//Player player = null;
		MediaInfoVO media = null;
		try{
			
			
			logger.info("download queue size=" + shopDownloadManager.queue.size());
			
			//queue 사이즈가 0보다 크면, 해당 queue의 제일 첫 음원을 재생한다.
			if ( shopDownloadManager.queue.size() > 0 ){
				
				media = shopDownloadManager.poll();
				logger.info("is queue poll seq(" + media.getSeq()+")");
				//InputStream stream = new FileInputStream(media.getFile());
				logger.info("seq gap is " + songVO.getSeq() + " - " + songVO.getSeq() + " = " +  (songVO.getSeq() - songVO.getSeq()) );
				logger.info("seq gap time is " + shopDownloadManager.getQueueRunningTimeGap(songVO.getSeq()) );
				
				player = new BasicPlayer();
				control = (BasicController)player;
				control.open(media.getFile());
				apl.setMedia(media);
				apl.setFirst();

			}else{
				response = shopHttpClient.getCDNMedia(songVO);
				int statusCode = response.getStatusLine().getStatusCode();			
				
				HttpEntity httpEntity = response.getEntity();			
		        
				logger.info("is http progressive streaming!!");
				
				player = new BasicPlayer();
				control = (BasicController)player;
				control.open(httpEntity.getContent());				
			}
			
			player.addBasicPlayerListener(apl);
			
			control.play();
			
			control.setGain(1.0);
			control.setPan(0.0);
			
			//Thread wait
			//EventListener에서 음악이 종료 시, thread notify를 한다.
			wait();
			
			

       }catch (Exception e) {
			logger.info(e.toString(), e);
       }finally{
    	   if ( media != null){
				File file = media.getFile();	
				String filePath = file.getPath();
				shopDownloadManager.addEQueue(media);
				file.delete();
    	   }    	   
       }
	          
	}
	
	
	
	
	/**
	 * ��Ʈ���� ���� üũ
	 */
	public void checkStreaming(){}
	
	/**
	 * ��Ʈ���� ���
	 * 1. ��Ʈ��������, �ٿ�ε�&�÷��� ���� üũ
	 * 2. �÷��� �� ��Ʈ��ũ ��Ȳ�� ���
	 * 3. ��� �߻�
	 *   - ���� ��� ������ ���� ����ϴ�. ���� �߻�
	 *   - {"data":{"sessionKey":"SG15214835036978780258","rtCode":"SG4037","rtMsg":"���� �ð��� ������ ���� ����ϴ�.","params":{}}}  
	 * 4. 
	 */
	public void play(){}
	
	/**
	 * ��Ʈ��ũ ���� �� ���� ���� ���
	 */
	public void networkFailPlay(){}




	public String getShopId() {
		return shopId;
	}




	public void setShopId(String shopId) {
		this.shopId = shopId;
	}




	public String getShopPasswd() {
		return shopPasswd;
	}




	public void setShopPasswd(String shopPasswd) {
		this.shopPasswd = shopPasswd;
	}




	public ShopAudioPlayerListener getApl() {
		return apl;
	}




	public void setApl(ShopAudioPlayerListener apl) {
		this.apl = apl;
	}




	public ShopHttpClient getShopHttpClient() {
		return shopHttpClient;
	}




	public void setShopHttpClient(ShopHttpClient shopHttpClient) {
		this.shopHttpClient = shopHttpClient;
	}




	public ShopDownloadManager getShopDownloadManager() {
		return shopDownloadManager;
	}




	public void setShopDownloadManager(ShopDownloadManager shopDownloadManager) {
		this.shopDownloadManager = shopDownloadManager;
	}




	public UserVO getUserAccountInfo() {
		return userAccountInfo;
	}




	public void setUserAccountInfo(UserVO userAccountInfo) {
		this.userAccountInfo = userAccountInfo;
	}




	public ChannelVO getLastChannelInfo() {
		return lastChannelInfo;
	}




	public void setLastChannelInfo(ChannelVO lastChannelInfo) {
		this.lastChannelInfo = lastChannelInfo;
	}




	public AppInfoVO getAppInfo() {
		return appInfo;
	}




	public void setAppInfo(AppInfoVO appInfo) {
		this.appInfo = appInfo;
	}




	public String getBasicId() {
		return basicId;
	}




	public void setBasicId(String basicId) {
		this.basicId = basicId;
	}




	public String getBasicPasswd() {
		return basicPasswd;
	}




	public void setBasicPasswd(String basicPasswd) {
		this.basicPasswd = basicPasswd;
	}




	public String getApiHost() {
		return apiHost;
	}




	public void setApiHost(String apiHost) {
		this.apiHost = apiHost;
	}




	public String getCdnHost() {
		return cdnHost;
	}




	public void setCdnHost(String cdnHost) {
		this.cdnHost = cdnHost;
	}




	public String getApiUrl() {
		return apiUrl;
	}




	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}




	public String getCdnUrl() {
		return cdnUrl;
	}




	public void setCdnUrl(String cdnUrl) {
		this.cdnUrl = cdnUrl;
	}




	public String getLoginUrl() {
		return loginUrl;
	}




	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}




	public String getUserInfoUrl() {
		return userInfoUrl;
	}




	public void setUserInfoUrl(String userInfoUrl) {
		this.userInfoUrl = userInfoUrl;
	}




	public String getLastSchedulesUrl() {
		return lastSchedulesUrl;
	}




	public void setLastSchedulesUrl(String lastSchedulesUrl) {
		this.lastSchedulesUrl = lastSchedulesUrl;
	}




	public String getAppInfoUrl() {
		return appInfoUrl;
	}




	public void setAppInfoUrl(String appInfoUrl) {
		this.appInfoUrl = appInfoUrl;
	}




	public String getSchedulesUrl() {
		return schedulesUrl;
	}




	public void setSchedulesUrl(String schedulesUrl) {
		this.schedulesUrl = schedulesUrl;
	}




	public String getCurrentSongUrl() {
		return currentSongUrl;
	}




	public void setCurrentSongUrl(String currentSongUrl) {
		this.currentSongUrl = currentSongUrl;
	}




	public String getDownloadUrl() {
		return downloadUrl;
	}




	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}




	public String getLocalDownloadPath() {
		return localDownloadPath;
	}




	public void setLocalDownloadPath(String localDownloadPath) {
		this.localDownloadPath = localDownloadPath;
	}




	public Integer getMaxDownloadCount() {
		return maxDownloadCount;
	}




	public void setMaxDownloadCount(Integer maxDownloadCount) {
		this.maxDownloadCount = maxDownloadCount;
	}




	public String getEmergencyDownloadPath() {
		return emergencyDownloadPath;
	}




	public void setEmergencyDownloadPath(String emergencyDownloadPath) {
		this.emergencyDownloadPath = emergencyDownloadPath;
	}




	public ShopNGenieAudioPlayer getPlayer() {
		return player;
	}




	public void setPlayer(ShopNGenieAudioPlayer player) {
		this.player = player;
	}

	
	
	
	
	
	
	
	
	
	

	
	/**
	 * 구동시작 일자와 구동일자를 체크 한다.
	 * 구동일자 시작 시 그날의 전체 스케쥴 표를 조회 한다.
	 * 스케쥴표에 시간에 맞는 음원만 놓고 스케쥴 표중 지난 시간의 재생 리스트는 제거 한다.
	 * 
	 * @throws Exception
	 */
	public void runNew() throws Exception {
		
		shopHttpClient.setShopId(shopId);
		shopHttpClient.setShopPasswd(shopPasswd);
		
		//구동 시간 정리
		String startingYmd   = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
		
		//1. app 로그인
		userAccountInfo = shopHttpClient.loginUser();
		
		//채널정보
		ChannelVO lastChannelInfo = shopHttpClient.getChannelInfo(shopId);
		
		//app 로그인 후 업데이트 정보 
		appInfo = shopHttpClient.getAppInfo(userAccountInfo);
		Long seq = -1L;
		
		boolean isFirst = true;
		
		SongVO songInfo = null;
		ArrayList<SongVO> daySongInfoList = null;
		ChannelVO songChannel = null;
		
		//네트워크 offline 및 장애처리 로직 없음		
		ArrayList<ChannelVO> arrChannelList = shopHttpClient.getChannelList(userAccountInfo,lastChannelInfo);
		ArrayList<SongVO> songInfoList = null;
		
		if(arrChannelList != null && arrChannelList.size() > 0){
		
			songChannel = arrChannelList.get(0);				
			//		
			songInfoList = shopHttpClient.getCurrentSong(userAccountInfo, songChannel);
		}
		
		
		while(true){
			
			String runningYmd   = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
			//구동일자와 동일한 날짜이면 스케쥴 정보를 조회하지 않는다.
			//만약 구동일자와 다른 날짜이면 스케쥴 정보를 조회 한다.			
			if (startingYmd.equals(runningYmd) ){//구동일자가 같으면
				
				if(arrChannelList != null && arrChannelList.size() > 0){
				
					songChannel = arrChannelList.get(0);
					songInfoList = shopHttpClient.getCurrentSong(userAccountInfo, songChannel);				
					
					if ( daySongInfoList == null){
						logger.info("is day schedules getting");
						
						if(isFirst == true){
							//하루의 스케쥴 전체 조회
							daySongInfoList = shopHttpClient.getDaySchedules(songChannel.getChannelUid(), songChannel.getScheduleUid(), songChannel.getChainUid(), userAccountInfo.getSessionKey());
							isFirst = false;
						}
						
						int j = daySongInfoList.size();
						String inTime   = new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date());
						
						//하루가 다 지나갔다는 의미이며, 23시50분 이상이라는 얘기
						if ( daySongInfoList.size() == 0){
							
						}else{
						
							for (int i=0; i <j ; ){
								SongVO songTmp = (SongVO)daySongInfoList.get(i);				
								Long startTime = Long.parseLong(songTmp.getStartTime());
								Long endTime = Long.parseLong(songTmp.getEndTime());
								
								Long currentTime = Long.parseLong(inTime);
								
								if ( currentTime > endTime ){							
									daySongInfoList.remove(songTmp);
									j--;							
								}else{
									i++;
								}
							}	
						}
					}
				}	
	
			}else{
				startingYmd = runningYmd;
				isFirst = true;
				arrChannelList = shopHttpClient.getChannelList(userAccountInfo,lastChannelInfo);
				if(arrChannelList != null && arrChannelList.size() > 0){
				
					songChannel = arrChannelList.get(0);				
					//		
					songInfoList = shopHttpClient.getCurrentSong(userAccountInfo, songChannel);
				}
			}
			
			//증복 로그인 시 size = 0  리턴
			if ( songInfoList.size() != 0 ){
				songInfo = songInfoList.get(0);
				
				shopDownloadManager.addMedia(userAccountInfo, songChannel,songInfo.getSeq(), daySongInfoList );
				
				//shopDownloadManager.addQueueMedia(userAccountInfo, songChannel,songInfo.getSeq());
				
				//shopDownloadManager.asynchDown(userAccountInfo, songChannel,songInfo.getSeq());
				
				
				//shopHttpClient.playTTS(player,songInfo.getArtistName(),songInfo.getSongTitle());
				
				playMusic(songInfo);
				
				//shopHttpClient.sendPlayLog(userAccountInfo,songInfo, formparams);
				
				//shopHttpClient.sendPlayLog(userInfoLogin,songInfo);
				
				seq = songInfo.getSeq();
			}else{
				Thread.sleep(10 * 1000);
			}
			
			Thread.sleep(1000);
				
			}
		}
	
	/**
	 * song play
	 * 
	 * @param songInfo
	 */
	public void playMusicNew(SongVO songInfo){
		
		HttpClient client = new DefaultHttpClient();
		
		Long starttime = 0L;
		Long startByte = 0L;
		
		
		
		HttpGet httpget = new HttpGet(songInfo.getStreamUrl());
		httpget.removeHeaders("Authorization");
		httpget.removeHeaders("X-AuthorityKey");
		
		if ( songInfo.getPlayStartRunTime() != 0){
			
			if (songInfo.getPlayStartRunTime() < 20){
				startByte = 0L;
			}else{
				starttime = songInfo.getPlayStartRunTime();
				startByte = (starttime*128*1024)/8;
			}
		
			httpget.addHeader("Range", "bytes=" + startByte + "-");		
		}

		HttpResponse response =null;
		ShopNGenieAudioPlayer player = null;
	
		//Player player = null;
		MediaInfoVO media = null;
		try{
			
			
		logger.info("download queue size=" + shopDownloadManager.queue.size());
			//queue 사이즈가 0보다 크면...
			if ( shopDownloadManager.queue.size() > 0 ){
				
				media = shopDownloadManager.poll();
				logger.info("is queue poll seq(" + media.getSeq()+")");
				InputStream stream = new FileInputStream(media.getFile());
				
				//player = new BasicPlayer(stream);
				player = new ShopNGenieAudioPlayer(stream);
				
			}else{
				response = client.execute(httpget);		
				int statusCode = response.getStatusLine().getStatusCode();			
				
				HttpEntity httpEntity = response.getEntity();			
		        
				logger.info("is http progressive streaming!!");
				//player = new ShopNGenieAudioPlayer(httpEntity.getContent());
				player = new ShopNGenieAudioPlayer(httpEntity.getContent());
				
			}
			
			player.play();
			
			if ( media != null){
				media.file.delete();
			}
			
						
       }catch (JavaLayerException je) {
    	   //Cannot create AudioDevice 
    	   //logger.warn(je.toString());
    	   logger.info(je.toString(), je);
    	   //logger.warn(je);
       }
		catch (Exception e) {
			logger.info(e.toString(), e);
			//logger.warn(e.toString());
       }
	          
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
