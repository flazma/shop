package com.genie.shop;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import com.genie.shop.vo.AppInfoVO;
import com.genie.shop.vo.ChannelVO;
import com.genie.shop.vo.MediaInfoVO;
import com.genie.shop.vo.SongVO;
import com.genie.shop.vo.UserVO;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


@Controller
public class ShopNGenieController {
	
	static Logger logger = LoggerFactory.getLogger(ShopNGenieController.class);
	
	@Autowired
	private ShopHttpClient shopHttpClient;
	
	@Autowired
	private ShopDownloadManager shopDownloadManager;
	
	private String shopId="";
	private String shopPasswd = "";
	
	private Boolean IS_FIRST= true;
	
	UserVO userInfoLogin = null;
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
	
	/**
	 * 
	 */
	public Player player = null;
	
	
	public static void main(String[] args) throws Exception {
		
		if (args.length <2){
			System.out.println("required shopId , shopPassword!!");
			System.exit(0);
		}
		
		ShopNGenieController c = new ShopNGenieController();
		c.shopId = args[0];
		c.shopPasswd = args[1];
		c.loadConfig();
		c.checkConfiguration();		
		c.run();
	}
	
	
	
	
	public void start() throws Exception {
		loadConfig();
		checkConfiguration();		
		run();
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
			logger.warn(e.toString());
		}
		
	}
	
	/**
	 * configuration load
	 * create dir
	 * delete cache mp3 files
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
	}
	
	public void run() throws Exception {
		
		//1. app 
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("shopId", shopId));
		formparams.add(new BasicNameValuePair("shopPassword", shopPasswd));
		
		//로그인
		userInfoLogin = shopHttpClient.parseUserInfo(formparams);
		
		//계정정보
		userAccountInfo = shopHttpClient.parseAccountInfo(shopId,userInfoLogin.getSessionKey());
		
		//채널정보
		ChannelVO lastChannelInfo = shopHttpClient.parseChannelInfo(shopId);
		
		//app 로그인 후 업데이트 정보 
		appInfo = shopHttpClient.parseAppInfo(userInfoLogin);
		Long seq = -1L;
		
		boolean isFirst = true;
		
		while(true){
			ArrayList<ChannelVO> arrChannelList = shopHttpClient.parseChannelList(userInfoLogin,lastChannelInfo);
				
			if(arrChannelList != null && arrChannelList.size() > 0){
			
				ChannelVO songChannel = arrChannelList.get(0);				
				//		
				ArrayList<SongVO> songInfoList = shopHttpClient.parseSongInfo(userInfoLogin, songChannel,seq);
				
				SongVO songInfo = null;
				
				//증복 로그인 시 size = 0  리턴
				if ( songInfoList.size() != 0 ){
					songInfo = songInfoList.get(0);
					
					shopDownloadManager.addQueueMedia(userInfoLogin, songChannel,songInfo.getSeq());
					
					playMusic(songInfo);
					seq = songInfo.getSeq();
				}else{
					Thread.sleep(10 * 1000);
				}
				
				Thread.sleep(1000);
				
			}
		}
		
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
		//BasicPlayer player = null;
		Player player = null;
		MediaInfoVO media = null;
		try{
			
			
		logger.info("download queue size=" + shopDownloadManager.queue.size());
			//queue 사이즈가 0보다 크면...
			if ( shopDownloadManager.queue.size() > 0 ){
				
				media = shopDownloadManager.poll();
				logger.info("is queue poll seq(" + media.getSeq()+")");
				InputStream stream = new FileInputStream(media.getFile());
				
				player = new Player(stream);
			}else{
				response = client.execute(httpget);		
				int statusCode = response.getStatusLine().getStatusCode();			
				
				HttpEntity httpEntity = response.getEntity();			
		        
				
				logger.info("is http progressive streaming!!");
				player = new Player(httpEntity.getContent());
			}
				
			//player = new BasicPlayer();
			//player.open(httpEntity.getContent());
			//logger.info("\nplayer position:"+ String.format("%,d", Integer.parseInt(""+startByte)) + " bytes");
			//player.seek(startByte);
			
			logger.info("is playing!!");
			
			player.play();
			
			if ( media != null){
				media.file.delete();
			}
			
       }catch (JavaLayerException je) {
    	   //Cannot create AudioDevice 
    	   logger.warn(je.toString());
       }
		catch (Exception e) {
			logger.warn(e.toString());
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


	public ShopHttpClient getShc() {
		return shopHttpClient;
	}




	public void setShc(ShopHttpClient shc) {
		this.shopHttpClient = shc;
	}




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




	public Boolean getIS_FIRST() {
		return IS_FIRST;
	}




	public void setIS_FIRST(Boolean iS_FIRST) {
		IS_FIRST = iS_FIRST;
	}




	public UserVO getUserInfoLogin() {
		return userInfoLogin;
	}




	public void setUserInfoLogin(UserVO userInfoLogin) {
		this.userInfoLogin = userInfoLogin;
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




	public Player getPlayer() {
		return player;
	}




	public void setPlayer(Player player) {
		this.player = player;
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


	
}
