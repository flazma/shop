package com.genie.shop;



import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.genie.shop.vo.AppInfoVO;
import com.genie.shop.vo.ChannelVO;
import com.genie.shop.vo.MediaInfoVO;
import com.genie.shop.vo.ProductVO;
import com.genie.shop.vo.SongVO;
import com.genie.shop.vo.UserVO;

import javazoom.jl.player.Player;
import javazoom.jlgui.basicplayer.BasicPlayer;


@Component
public class ShopHttpClient{
	
	
	private String xauth = "";
	static Logger logger = LoggerFactory.getLogger(ShopHttpClient.class);
	
	HttpClient client = new DefaultHttpClient();
	
	public String sessionKey = "";
	
	
	@Value("#{config['basic.id']}")
	private String basicId = "";
	
	@Value("#{config['basic.passwd']}")
	private String basicPass = "";
	
	
	@Value("#{config['api.url']}")
	public String apiUrl = "";
	
	@Value("#{config['login.url']}")
	public String loginUrl;
	
	
	@Value("#{config['appinfo.url']}")
	public String appInfoUrl = "";
	
	@Value("#{config['lastschedules.url']}")
	public String lastSchedulesUrl = "";
	
	
	@Value("#{config['schedules.url']}")
	public String schedulesUrl = "";
	
	@Value("#{config['userinfo.url']}")
	public String userInfoUrl = "";
	
	
	@Value("#{config['currentsong.url']}")
	public String currentSongUrl = "";

	@Value("#{config['download.url']}")
	public String downloadUrl = "";
	
	@Value("#{config['play.log.url']}")
	public String playLogUrl = "";
	
	
	@Value("#{config['day.schedules.url']}")
	public String daySchedulesUrl = "";
	
	
	public String shopId;
	public String shopPasswd;
	
	
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

	public String getBasicId() {
		return basicId;
	}

	public void setBasicId(String basicId) {
		this.basicId = basicId;
	}

	public String getBasicPass() {
		return basicPass;
	}

	public void setBasicPass(String basicPass) {
		this.basicPass = basicPass;
	}

	public String getXauth() {
		return xauth;
	}

	public void setXauth(String xauth) {
		this.xauth = xauth;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}
	

	public static void main(String[] args) throws Exception {
		
		if (args.length <2){
			System.out.println("required shopId , shopPassword!!");
			System.exit(0);
		}
		
	}
	
	
	public String getHttpGetResponse(String url) throws Exception  {
		return getHttpGetResponse(url,null);
	}
	
	public String getHttpGetResponse(String url,String xauth) throws Exception{		
		
		HttpGet httpget = new HttpGet(apiUrl + url);
		HttpEntity httpEntity = null;
				
		httpget.removeHeaders("Authorization");
		httpget.removeHeaders("X-AuthorityKey");
		httpget.setHeader("Authorization", "Basic " + Base64.encodeBase64String((basicId +":" + basicPass).getBytes()));
		
		if ( xauth != null){
			httpget.setHeader("X-AuthorityKey", xauth);
		}
		
		HttpResponse response = client.execute(httpget);
		int statusCode = response.getStatusLine().getStatusCode();
		
		httpEntity = response.getEntity();
		
		return EntityUtils.toString(httpEntity);
		
	}
	
	public String getHttpPostResponse(String url,List<NameValuePair> formparams) throws Exception{		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		HttpPost httppost = new HttpPost(apiUrl + url);
		httppost.removeHeaders("Authorization");
		httppost.setEntity(entity);		
		httppost.setHeader("Authorization", "Basic " + Base64.encodeBase64String((basicId +":" + basicPass).getBytes()));
				
		HttpResponse response = client.execute(httppost);
		int statusCode = response.getStatusLine().getStatusCode();
		
		HttpEntity httpEntity = response.getEntity();
		
		return EntityUtils.toString(httpEntity);
	}
	
	
	
	
	/**
	 * API 헤더 추가, X auth 및 basic auth
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String setApiHeader(String url) throws Exception  {
		return setApiHeader(url,null);
	}
	
	/**
	 *  API 헤더 추가, X auth 및 basic auth
	 * @param url
	 * @param xauth
	 * @return
	 * @throws Exception
	 */
	public String setApiHeader(String url,String xauth) throws Exception{		
		
		HttpGet httpget = new HttpGet(apiUrl + url);
		HttpEntity httpEntity = null;
				
		httpget.removeHeaders("Authorization");
		httpget.removeHeaders("X-AuthorityKey");
		httpget.setHeader("Authorization", "Basic " + Base64.encodeBase64String((basicId +":" + basicPass).getBytes()));
		
		if ( xauth != null){
			logger.info("X-AuthorityKey:"+ xauth);
			httpget.setHeader("X-AuthorityKey", xauth);			
		}
		
		HttpResponse response = client.execute(httpget);
		httpEntity = response.getEntity();
		
		return EntityUtils.toString(httpEntity);
		
	}
	
	
	public String setPostApiHeader(String url,List<NameValuePair> formparams) throws Exception{
		return setPostApiHeader(url,formparams,null);
	}
	
	/**
	 * POST용 API 
	 * @param url
	 * @param formparams
	 * @return
	 * @throws Exception
	 */
	public String setPostApiHeader(String url,List<NameValuePair> formparams, String xauth) throws Exception{		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		HttpPost httppost = new HttpPost(apiUrl + url);
		httppost.removeHeaders("Authorization");		
		httppost.removeHeaders("X-AuthorityKey");
		httppost.setEntity(entity);		
		httppost.setHeader("Authorization", "Basic " + Base64.encodeBase64String((basicId +":" + basicPass).getBytes()));
		
		if ( xauth != null){
			logger.info("X-AuthorityKey:"+ xauth);
			httppost.setHeader("X-AuthorityKey", xauth);
		}
		
		HttpResponse response = client.execute(httppost);
		HttpEntity httpEntity = response.getEntity();
		
		return EntityUtils.toString(httpEntity);
	}
	
	
	/**
	 * 재생로그 전송 api
	 * @param url
	 * @param formparams
	 * @param songInfo
	 * @throws Exception
	 */
	public void sendPlayLog(UserVO userInfo,SongVO songInfo, List<NameValuePair> formparams) throws Exception {

		logger.info("is play log songUid=" + songInfo.getSongUid() + ",title=" + songInfo.getSongTitle());
		
		formparams.add(new BasicNameValuePair("siteCode", songInfo.getSiteCode()));
		formparams.add(new BasicNameValuePair("sidCode", songInfo.getSidCode()));
		formparams.add(new BasicNameValuePair("chainUid", "" + songInfo.getChainUid()));
		formparams.add(new BasicNameValuePair("shopUid", "" + userInfo.getShopUid() ));
		formparams.add(new BasicNameValuePair("channelUid", "" + songInfo.getChannelUid() ));
		formparams.add(new BasicNameValuePair("albumUid", "" + songInfo.getAlbumUid() ));
		formparams.add(new BasicNameValuePair("scheduleUid", "" + songInfo.getScheduleUid() ));
		formparams.add(new BasicNameValuePair("songUid", "" + songInfo.getSongUid() ));
		formparams.add(new BasicNameValuePair("songLid", "" + songInfo.getSongLid() ));
		formparams.add(new BasicNameValuePair("cmYn", "" + (songInfo.getSongType().equals("CM") ? "Y" : "N" ) ));
		
		String songJson = setPostApiHeader(playLogUrl,formparams,userInfo.getSessionKey());
		
		logger.info("is play log result =" + songJson);
		
	}
	
	
	public UserVO loginUser() throws Exception{
		return loginUser(shopId,shopPasswd);
	}
	
	/**
	 * userInfo parse
	 * @param loginJson
	 * @return
	 * @throws Exception
	 */
	public UserVO loginUser(String shopId, String shopPasswd) throws Exception{
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("shopId", shopId));
		formparams.add(new BasicNameValuePair("shopPassword", shopPasswd));
		
		String loginJson = setPostApiHeader( loginUrl,formparams);
		
		logger.info("login json=" + loginJson);
		
		UserVO userInfo = new UserVO();
		String sessionKey = "";
		String rtMsg = "";
		
		JSONParser par = new JSONParser();
		
		JSONObject json = (JSONObject)par.parse(loginJson);
		JSONObject jsonData = (JSONObject)json.get("data");		
		String rtCode = StringUtils.trimToEmpty((String)jsonData.get("rtCode"));
		
		if ("0".equals(rtCode)){
			
			sessionKey = StringUtils.trimToEmpty((String)jsonData.get("sessionKey"));
			rtCode = StringUtils.trimToEmpty((String)jsonData.get("rtCode"));
			rtMsg = StringUtils.trimToEmpty((String)jsonData.get("rtMsg"));
		
		}else{
			throw new Exception("error :" + rtCode+","+rtMsg);
		}
		
		return getAccountInfo(shopId,sessionKey);		
		
	}
	
	
	/**
	 * AppInfo parse
	 * @param appJson
	 * @return
	 * @throws Exception
	 */
	public AppInfoVO getAppInfo(UserVO userInfoLogin) throws Exception {
		
		String reAppInfo = StringUtils.replace(appInfoUrl, "#chainUid#", "" + userInfoLogin.getChainUid());
		reAppInfo = StringUtils.replace(reAppInfo, "#shopUid#", "" + userInfoLogin.getShopUid());
		
		
		String appJson = setApiHeader(reAppInfo);
		
		AppInfoVO appInfo = null;
		
		JSONParser par = new JSONParser();
		
		JSONObject json = (JSONObject)par.parse(appJson);
		JSONObject jsonData = (JSONObject)json.get("data");		
		
		String rtCode = StringUtils.trimToEmpty((String)jsonData.get("rtCode"));
		
		if ("0".equals(rtCode)){
			
			appInfo = new AppInfoVO();
			JSONObject jsonParams = (JSONObject)((JSONObject)jsonData.get("params")).get("application");
			
			appInfo.setAppUid(StringUtils.trimToEmpty((String)jsonParams.get("appUid")));
			appInfo.setPlatform(StringUtils.trimToEmpty((String)jsonParams.get("platform")));
			appInfo.setVersion(StringUtils.trimToEmpty((String)jsonParams.get("version")));
			appInfo.setMarket(StringUtils.trimToEmpty((String)jsonParams.get("market")));
			appInfo.setFilePath(StringUtils.trimToEmpty((String)jsonParams.get("filePath")));
			
			
			//{"mdate":null,"appUid":"24","version":"01.01.05"}
			//2016-09-22 11:18:59
			SimpleDateFormat dateParse = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss"); 
			String strDate = StringUtils.trimToEmpty((String)jsonParams.get("rdate"));
			Date rDate = dateParse.parse(strDate);
			appInfo.setDeployDate(rDate);
			
			
			dateParse = new SimpleDateFormat("yyyy-mm-dd"); 
			strDate = StringUtils.trimToEmpty((String)jsonParams.get("deployDate"));
			Date deployDate = dateParse.parse(strDate);		
			appInfo.setDeployDate(deployDate);
		}
		return appInfo;
	}
	
	/**
	 * 채널 정보 parse 
	 * @param channelJson
	 * @return
	 * @throws Exception
	 */
	public ChannelVO getChannelInfo(String shopId) throws Exception{
		
		
		String channelJson = setApiHeader(StringUtils.replace(lastSchedulesUrl, "#shopId#", shopId));
		
		ChannelVO channelInfo = new ChannelVO();
		
		JSONParser par = new JSONParser();
		
		JSONObject json = (JSONObject)par.parse(channelJson);
		JSONObject jsonData = (JSONObject)json.get("data");	
		String rtCode = (String)jsonData.get("rtCode");
		
		
		if ("0".equals(rtCode)){
		
			JSONObject jsonParams = (JSONObject)jsonData.get("params");
			
			channelInfo.setChannelName(StringUtils.trimToEmpty((String)jsonParams.get("lastestChannelName")));
			channelInfo.setRootChannelName(StringUtils.trimToEmpty((String)jsonParams.get("lastestRootChannelName")));
			channelInfo.setRootChannelUid((Long)jsonParams.get("lastestRootChannelUid"));
			channelInfo.setIsLikeSchedule(StringUtils.trimToEmpty((String)jsonParams.get("isLikeSchedule")));
			channelInfo.setScheduleUid((Long)jsonParams.get("lastestScheduleUid"));
			channelInfo.setScheduleName(StringUtils.trimToEmpty((String)jsonParams.get("lastestScheduleName")));
			channelInfo.setChannelUid((Long)jsonParams.get("lastestChannelUid"));
			
			logger.info(channelInfo.getRootChannelName()+","+channelInfo.getScheduleName());
		}
		
		return channelInfo;
	}
	
	
	/**
	 * 채널 리스트 파싱 api 
	 * @param reSchduleUrl
	 * @return
	 * @throws Exception
	 */
	public ArrayList<ChannelVO> getChannelList(UserVO userInfoLogin,ChannelVO lastChannelInfo) throws Exception{
		
		
		String reSchduleUrl = StringUtils.replace(schedulesUrl, "#scheduleId#", "" + lastChannelInfo.getChannelUid());
		reSchduleUrl = StringUtils.replace(reSchduleUrl, "#chainUid#", "" + userInfoLogin.getChainUid());
		reSchduleUrl = StringUtils.replace(reSchduleUrl, "#shopUid#", "" + userInfoLogin.getShopUid());
		
		
		String channelJson = setApiHeader(reSchduleUrl);
		
		ArrayList<ChannelVO> arrChannelInfo = new ArrayList();
		
		JSONParser par = new JSONParser();
		
		JSONObject json = (JSONObject)par.parse(channelJson);
		JSONObject jsonData = (JSONObject)json.get("data");		
		String rtCode = (String)jsonData.get("rtCode");
		
		
		if ( "0".equals(rtCode)){
		
			JSONArray jsonArray = (JSONArray)((JSONObject)jsonData.get("params")).get("channelList");
			
			for(int i=0,j=jsonArray.size(); i<j ; i++){
				ChannelVO channelInfo = new ChannelVO();
				JSONObject jsonList = (JSONObject)jsonArray.get(i);
				
				channelInfo.setChainUid((Long)jsonList.get("chainUid"));
				channelInfo.setChainName(StringUtils.trimToEmpty((String)jsonList.get("chainName")));
				channelInfo.setAlbumUid((Long)jsonList.get("albumUid"));
				channelInfo.setAlbumName(StringUtils.trimToEmpty((String)jsonList.get("albumName")));
				channelInfo.setChannelUid((Long)jsonList.get("channelUid"));
				channelInfo.setChannelName(StringUtils.trimToEmpty((String)jsonList.get("channelName")));
				channelInfo.setScheduleUid((Long)jsonList.get("scheduleUid"));
				channelInfo.setScheduleName(StringUtils.trimToEmpty((String)jsonList.get("scheduleName")));
				channelInfo.setScheduleDesc(StringUtils.trimToEmpty((String)jsonList.get("scheduleDesc")));
				channelInfo.setSongTitle(StringUtils.trimToEmpty((String)jsonList.get("songTitle")));
				channelInfo.setCoverImgBig(StringUtils.trimToEmpty((String)jsonList.get("coverImgBig")));
				channelInfo.setCoverImgMid(StringUtils.trimToEmpty((String)jsonList.get("coverImgMid")));
				channelInfo.setScheduleSongType(StringUtils.trimToEmpty((String)jsonList.get("scheduleSongType")));
				channelInfo.setLikeSchedule(StringUtils.trimToEmpty((String)jsonList.get("likeSchedule")));
				channelInfo.setFilePath(StringUtils.trimToEmpty((String)jsonList.get("filePath")));
				
				arrChannelInfo.add(channelInfo);
			}
		}
		return arrChannelInfo;
	}
	
	/**
	 * 일별 스케쥴정보 조회
	 * @param shopId
	 * @param sessionKey
	 * @return
	 * @throws Exception
	 */
	public ArrayList<SongVO> getDaySchedules(Long channelUid, Long schedulesUid, Long chainUid, String sessionKey) throws Exception{
		
		//#channelUid#/schedules/#schedulesUid#?chainUid=#chainUid#
		String tmpDaySchedulesUrl = StringUtils.replace(daySchedulesUrl, "#channelUid#", "" + channelUid);		
		tmpDaySchedulesUrl = StringUtils.replace(tmpDaySchedulesUrl, "#schedulesUid#", "" + schedulesUid);
		tmpDaySchedulesUrl = StringUtils.replace(tmpDaySchedulesUrl, "#chainUid#", "" + chainUid);
		
		String songJson = setApiHeader(tmpDaySchedulesUrl,sessionKey);
		
		ArrayList<SongVO> arrSongVO = new ArrayList();
		
		JSONParser par = new JSONParser();
		
		JSONObject json = (JSONObject)par.parse(songJson);
		JSONObject jsonData = (JSONObject)json.get("data");		
		String  rtCode = (String)jsonData.get("rtCode");
		
		
		if ( "0".equals(rtCode)){
		
			JSONArray jsonResult = (JSONArray)((JSONObject)jsonData.get("params")).get("songList");
				
			for(int i=0,j=jsonResult.size(); i < j ; i++){
				JSONObject songObject = (JSONObject)jsonResult.get(i);
				SongVO songInfo = new SongVO();
						
				songInfo.setPlayListUid((Long)songObject.get("play_list_uid"));
				songInfo.setChainUid((Long)songObject.get("chain_uid"));
				songInfo.setAlbumUid((Long)songObject.get("album_uid"));
				songInfo.setScheduleUid((Long)songObject.get("schedule_uid"));
				songInfo.setScheduleType(StringUtils.trimToEmpty((String)songObject.get("schedule_type")));
				songInfo.setVersion(StringUtils.trimToEmpty((String)songObject.get("version")));
				songInfo.setSeq((Long)songObject.get("seq"));
				songInfo.setStartTime(StringUtils.trimToEmpty((String)songObject.get("start_time")));
				songInfo.setEndTime(StringUtils.trimToEmpty((String)songObject.get("end_time")));
				songInfo.setStartRunTime((Long)songObject.get("start_run_time"));
				songInfo.setEndRunTime((Long)songObject.get("end_run_time"));
				songInfo.setIsActive(StringUtils.trimToEmpty((String)songObject.get("is_active")));
				//payAlert 추가 되어야 함
				songInfo.setAppointUnit(StringUtils.trimToEmpty((String)songObject.get("appoint_unit")));
				songInfo.setPriority((Long)songObject.get("priority"));
				songInfo.setFilePathNortest(StringUtils.trimToEmpty((String)songObject.get("file_path_nortest")));
				songInfo.setSongUid((Long)songObject.get("song_uid"));
				songInfo.setSongLid((Long)songObject.get("song_lid"));
				songInfo.setSongTitle(StringUtils.trimToEmpty((String)songObject.get("song_title")));
				songInfo.setArtistName(StringUtils.trimToEmpty((String)songObject.get("artist_name")));
				songInfo.setUnder19Yn(StringUtils.trimToEmpty((String)songObject.get("under19_yn")));
				songInfo.setCoverImg(StringUtils.trimToEmpty((String)songObject.get("cover_img")));
				songInfo.setCoverImgMid(StringUtils.trimToEmpty((String)songObject.get("cover_img_mid")));
				songInfo.setCoverImgBig(StringUtils.trimToEmpty((String)songObject.get("cover_img_big")));
				songInfo.setFilePath(StringUtils.trimToEmpty((String)songObject.get("file_path")));
				songInfo.setStreamUrl(StringUtils.trimToEmpty((String)songObject.get("stream_url")));
				songInfo.setRuntime((Long)songObject.get("runtime"));
				songInfo.setSongType(StringUtils.trimToEmpty((String)songObject.get("song_type")));
				songInfo.setCutYn(StringUtils.trimToEmpty((String)songObject.get("cut_yn")));
				songInfo.setCutTime((Long)songObject.get("cutTime"));
				songInfo.setCurrentTime((Long)songObject.get("current_time"));
				songInfo.setPlayStartRunTime((Long)songObject.get("play_start_run_time"));
				songInfo.setSiteCode(StringUtils.trimToEmpty((String)songObject.get("siteCode")));
				songInfo.setSidCode(StringUtils.trimToEmpty((String)songObject.get("sidCode")));
				songInfo.setCexpDates(StringUtils.trimToEmpty((String)songObject.get("cexpdates")));
				
				arrSongVO.add(songInfo);
			}
		}
		return arrSongVO;
	}
	
	
	/**
	 * 계정정보 파싱
	 * @param shopId
	 * @param sessionKey
	 * @return
	 * @throws Exception
	 */
	public UserVO getAccountInfo(String shopId, String sessionKey) throws Exception{
		
		String accountJson = setApiHeader(StringUtils.replace(userInfoUrl, "#shopId#", shopId),sessionKey);
		
		
		UserVO userInfo = new UserVO();
		
		JSONParser par = new JSONParser();
		
		JSONObject json = (JSONObject)par.parse(accountJson);
		JSONObject jsonData = (JSONObject)json.get("data");		
		String  rtCode = (String)jsonData.get("rtCode");
		
		
		if ( "0".equals(rtCode)){
		
			JSONObject jsonResult = (JSONObject)((JSONObject)jsonData.get("params")).get("result");
			JSONObject jsonDefaultInfo = (JSONObject)jsonResult.get("defaultInfo");
			
			userInfo.setOldPasswdCheck((Boolean)jsonDefaultInfo.get("oldPasswdCheck"));
			userInfo.setAccountId(StringUtils.trimToEmpty((String)jsonDefaultInfo.get("accountId")));
			userInfo.setShopUid((Long)jsonDefaultInfo.get("shopUid"));
			userInfo.setPhoneNumber(StringUtils.trimToEmpty((String)jsonDefaultInfo.get("phoneNumber")));
			userInfo.setEmail(StringUtils.trimToEmpty((String)jsonDefaultInfo.get("email")));
			userInfo.setAllianceCode(StringUtils.trimToEmpty((String)jsonDefaultInfo.get("allianceCode")));
			userInfo.setShopName(StringUtils.trimToEmpty((String)jsonDefaultInfo.get("shopName")));
			userInfo.setChainUid((Long)jsonDefaultInfo.get("chainUid"));
			
			
			JSONObject jsonProductInfo = (JSONObject)jsonResult.get("productInfo");
			SimpleDateFormat dateParse = new SimpleDateFormat("yyyy.mm.dd");		
			String strCexpDate = (String)jsonDefaultInfo.get("cexpDate");
			
			if ( strCexpDate != null ){
				userInfo.setCexpDate(dateParse.parse(strCexpDate));
			}
			
			userInfo.setProdName(StringUtils.trimToEmpty((String)jsonDefaultInfo.get("prodName")));
			userInfo.setBpayNos(StringUtils.trimToEmpty((String)jsonDefaultInfo.get("bpayNos")));
			userInfo.setBpayYn(StringUtils.trimToEmpty((String)jsonDefaultInfo.get("bpayYn")));
			
			String strConsumeDate = (String)jsonDefaultInfo.get("consumEdate");
			
			if ( strConsumeDate != null){
				userInfo.setConsumeDate(dateParse.parse(strConsumeDate));
			}
			
			
			
			JSONArray jsonProductArray = (JSONArray)((JSONObject)jsonResult.get("productList")).get("myProductList");
					
			//상품 정보 parse
			for(int i=0,j=jsonProductArray.size(); i < j ; i++){
				JSONObject product = (JSONObject)jsonProductArray.get(i);
				ProductVO productInfo = new ProductVO();
				productInfo.setRowNo(StringUtils.trimToEmpty((String)product.get("ROW_NO")));			
				productInfo.setMchargeNo(StringUtils.trimToEmpty((String)product.get("MCHARGENO")));
				productInfo.setPackageName(StringUtils.trimToEmpty((String)product.get("PACKAGENAME")));
				
				dateParse = new SimpleDateFormat("yyyy.mm.dd HH:mm:ss");
				
				productInfo.setCosumeDate(dateParse.parse(StringUtils.trimToEmpty((String)product.get("CONSUMEDATE"))));						
				productInfo.setCexpDate(dateParse.parse(StringUtils.trimToEmpty((String)product.get("CEXPDATE"))));			
				productInfo.setYmd(StringUtils.trimToEmpty((String)product.get("YMD")));
				productInfo.setPayToolm(StringUtils.trimToEmpty((String)product.get("PAYTOOLM")));
				productInfo.setUseState(StringUtils.trimToEmpty((String)product.get("USESTATE")));
				
				dateParse = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
				productInfo.setRegDate(dateParse.parse(StringUtils.trimToEmpty((String)product.get("REGDATE"))));
				productInfo.setUseState(StringUtils.trimToEmpty((String)product.get("PACKAGEID")));
				
				userInfo.getProductList().add(productInfo);				
			}
			
			userInfo.setSessionKey(sessionKey);
			
		}
		return userInfo;
	}
	
	/**
	 * 현재곡 조회
	 * 중복 로그인 시 강제 로그인을 진행 후 현재 재생곡 조회 진행
	 * @param songJson
	 * @return
	 * @throws Exception
	 */
	public ArrayList<SongVO> getCurrentSong(UserVO userVO, ChannelVO songChannel, Long seq) throws Exception{
		

		String strCurrentSongUrl = StringUtils.replace(currentSongUrl, "#channelUid#", "" + songChannel.getChannelUid());
		strCurrentSongUrl = StringUtils.replace(strCurrentSongUrl, "#schedulesUid#", "" + songChannel.getScheduleUid());
		strCurrentSongUrl = StringUtils.replace(strCurrentSongUrl, "#shopUid#", "" + userVO.getShopUid());		
		strCurrentSongUrl = StringUtils.replace(strCurrentSongUrl, "#seq#", "" + seq);
		
		String songJson = setApiHeader(strCurrentSongUrl,userVO.getSessionKey());
		
		
		logger.info("schedule songInfo "+ songJson+"\n");
		
		ArrayList<SongVO> arrSongInfo = null;
				
		JSONParser par = new JSONParser();
		
		JSONObject json = (JSONObject)par.parse(songJson);
		JSONObject jsonData = (JSONObject)json.get("data");
		
		String rtCode = (String)jsonData.get("rtCode");
		
		
		if ( "0".equals(rtCode)){	//성공이면		
			
			JSONArray jsonResult = (JSONArray)((JSONObject)jsonData.get("params")).get("song");
			arrSongInfo = parseSong(jsonResult);
			
		}/*else if ("SG4035".equals(rtCode)){ //중복 로그인 이면
			
			//강제 로그인
			userVO = loginUser();
			
			getChannelInfo(shopId);
			
			songJson = setApiHeader(strCurrentSongUrl,userVO.getSessionKey());
			
			json = (JSONObject)par.parse(songJson);
			jsonData = (JSONObject)json.get("data");
			
			rtCode = (String)jsonData.get("rtCode");
			
			JSONArray jsonResult = (JSONArray)((JSONObject)jsonData.get("params")).get("song");
			arrSongInfo = parseSong(jsonResult);
		}
		*/
		return arrSongInfo;
		
	}
	
	
	private ArrayList<SongVO> parseSong(JSONArray jsonResult){
		
		ArrayList<SongVO> arrSongInfo = new ArrayList<SongVO>();
		
		if ( jsonResult != null){
		
			for(int i=0,j=jsonResult.size(); i < j ; i++){
				JSONObject songObject = (JSONObject)jsonResult.get(i);
				SongVO songInfo = new SongVO();
				songInfo.setPlayListUid((Long)songObject.get("play_list_uid"));
				songInfo.setChainUid((Long)songObject.get("chain_uid"));
				songInfo.setAlbumUid((Long)songObject.get("album_uid"));
				songInfo.setChannelUid((Long)songObject.get("channel_uid"));
				songInfo.setScheduleUid((Long)songObject.get("schedule_uid"));
				songInfo.setScheduleType(StringUtils.trimToEmpty((String)songObject.get("schedule_type")));
				songInfo.setVersion(StringUtils.trimToEmpty((String)songObject.get("version")));
				songInfo.setSeq((Long)songObject.get("seq"));
				songInfo.setStartTime(StringUtils.trimToEmpty((String)songObject.get("start_time")));
				songInfo.setEndTime(StringUtils.trimToEmpty((String)songObject.get("end_time")));
				songInfo.setStartRunTime((Long)songObject.get("start_run_time"));
				songInfo.setEndRunTime((Long)songObject.get("end_run_time"));
				songInfo.setIsActive(StringUtils.trimToEmpty((String)songObject.get("is_active")));
				songInfo.setPayAlertType((Boolean)((JSONObject)songObject.get("payAlert")).get("type"));
				songInfo.setAppointUnit(StringUtils.trimToEmpty((String)songObject.get("appoint_unit")));
				songInfo.setPriority((Long)songObject.get("priority"));
				songInfo.setFilePathNortest(StringUtils.trimToEmpty((String)songObject.get("file_path_nortest")));
				songInfo.setSongUid((Long)songObject.get("song_uid"));			
				songInfo.setSongLid((Long)songObject.get("song_lid"));
				songInfo.setSongTitle(StringUtils.trimToEmpty((String)songObject.get("song_title")));
				songInfo.setArtistName(StringUtils.trimToEmpty((String)songObject.get("artist_name")));
				songInfo.setUnder19Yn(StringUtils.trimToEmpty((String)songObject.get("under19_yn")));
				songInfo.setCoverImg(StringUtils.trimToEmpty((String)songObject.get("cover_img")));
				songInfo.setCoverImgMid(StringUtils.trimToEmpty((String)songObject.get("cover_img_mid")));
				songInfo.setCoverImgBig(StringUtils.trimToEmpty((String)songObject.get("cover_img_big")));
				songInfo.setStreamUrl(StringUtils.trimToEmpty((String)songObject.get("stream_url")));
				songInfo.setFilePath(StringUtils.trimToEmpty((String)songObject.get("file_path")));
				songInfo.setRuntime((Long)songObject.get("runtime"));
				songInfo.setSongType(StringUtils.trimToEmpty((String)songObject.get("song_type")));
				songInfo.setCutYn(StringUtils.trimToEmpty((String)songObject.get("cut_yn")));
				songInfo.setCutTime((Long)songObject.get("cutTime"));
				songInfo.setCurrentTime((Long)songObject.get("current_time"));
				songInfo.setPlayStartRunTime((Long)songObject.get("play_start_run_time"));
				songInfo.setSiteCode(StringUtils.trimToEmpty((String)songObject.get("siteCode")));
				songInfo.setSidCode(StringUtils.trimToEmpty((String)songObject.get("sidCode")));
				songInfo.setCexpDates(StringUtils.trimToEmpty((String)songObject.get("cexpdates")));
				logger.info("seq=" +songInfo.getSeq()+",starttime="+songInfo.getStartTime() +",endtime="+ songInfo.getEndTime()+",songUid="+songInfo.getSongUid()+",artist="+songInfo.getArtistName()+",title="+songInfo.getSongTitle()+",playstarttime="+songInfo.getPlayStartRunTime());			
				
				arrSongInfo.add(songInfo);
			}
		}
		
		return arrSongInfo;
		
	}
	
	
	
	/**
	 * ��Ʈ���� ���� 
	 * @param shopId
	 * @param shopPasswd
	 * @throws Exception
	 */
	/*public void startShopGenie(String shopId,String shopPasswd) throws Exception{

		client = new DefaultHttpClient();
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("shopId", shopId));
		formparams.add(new BasicNameValuePair("shopPassword", shopPasswd));
		//�α��� ó��
		UserVO userInfoLogin = parseUserInfo(postApi(loginUrl,formparams));
		
		//���� ���� ��ȸ
		UserVO userAccountInfo = parseAccountInfo(setApiHeader(StringUtils.replace(userInfoUrl, "#shopId#", shopId),userInfoLogin.getSessionKey()));
		
		//������ ä�� �缺 ���� ��ȸ
		ChannelVO lastChannelInfo = parseChannelInfo(setApiHeader(StringUtils.replace(lastSchedulesUrl, "#shopId#", shopId)));

		
		//app���� ��ȸ
		String reAppInfo = StringUtils.replace(appInfoUrl, "#chainUid#", "" + userInfoLogin.getChainUid());
		reAppInfo = StringUtils.replace(reAppInfo, "#shopUid#", "" + userInfoLogin.getShopUid());

		AppInfoVO appInfo = parseAppInfo(setApiHeader(reAppInfo));
		
		while(true){
			//������ ����Ʈ
			//schedulesUrl = "/v1/api/channels/#scheduleId#/schedules?shopUid=#shopUid#&chainUid=#chainUid#";
			String reSchduleUrl = StringUtils.replace(schedulesUrl, "#scheduleId#", "" + lastChannelInfo.getChannelUid());
			reSchduleUrl = StringUtils.replace(reSchduleUrl, "#chainUid#", "" + userInfoLogin.getChainUid());
			reSchduleUrl = StringUtils.replace(reSchduleUrl, "#shopUid#", "" + userInfoLogin.getShopUid());
	
			ArrayList<ChannelVO> arrChannelList = parseChannelList(setApiHeader(reSchduleUrl));
			
			ChannelVO songChannel = arrChannelList.get(0);
			
			//����� ��ȸ
			String strCurrentSongUrl = StringUtils.replace(currentSongUrl, "#channelUid#", "" + songChannel.getChainUid());
			strCurrentSongUrl = StringUtils.replace(strCurrentSongUrl, "#schedulesUid#", "" + songChannel.getScheduleUid());
			strCurrentSongUrl = StringUtils.replace(strCurrentSongUrl, "#shopUid#", "" + userInfoLogin.getShopUid());
			ArrayList<SongVO> songInfoList = parseSongInfo(setApiHeader(strCurrentSongUrl,userInfoLogin.getSessionKey()));
	
			SongVO songInfo = songInfoList.get(0);
			
			playMusic(songInfo);
			Thread.sleep(1000);
		}
		
		
	    loginApiCall "https://api-shop.genie.co.kr/v1/api/notices?chainUid=118&firstRecordIndex=0", "GET"
	    //playlist ��ȸ
	    https://api-shop.genie.co.kr/v1/api/channels/33/schedules/1?chainUid=185, GET
	    
		
	}*/
	
	public HttpClient getClient(){
		return client;
	}
	
	
	
	/**
	 * TTS 재생
	 * @param player
	 * @param artistName
	 * @param songTitle
	 */
	public void playTTS(Player player,String artistName,String songTitle){
		String ttsStr = "http://translate.google.com/translate_tts?ie=UTF-8&total=1&idx=0&textlen=60&client=tw-ob&tl=ko-kr&q=";
		HttpGet httpttsGet = null;
		HttpResponse response =null;
		
		try{
			
			httpttsGet = new HttpGet(ttsStr + URLEncoder.encode(artistName+" 의 ","UTF-8") + URLEncoder.encode(songTitle+" 이 재생됩니다","UTF-8"));
			response = client.execute(httpttsGet);		
			
			HttpEntity httpEntity = response.getEntity();
			player = new Player(httpEntity.getContent());
			player.play();
		}catch(Exception e){
			logger.warn(e.toString());
		}
	}
	
	
	/**
	 * CDN에 따른 스트링 리턴
	 * @param url
	 * @return
	 */
	public InputStream getCDNMedia(String url) {
		HttpGet httpttsGet = null;
		HttpResponse response =null;
		HttpEntity httpEntity =  null;
		InputStream inp = null;
		try{
			
			httpttsGet = new HttpGet(url);
			response = client.execute(httpttsGet);		
			
			httpEntity = response.getEntity();
			inp =  httpEntity.getContent();
			
		}catch(Exception e){
			logger.warn(e.toString());
		}
		
		return inp;
	}
	
}
