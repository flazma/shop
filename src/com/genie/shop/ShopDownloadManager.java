package com.genie.shop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.genie.shop.vo.ChannelVO;
import com.genie.shop.vo.MediaInfoVO;
import com.genie.shop.vo.SongVO;
import com.genie.shop.vo.UserVO;


/**
 *
 * @author flazma
 *
 */
@Component
public class ShopDownloadManager {
	static Logger logger = LoggerFactory.getLogger(ShopDownloadManager.class);
	
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
	 * queue to peek file(remove)
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
	 * mp3 file to local disk write
	 * location = ./cache/xxx.mp3
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
	
	
	public MediaInfoVO newPoll(){
		
		Long currentTime = Long.parseLong(new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date()));
		logger.info("before poll size =" + queue.size());
		MediaInfoVO mediaInfo = queue.poll();
		Long endTime = Long.parseLong(mediaInfo.getEndTime());
		
		//현재 시간보다 종료 시간이 작으면 해당 음원을 queue에서 삭제한다.
		if (currentTime > endTime ){
			Iterator itr = queue.iterator();
			while(itr.hasNext()){
				mediaInfo = (MediaInfoVO)itr.next();
				endTime = Long.parseLong(mediaInfo.getEndTime());
				if(currentTime > endTime){
					queue.remove(mediaInfo);
					logger.info("media remove in the queue, starttime(" + mediaInfo.getStartTime() +"),endtime("+ mediaInfo.getEndTime() + "),seq("+ mediaInfo.getSeq() +"),songTitle("+ mediaInfo.getSongTitle() +"),songUid(" + mediaInfo.getSongUid() + "),filePath="+mediaInfo.getFilePath());
				}
			}			
		}
		
		logger.info("media info is seq=" + mediaInfo.getSeq() +",starttime(" + mediaInfo.getStartTime() +"),endtime("+ mediaInfo.getEndTime() + "),songTitle("+ mediaInfo.getSongTitle() +"),songUid(" + mediaInfo.getSongUid() + "),filePath="+mediaInfo.getFilePath());		
		logger.info("after poll size =" + queue.size());
		
		Iterator itr = queue.iterator();
		while(itr.hasNext()){
			MediaInfoVO tmp = (MediaInfoVO)itr.next();
			logger.info("remain queue info seq("+tmp.getSeq()+"),starttime(" + tmp.getStartTime() +"),endtime("+ tmp.getEndTime() + "),songTitle("+ tmp.getSongTitle() +"),songUid("+tmp.getSongUid()+"),filePath("+tmp.filePath+")");
		}
		
		return mediaInfo;
		
	}
	
	
	//queue중에 현재 시간에 맞지 않는 음원은 삭제 로직 추가
	public MediaInfoVO poll(){
		
		//Long currentTime = Long.parseLong(new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date()));
		logger.info("before poll size =" + queue.size());
		MediaInfoVO mediaInfo = queue.poll();
		//Long endTime = Long.parseLong(mediaInfo.getEndTime());
		
		//현재 시간보다 종료 시간이 작으면
		/*if (currentTime > endTime ){
			Iterator itr = queue.iterator();
			while(itr.hasNext()){
				mediaInfo = (MediaInfoVO)itr.next();
				endTime = Long.parseLong(mediaInfo.getEndTime());
				if(currentTime > endTime){
					queue.remove(mediaInfo);
					logger.info("media remove in the queue, starttime(" + mediaInfo.getStartTime() +"),endtime("+ mediaInfo.getEndTime() + "),seq("+ mediaInfo.getSeq() +"),songTitle("+ mediaInfo.getSongTitle() +"),songUid(" + mediaInfo.getSongUid() + "),filePath="+mediaInfo.getFilePath());
				}
			}			
		}*/
		
		logger.info("media info is seq=" + mediaInfo.getSeq() +",starttime(" + mediaInfo.getStartTime() +"),endtime("+ mediaInfo.getEndTime() + "),songTitle("+ mediaInfo.getSongTitle() +"),songUid(" + mediaInfo.getSongUid() + "),filePath="+mediaInfo.getFilePath());		
		logger.info("after poll size =" + queue.size());
		
		Iterator itr = queue.iterator();
		while(itr.hasNext()){
			MediaInfoVO tmp = (MediaInfoVO)itr.next();
			logger.info("remain queue info seq("+tmp.getSeq()+"),starttime(" + tmp.getStartTime() +"),endtime("+ tmp.getEndTime() + "),songTitle("+ tmp.getSongTitle() +"),songUid("+tmp.getSongUid()+"),filePath("+tmp.filePath+")");
		}
		
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
	
	/*@Async
	public void asynchDown(UserVO user, ChannelVO channelInfo, Long seq) throws Exception {
		
		AsynchDownloadLog adl = new AsynchDownloadLog(downloadUrl,queue,user, channelInfo, seq, shopHttpClient);
		adl.run();		
	}*/
	
	
	
	/*@Async
	public Future<Queue<MediaInfoVO>> asynchDown(UserVO user, ChannelVO channelInfo, Long seq) throws Exception {
		
		ArrayList<MediaInfoVO> arrMedia = new ArrayList<MediaInfoVO>();

		String ldownloadUrl = StringUtils.replace(downloadUrl, "#channelUid#", "" + channelInfo.getChannelUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#schedulesUid#", "" + channelInfo.getScheduleUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#shopUid#", "" + user.getShopUid());
		
		String songList = "";
		
		
		int gap = MAX_SIZE - queue.size();
	
		
		logger.info("\tdownload gap is MAX_SIZE("+MAX_SIZE+")-queue.size("+queue.size()+") = "+gap);
		
		//queue에서 마지막 seq를 참고하여 seq 에서 gap까지를 다운받아야 함
		//만약 마지막 큐의 값이 0이 아니면 마지막 seq의 값을 참고하여 다운로드 리스트 작성
		if ( getQueueLastSeq() != 0L){
			seq = getQueueLastSeq()+1;
		}
		
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
					
					logger.info("\tdownload info=mediaInfo.getSeq()"+ mediaInfo.getSeq() +",getSongUid()"+ mediaInfo.getSongUid()+",getFilePath()"+ mediaInfo.getFilePath());
										
					File file = new File("./cache/"+ mediaInfo.getSongUid() +".mp3");
					InputStream instream = shopHttpClient.getMedia(mediaInfo.getCdnPath());
					FileOutputStream output = new FileOutputStream(file);
					
					mediaInfo.setFilePath(file.getPath());
					
					logger.info("\t mp3 disk write="+ file.getPath());
		
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
		return queue;
	}
	*/
	
	
	
	//곡 저장
	public synchronized void addMedia(UserVO user, ChannelVO channelInfo,Long seq, ArrayList<SongVO> arrSongVO) throws Exception {
		
		ArrayList<MediaInfoVO> arrMedia = new ArrayList<MediaInfoVO>();
		
		String ldownloadUrl = StringUtils.replace(downloadUrl, "#channelUid#", "" + channelInfo.getChannelUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#schedulesUid#", "" + channelInfo.getScheduleUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#shopUid#", "" + user.getShopUid());
		
		String songList = "";
		
		
		int gap = MAX_SIZE - queue.size();
		
		logger.info("\tdownload gap is MAX_SIZE("+MAX_SIZE+")-queue.size("+queue.size()+") = "+gap);
		
		//queue에서 마지막 seq를 참고하여 seq 에서 gap까지를 다운받아야 함
		//만약 마지막 큐의 값이 0이 아니면 마지막 seq의 값을 참고하여 다운로드 리스트 작성
		if ( getQueueLastSeq() != 0L){
			seq = getQueueLastSeq()+1;
		}
		
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
					
					
					Long mediaSeq = (Long)songObject.get("seq");
					
					Iterator itr = arrSongVO.iterator();
					while(itr.hasNext()){
						SongVO songVO = (SongVO)itr.next();						
						if ( mediaSeq.equals(songVO.getSeq())){
							
							mediaInfo.setStartRunTime(songVO.getStartRunTime());
							mediaInfo.setEndRunTime(songVO.getEndRunTime());
							mediaInfo.setStartTime(songVO.getStartTime());
							mediaInfo.setEndTime(songVO.getEndTime());
							mediaInfo.setSongTitle(songVO.getSongTitle());
							mediaInfo.setArtistName(songVO.getArtistName());
							mediaInfo.setSongType(songVO.getSongType());
							arrSongVO.remove(songVO);
							break;
						}						
					}
					
					mediaInfo.setSeq(mediaSeq);
					mediaInfo.setSongUid((Long)songObject.get("songUid"));
					mediaInfo.setCdnPath(StringUtils.trimToEmpty((String)songObject.get("filePath")));
					
					logger.info("\tdownload info=mediaInfo.getSeq()"+ mediaInfo.getSeq() +",startTime()"+ mediaInfo.getStartTime() +",endTime()"+mediaInfo.getEndTime()+",getSongUid()"+ mediaInfo.getSongUid()+",getFilePath()"+ mediaInfo.getFilePath());
										
					File file = new File("./cache/"+ mediaInfo.getSongUid() +".mp3");
					InputStream instream = shopHttpClient.getMedia(mediaInfo.getCdnPath());
					FileOutputStream output = new FileOutputStream(file);
					
					mediaInfo.setFilePath(file.getPath());
					
					logger.info("\t mp3 disk write="+ file.getPath());
		
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
					
					
					 //ArrayList<SongVO> songVO
					
					
				}
				
				logger.info("\tis downloaded end!!!");
			}
			
		
		//return arrMedia;
	}
	
	
	
	
	//@Async
	public void addQueueMedia(UserVO user, ChannelVO channelInfo, Long seq) throws Exception {
		//public ArrayList<MediaInfoVO> addQueueMedia(UserVO user, ChannelVO channelInfo, Long seq) throws Exception {
		
		ArrayList<MediaInfoVO> arrMedia = new ArrayList<MediaInfoVO>();
		
		String ldownloadUrl = StringUtils.replace(downloadUrl, "#channelUid#", "" + channelInfo.getChannelUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#schedulesUid#", "" + channelInfo.getScheduleUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#shopUid#", "" + user.getShopUid());
		
		String songList = "";
		
		
		int gap = MAX_SIZE - queue.size();
	
		
		logger.info("\tdownload gap is MAX_SIZE("+MAX_SIZE+")-queue.size("+queue.size()+") = "+gap);
		
		//queue에서 마지막 seq를 참고하여 seq 에서 gap까지를 다운받아야 함
		//만약 마지막 큐의 값이 0이 아니면 마지막 seq의 값을 참고하여 다운로드 리스트 작성
		if ( getQueueLastSeq() != 0L){
			seq = getQueueLastSeq()+1;
		}
		
		//queue 다운로드 문자열 생성
		for ( Long i=seq, j = i+gap; i<j; i++){
			songList += i + ",";
		}
	
		songList = songList.substring(0, songList.length()-1);
		
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#songList#", songList);
		
		logger.info("is download start!!!"+ ldownloadUrl);
						
		String songJson = shopHttpClient.setApiHeader(ldownloadUrl,user.getSessionKey());

		logger.info("is download json :"+ songJson+":");
		
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
				
				logger.info("\tdownload info=mediaInfo.getSeq()"+ mediaInfo.getSeq() +",getSongUid()"+ mediaInfo.getSongUid()+",getCdnPath()"+ mediaInfo.getCdnPath());
									
				File file = new File("./cache/"+ mediaInfo.getSongUid() +".mp3");
				InputStream instream = shopHttpClient.getMedia(mediaInfo.getCdnPath());
				FileOutputStream output = new FileOutputStream(file);
				
				mediaInfo.setFilePath(file.getPath());
				
				logger.info("\t mp3 disk write="+ file.getPath());
	
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
			
		
		//return arrMedia;
	}

	/**
	 * queue get last seq
	 * @return
	 */
	public Long getQueueLastSeq(){
		MediaInfoVO media = null;
		Long seq = 0L;
		Iterator itr = queue.iterator();
		while(itr.hasNext()){
			media = (MediaInfoVO)itr.next();
		}
		
		if ( media != null){
			seq = media.getSeq();
		}
		
		return seq;
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

	
	/*class AsynchDownloadLog implements Runnable{
		
		public String downloadUrl;
		public Queue<MediaInfoVO> queue;
		public UserVO user;
		public ChannelVO channelInfo;
		public Long seq;
		public ShopHttpClient shopHttpClient;

		public AsynchDownloadLog(String downloadUrl, Queue<MediaInfoVO> queue, UserVO user, ChannelVO channelInfo, Long seq, ShopHttpClient shopHttpClient){
			this.downloadUrl = downloadUrl;
			this.queue = queue;
			this.user =  user;
			this.channelInfo = channelInfo;
			this.seq = seq;
			this.shopHttpClient = shopHttpClient;
		}
		
		
		@Override
		public void run() {
				
			try{
				ArrayList<MediaInfoVO> arrMedia = new ArrayList<MediaInfoVO>();
				
				String ldownloadUrl = StringUtils.replace(downloadUrl, "#channelUid#", "" + channelInfo.getChannelUid());
				ldownloadUrl = StringUtils.replace(ldownloadUrl, "#schedulesUid#", "" + channelInfo.getScheduleUid());
				ldownloadUrl = StringUtils.replace(ldownloadUrl, "#shopUid#", "" + user.getShopUid());
				
				String songList = "";
				
				
				int gap = MAX_SIZE - queue.size();
			
				
				logger.info("\tdownload gap is MAX_SIZE("+MAX_SIZE+")-queue.size("+queue.size()+") = "+gap);
				
				//queue에서 마지막 seq를 참고하여 seq 에서 gap까지를 다운받아야 함
				//만약 마지막 큐의 값이 0이 아니면 마지막 seq의 값을 참고하여 다운로드 리스트 작성
				if ( getQueueLastSeq() != 0L){
					seq = getQueueLastSeq()+1;
				}
				
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
							
							logger.info("\tdownload info=mediaInfo.getSeq()"+ mediaInfo.getSeq() +",getSongUid()"+ mediaInfo.getSongUid()+",getFilePath()"+ mediaInfo.getFilePath());
												
							File file = new File("./cache/"+ mediaInfo.getSongUid() +".mp3");
							InputStream instream = shopHttpClient.getMedia(mediaInfo.getCdnPath());
							FileOutputStream output = new FileOutputStream(file);
							
							mediaInfo.setFilePath(file.getPath());
							
							logger.info("\t mp3 disk write="+ file.getPath());
				
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
					
			}catch(Exception e){
				//return arrMedia;
				e.printStackTrace();
			}
			
			}
			
			
	}*/
	
	
}
