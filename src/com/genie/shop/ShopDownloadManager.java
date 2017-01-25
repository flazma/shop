package com.genie.shop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import com.genie.shop.vo.ChannelVO;
import com.genie.shop.vo.MediaInfoVO;
import com.genie.shop.vo.UserVO;


/**
 *
 * @author flazma
 *
 */
@Component
public class ShopDownloadManager {
	static Logger logger = Logger.getLogger(ShopDownloadManager.class);
	
	@Value("#{config['download.url']}")
	public String downloadUrl = "";
	
	@Value("#{config['max.download.count']}")
	private int MAX_SIZE=0;
	
	public Queue<MediaInfoVO> queue = new LinkedList<MediaInfoVO>();
	
	@Autowired
	public ShopHttpClient shopHttpClient;
	
	
	public static ShopDownloadManager instance;
	
	public synchronized static ShopDownloadManager getInstance(){
		
			if ( instance == null){
				instance = new ShopDownloadManager();
				
			}

			return instance;
	}
	
	/**
	 * queue�� file�� �����´�. ������ �Ŀ��� �����
	 * @return
	 */
	public synchronized MediaInfoVO getAudioFile(){
		
		//queue 
		if ( queue.size() <= MAX_SIZE){
			MediaInfoVO media = queue.peek();
			String fileName = media.file.getName();
			media.file.delete();
		}
		
		return queue.poll();
	}
	
	/**
	 * queue
	 * @param file
	 * @return
	 */
	public boolean putAudioFile(MediaInfoVO file){		
		return queue.offer(file);				
	}
	
	/**
	 * Ư�� ��ο� mp3������ ����
	 * ��δ� ../../cache/xxx.mp3
	 * @param contents
	 * @param fileName
	 * @throws Exception
	 */
	public void writeFile(byte[] contents,String path,String fileName) throws Exception {
		FileOutputStream fop = null;
		File file;
		//String content = "This is the text content";

		try {

			file = new File(path + fileName + ".mp3");
			fop = new FileOutputStream(file);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// get the content in bytes
			//byte[] contentInBytes = contents.getBytes();

			fop.write(contents);
			fop.flush();
			fop.close();


		} catch (Exception e) {
			throw e;
		} finally {
			if (fop != null) {	
				try{fop.close();}catch(Exception e){ throw e;}
			}
		}
	}
	
	public MediaInfoVO poll(){
		logger.info("before poll size =" + queue.size());
		MediaInfoVO mediaInfo = queue.poll();
		logger.info("media info is seq=" + mediaInfo.getSeq() +",songUid=" + mediaInfo.getSongUid() + ",filePath="+mediaInfo.getFilePath());		
		logger.info("after poll size =" + queue.size());
		return mediaInfo;
		
	}
	
	public boolean removeFile(String filePath){
		File file = new File(filePath);
		return file.delete();
	}
	
	/**
	 * 
	 */
	
	public void run(){
		
		//		
		
		/*File file = new File("./cache/"+ mediaInfo.getSongUid() +".mp3");
		InputStream instream = getMedia(mediaInfo.filePath);
		FileOutputStream output = new FileOutputStream(file);

        try {
            int l;
            byte[] tmp = new byte[2048];
            while ( (l = instream.read(tmp)) != -1 ) {
                output.write(tmp, 0, l);
            }
        } finally {
        	try{output.close();}catch(Exception e){}
        	try{instream.close();}catch(Exception e){}	            
        }*/
		
		
	}
	
	@Async
	public ArrayList<MediaInfoVO> addQueueMedia(UserVO user, ChannelVO channelInfo, Long seq) throws Exception {
		
		ArrayList<MediaInfoVO> arrMedia = new ArrayList<MediaInfoVO>();
		
		String ldownloadUrl = StringUtils.replace(downloadUrl, "#channelUid#", "" + channelInfo.getChannelUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#schedulesUid#", "" + channelInfo.getScheduleUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#shopUid#", "" + user.getShopUid());
		
		String songList = "";
		
		
		int gap = MAX_SIZE - queue.size();
	
		
		logger.info("\tdownload gap is MAX_SIZE("+MAX_SIZE+")-queue.size("+queue.size()+") = "+gap);
		
		Iterator itr = queue.iterator();
		
		//queue 다운로드 문자열 생성		
		for ( Long i=seq, j = i+gap; i<j; i++){
			songList += i + ",";
		}
	
		songList = songList.substring(0, songList.length()-1);
		
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#songList#", songList);
		
		logger.info("is download start!!!"+ ldownloadUrl);
						
			String songJson = shopHttpClient.setApiHeader(ldownloadUrl,user.getSessionKey());
						
			JSONParser par = new JSONParser();
			
			JSONObject json = (JSONObject)par.parse(songJson);
			JSONObject jsonData = (JSONObject)json.get("data");
			String rtCode = (String)jsonData.get("rtCode");
			
			if ("0".equals(rtCode)){
			
				JSONArray jsonResult = (JSONArray)((JSONObject)jsonData.get("params")).get("dnList");
				
				for(int i=0,j=jsonResult.size(); i < j ; i++){
					JSONObject songObject = (JSONObject)jsonResult.get(i);
					MediaInfoVO mediaInfo = new MediaInfoVO();
					
					
					mediaInfo.setSeq((Long)songObject.get("seq"));
					mediaInfo.setSongUid((Long)songObject.get("songUid"));
					mediaInfo.setCdnPath(StringUtils.trimToEmpty((String)songObject.get("filePath")));
					
					logger.info("\tdownload info=mediaInfo.getSeq()"+ mediaInfo.getSeq());
					logger.info("\tdownload info=mediaInfo.getSongUid()"+ mediaInfo.getSongUid());
					logger.info("\tdownload info=mediaInfo.getFilePath()"+ mediaInfo.getFilePath());
										
					File file = new File("./cache/"+ mediaInfo.getSongUid() +".mp3");
					InputStream instream = shopHttpClient.getMedia(mediaInfo.getCdnPath());
					FileOutputStream output = new FileOutputStream(file);
					
					mediaInfo.setFilePath(file.getPath());
					
					logger.info("\tdownload mp3 disk write="+ file.getPath());
		
			        try {
			            int l;
			            byte[] tmp = new byte[2048];
			            while ( (l = instream.read(tmp)) != -1 ) {
			                output.write(tmp, 0, l);
			            }
			        } finally {
			        	try{output.close();}catch(Exception e){}
			        	try{instream.close();}catch(Exception e){}	            
			        }
					
			        mediaInfo.setFile(file);
					
					arrMedia.add(mediaInfo);
					queue.add(mediaInfo);
				}
				
				logger.info("\tis downloaded end!!!");
			}
			
		
		
		
		return arrMedia;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		ShopDownloadManager.logger = logger;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public int getMAX_SIZE() {
		return MAX_SIZE;
	}

	public void setMAX_SIZE(int mAX_SIZE) {
		MAX_SIZE = mAX_SIZE;
	}

	public Queue<MediaInfoVO> getQueue() {
		return queue;
	}

	public void setQueue(Queue<MediaInfoVO> queue) {
		this.queue = queue;
	}

	public ShopHttpClient getShopHttpClient() {
		return shopHttpClient;
	}

	public void setShopHttpClient(ShopHttpClient shopHttpClient) {
		this.shopHttpClient = shopHttpClient;
	}

	public static void setInstance(ShopDownloadManager instance) {
		ShopDownloadManager.instance = instance;
	}


	
	
	
	
}
