package com.genie.shop;

import java.io.File;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.genie.shop.vo.AppInfoVO;
import com.genie.shop.vo.ChannelVO;
import com.genie.shop.vo.MediaInfoVO;
import com.genie.shop.vo.SongVO;
import com.genie.shop.vo.UserVO;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;

@Service
public class ShopNGenieService{

	static Logger logger = LoggerFactory.getLogger(ShopNGenieService.class);
	
	@Autowired
	public ShopAudioPlayerListener shopAudioPlayerListerner;
	
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
	
	@Value("#{config['send.play.log']}")
	private String sendPlayLog ="";
	
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
	/*@Resource
	public ShopNGenieAudioPlayer shopNGenieAudioPlayer;*/
	
	
	
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
						
						Long removeGap = 0L; 
						
						if( "true".equals(removeSongGap)){
							removeGap = shopDownloadManager.removeQueueGap(songInfo.getSeq());
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
					
					
					if ( "true".equals(sendPlayLog)){
						shopHttpClient.allSendPlayLog(userAccountInfo);
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
		shopAudioPlayerListerner.setMedia(media);
	
		player.addBasicPlayerListener(shopAudioPlayerListerner);
	
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
				
				
				if ( songVO.getSeq().equals(media.getSeq())){
					media.setAlbumUid(songVO.getAlbumUid());
					media.setAppointUnit(songVO.getAppointUnit());
					media.setArtistName(songVO.getArtistName());
					media.setCexpDates(songVO.getCexpDates());
					media.setChainUid(songVO.getChainUid());
					media.setChannelUid(songVO.getChannelUid());
					media.setCoverImg(songVO.getCoverImg());
					media.setCoverImgBig(songVO.getCoverImgBig());
					media.setCoverImgMid(songVO.getCoverImgMid());
					media.setCurrentTime(songVO.getCurrentTime());
					media.setCutTime(songVO.getCutTime());
					media.setCutYn(songVO.getCutYn());
					media.setEndRunTime(songVO.getEndRunTime());
					media.setEndTime(songVO.getEndTime());
					//media.setFile(songVO.getFile());
					media.setFilePath(songVO.getFilePath());
					media.setFilePathNortest(songVO.getFilePathNortest());
					media.setIsActive(songVO.getIsActive());
					media.setPayAlertType(songVO.getPayAlertType());
					media.setPlayListUid(songVO.getPlayListUid());
					media.setPlayStartRunTime(songVO.getPlayStartRunTime());
					media.setPriority(songVO.getPriority());
					media.setRuntime(songVO.getRuntime());
					media.setScheduleType(songVO.getScheduleType());
					media.setScheduleUid(songVO.getScheduleUid());
					media.setSidCode(songVO.getSidCode());
					media.setSiteCode(songVO.getSiteCode());
					media.setSongLid(songVO.getSongLid());
					media.setSongTitle(songVO.getSongTitle());
					media.setSongType(songVO.getSongType());
					media.setSongUid(songVO.getSongUid());
					media.setStartRunTime(songVO.getStartRunTime());
					media.setStartTime(songVO.getStartTime());
					media.setStreamUrl(songVO.getStreamUrl());
					media.setUnder19Yn(songVO.getUnder19Yn());
					media.setVersion(songVO.getVersion());
					
				}
				
				player = new BasicPlayer();
				control = (BasicController)player;
				control.open(media.getFile());
				shopAudioPlayerListerner.setMedia(media);
				shopAudioPlayerListerner.setFirst();

			}else{
				response = shopHttpClient.getCDNMedia(songVO);
				int statusCode = response.getStatusLine().getStatusCode();			
				
				HttpEntity httpEntity = response.getEntity();			
		        
				logger.info("is http progressive streaming!!");
				
				player = new BasicPlayer();
				control = (BasicController)player;
				control.open(httpEntity.getContent());				
			}
			
			player.addBasicPlayerListener(shopAudioPlayerListerner);
			
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
				shopHttpClient.addPlayLog(media);
    	   }
       }
	          
	}
	
	
	
	
	/**
	 * streaming check
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
		return shopAudioPlayerListerner;
	}




	public void setApl(ShopAudioPlayerListener apl) {
		this.shopAudioPlayerListerner = apl;
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


}
