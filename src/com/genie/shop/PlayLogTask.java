package com.genie.shop;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.genie.shop.vo.MediaInfoVO;
import com.genie.shop.vo.UserVO;

public class PlayLogTask implements Runnable{

	static Logger logger = LoggerFactory.getLogger(PlayLogTask.class);

	@Value("#{config['play.log.url']}")
	public String playLogUrl = "";
	
	@Value("#{config['user.agent']}")
	private String userAgent = "";
	
	@Value("#{config['basic.id']}")
	private String basicId = "";
	
	@Value("#{config['basic.passwd']}")
	private String basicPass = "";
	
	
	@Value("#{config['api.url']}")
	public String apiUrl = "";
	
	HttpClient client = new DefaultHttpClient();
	
	
	public MediaInfoVO mediaInfo;
	public UserVO user;
	public ArrayList<MediaInfoVO> playLogQueue;
	
	public PlayLogTask(MediaInfoVO mediaInfo, UserVO user,String apiUrl, String playLogUrl, String userAgent, String basicId, String basicPass){
		this.mediaInfo = mediaInfo;
		this.user = user;
		this.apiUrl = apiUrl;
		this.playLogUrl = playLogUrl;
		this.userAgent = userAgent;
		this.basicId = basicId;
		this.basicPass = basicPass;
		
	}
	
	@Override
	public void run(){
		logger.info("!!asynch PlayLog!!");
		try{
			sendPlayLog(user,mediaInfo);
		}catch(Exception e){
			logger.warn("sendPlayLog Exception:",e);
		}
	}
	
	
	public void sendPlayLog(UserVO user,MediaInfoVO mediaInfo) throws Exception {

		logger.info("####### asynch play log songUid=" + mediaInfo.getSongUid() + ",title=" + mediaInfo.getSongTitle());
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		
		formparams.add(new BasicNameValuePair("siteCode", StringUtils.defaultString(mediaInfo.getSiteCode())));
		formparams.add(new BasicNameValuePair("sidCode", StringUtils.defaultString(mediaInfo.getSidCode())));
		formparams.add(new BasicNameValuePair("chainUid", ""+mediaInfo.getChainUid()));
		formparams.add(new BasicNameValuePair("shopUid", ""+user.getShopUid()));
		formparams.add(new BasicNameValuePair("channelUid", "" + mediaInfo.getChannelUid() ));
		formparams.add(new BasicNameValuePair("albumUid", "" + mediaInfo.getAlbumUid() ));
		formparams.add(new BasicNameValuePair("scheduleUid", "" + mediaInfo.getScheduleUid() ));
		formparams.add(new BasicNameValuePair("songUid", "" + mediaInfo.getSongUid() ));
		formparams.add(new BasicNameValuePair("songLid", "" + mediaInfo.getSongLid() ));
		formparams.add(new BasicNameValuePair("cmYn", "" + (mediaInfo.getSongType().equals("CM") ? "Y" : "N" ) ));
		
		String songJson = null;
		try{
			songJson = setPostApiHeader(playLogUrl,formparams,user.getSessionKey());
			logger.info("####### asynch play log result =" + songJson);
		}catch(Exception e){
			logger.error("::play log error::", e);
		}
		
		
		
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
		
		httppost.removeHeaders("User-Agent");
		httppost.removeHeaders("Authorization");		
		httppost.removeHeaders("X-AuthorityKey");
		
		httppost.setEntity(entity);		
		httppost.setHeader("Authorization", "Basic " + Base64.encodeBase64String((basicId +":" + basicPass).getBytes()));
		httppost.setHeader("User-Agent",userAgent );				
		if ( xauth != null){			
			httppost.setHeader("X-AuthorityKey", xauth);
		}
		
		for(NameValuePair tmpparams: formparams){
			logger.info(""+tmpparams.getName() +"="+tmpparams.getValue());			
		}
		
		
		HttpResponse response =  client.execute(httppost);
		
		HttpEntity httpEntity = response.getEntity();
		
		return EntityUtils.toString(httpEntity);
	}

}
