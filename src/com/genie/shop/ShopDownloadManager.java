package com.genie.shop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


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
	
	
	
	@Value("#{config['local.download.path']}")
	public String localDownloadPath = "";
	
	@Value("#{config['emergency.download.path']}")
	public String emergencyDownloadPath = "";
	
	@Value("#{config['emergency.download.count']}")
	private int EMER_MAX_SIZE=0;
	
	@Value("#{config['aod.file.type']}")
	public String aodFileType = "";
	
	@Value("#{config['json.file.type']}")
	public String jsonFileType = "";
	
	
	/**
	 * 정상 재생용 음원 큐
	 */
	public Queue<MediaInfoVO> queue = new LinkedList<MediaInfoVO>();
	
	/**
	 * 비상용 음원 큐
	 */
	public ArrayList<MediaInfoVO> emeAodPool = new ArrayList<MediaInfoVO>();
	
	
	
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

			file = new File(path + File.separator + fileName + "." + aodFileType );
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
					logger.info("media remove in the queue, starttime(" + mediaInfo.getStartTime() +"),endtime("+ mediaInfo.getEndTime() + "),"
							+ "seq("+ mediaInfo.getSeq() +"),songTitle("+ mediaInfo.getSongTitle() +"),songUid(" + mediaInfo.getSongUid() + "),filePath="+mediaInfo.getFilePath()
							+ ",file size=" + mediaInfo.getFile().length()/1024/1024 +"Mbytes");
				}
			}			
		}
		
		logger.info("media info is seq=" + mediaInfo.getSeq() +",starttime(" + mediaInfo.getStartTime() +"),endtime("+ mediaInfo.getEndTime() + "),songTitle("+ mediaInfo.getSongTitle() +
				"),songUid(" + mediaInfo.getSongUid() + "),filePath="+mediaInfo.getFilePath()+ ",file size=" + mediaInfo.getFile().length()/1024/1024+"Mbytes");	
		logger.info("after poll size =" + queue.size());
		
		Iterator itr = queue.iterator();
		while(itr.hasNext()){
			MediaInfoVO tmp = (MediaInfoVO)itr.next();
			logger.info("remain queue info seq("+tmp.getSeq()+"),starttime(" + tmp.getStartTime() +"),endtime("+ tmp.getEndTime() + "),"
					+ "songTitle("+ tmp.getSongTitle() +"),songUid("+tmp.getSongUid()+"),filePath("+tmp.getFilePath()+"),file size=" + tmp.getFile().length()/1024/1024+"Mbytes");
		}
		
		return mediaInfo;
		
	}
	
	
	/**
	 * 임시 음원 저장소에 미디어 저장
	 * @param media
	 */
	public void addEQueue(MediaInfoVO media){
		
		copyToEmergencyQueue(media);
		
		int gap = emeAodPool.size() - EMER_MAX_SIZE;
		
		if (!"CM".equals(media.getSongType())){
						
			if ( gap > 0 ){
				for( int i= 0, j=gap; i<j; i++){
					MediaInfoVO tMedia = emeAodPool.get(0);
					
					String filePath = tMedia.file.getPath();
					filePath = StringUtils.replace(filePath, aodFileType, jsonFileType);
					
					File jsonFile = new File(filePath);
				
					jsonFile.delete();					
					tMedia.file.delete();
					
					logger.info("###emergency queue gap delete seq(" + tMedia.getSeq() +") ####");
					emeAodPool.remove(tMedia);
					
				}				
			}
			
			
						
		}
		
		
	}
	
	/**
	 * 파일이동
	 * @param media
	 */
	public void moveToEmergencyQueue(MediaInfoVO media){
		
		File orgFile = media.getFile();
		
		String org = media.getFilePath();
		String to = localDownloadPath = File.separator + media.getSongUid() + "." + aodFileType;
		
		File toFile = new File(to);
		
		if(orgFile.renameTo(toFile)){		
			logger.info("######move completed file path =" + orgFile.getPath());
		}else{
			logger.info("#####move error file path =" + orgFile.getPath());
		}
		
		media.setFile(orgFile);
		
	}
	
	/**
	 * 파일 복사 후 삭제
	 * @param media
	 */
	public void copyToEmergencyQueue(MediaInfoVO media){
		
		FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel in = null;
        FileChannel out = null;
        
		File orgFile = media.getFile();
        
		String to = emergencyDownloadPath + File.separator + media.getSongUid() + "." + aodFileType;
		String org = orgFile.getPath();
		
		try{
            fis = new FileInputStream(orgFile.getPath());
            fos = new FileOutputStream(to);
            in = fis.getChannel();
            out = fos.getChannel();
 
            //MappedByteBuffer를 사용하여 복사하는 방법
            MappedByteBuffer m = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
            out.write(m);
 
            //메모리를 사용하여 복사하는 방법
            //in.transferTo(0, in.size(), out);
             
        }catch (Exception e) {
            logger.error("",e);
        } finally {
            if(out != null) try{out.close();}catch(Exception e){}
            if(in != null) try{in.close();}catch(Exception e){}
            if(fos != null) try{fos.close();}catch(Exception e){}
            if(fis != null) try{fis.close();}catch(Exception e){}
            orgFile.delete();
        }
		
		
		File toFile = new File(to);
		media.setFile(toFile);
		media.setFilePath(toFile.getPath());
		
		//비상용 음원으로 저장 시 CM인지 여부 확인 필요
		if (!"CM".equals(media.getSongType())){
			emeAodPool.add(media);
			mediaToJson(media);
		}
	}
	
	/**
	 * media object convert to json file
	 * @param obj
	 */
	public void mediaToJson(MediaInfoVO obj) {
		String destPath = emergencyDownloadPath + File.separator + obj.getSongUid() + ".json";
		
		Gson gson = new GsonBuilder().create();
		Writer writer = null;
		
		try{
			logger.info("media object to json:" + destPath);
			writer = new OutputStreamWriter(new FileOutputStream(destPath), "UTF-8");
			gson.toJson(obj, writer);
			writer.close();
		}catch(Exception e){
			logger.error("",e);
		}
		
	}
	
	/**
	 * json file to read covert mediainfo vo object
	 * @param songUid
	 * @return
	 * @throws Exception
	 */
	public MediaInfoVO jsonToMedia(Long songUid) throws Exception {
		String jsonPath = emergencyDownloadPath + File.separator + songUid + ".json";
		
		Gson gson = new GsonBuilder().create();
		Reader reader = null;
		MediaInfoVO media = null;
		try{			
			reader = new InputStreamReader(new FileInputStream(jsonPath), "UTF-8");
			media = gson.fromJson(reader, MediaInfoVO.class);
			reader.close();
		}catch(Exception e){
			throw e;
		}
		
		return media;
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
		
		logger.info("media info is seq=" + mediaInfo.getSeq() +",starttime(" + mediaInfo.getStartTime() +"),endtime("+ mediaInfo.getEndTime() + "),songTitle("+ mediaInfo.getSongTitle() +
				"),songUid(" + mediaInfo.getSongUid() +	"),filePath="+mediaInfo.getFilePath()+ ",file size=" + mediaInfo.getFile().length()/1024/1024+"Mbytes");			
		logger.info("after poll size =" + queue.size());
		
		Iterator itr = queue.iterator();
		while(itr.hasNext()){
			MediaInfoVO tmp = (MediaInfoVO)itr.next();
			logger.info("remain queue info seq("+tmp.getSeq()+"),starttime(" + tmp.getStartTime() +"),endtime("+ tmp.getEndTime() + "),songTitle("+ tmp.getSongTitle() +
					"),songUid("+tmp.getSongUid()+"),filePath("+tmp.getFilePath()+"),file size=" + tmp.getFile().length()/1024/1024+"Mbytes");	
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
		
		/*File file = new File(localDownloadPath + mediaInfo.getSongUid() +"." + + aodType;
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
										
					File file = new File(localDownloadPath + mediaInfo.getSongUid() +"." + aodType);
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
										
					File file = new File(localDownloadPath + mediaInfo.getSongUid() +"." + aodFileType);
					InputStream instream = shopHttpClient.getCDNMedia(mediaInfo.getCdnPath());
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
	
	
	/*public void addQueueMedia(UserVO user, ChannelVO channelInfo, SongVO songVO) throws Exception {
		addQueueMedia(user,channelInfo,seq,null);
	}*/
	
	//@Async
	public void addQueueMedia(UserVO userVO, ChannelVO channelInfo, Long seq) throws Exception {
		
		ArrayList<MediaInfoVO> arrMedia = new ArrayList<MediaInfoVO>();
		
		String ldownloadUrl = StringUtils.replace(downloadUrl, "#channelUid#", "" + channelInfo.getChannelUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#schedulesUid#", "" + channelInfo.getScheduleUid());
		ldownloadUrl = StringUtils.replace(ldownloadUrl, "#shopUid#", "" + userVO.getShopUid());
		
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
						
		String songJson = shopHttpClient.setApiHeader(ldownloadUrl,userVO.getSessionKey());

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
									
				File file = new File(localDownloadPath + mediaInfo.getSongUid() +"." + aodFileType);
				
				Long startTime = System.nanoTime();
				
				InputStream instream = shopHttpClient.getCDNMedia(mediaInfo.getCdnPath());
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
				
		        Long endTime = System.nanoTime();
		        
		        Float totalTime = Float.valueOf(TimeUnit.NANOSECONDS.toSeconds(endTime - startTime));
		        
		        Long fileSize = file.length(); //byte로 반환
		        
		        logger.info("download time is " + totalTime +" sec, bandwidth is " + (fileSize/1024/1024)/totalTime +"Mbytes/sec");
		        
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
	
	
	/**
	 * queue get first seq
	 * @return
	 */
	public Long getQueueFirstSeq(){
		MediaInfoVO media = null;
		Long seq = 0L;
		Iterator itr = queue.iterator();
		while(itr.hasNext()){
			if(media != null){
				media = (MediaInfoVO)itr.next();
				break;
			}
		}
		
		if ( media != null){
			seq = media.getSeq();
		}
		
		return seq;
	}
	
	
	/**
	 * get remain runtime 
	 * @param currentSeq
	 * @return
	 */
	public int getQueueRunningTimeGap(Long currentSeq){
		Long queueSeq = getQueueLastSeq();
		int runningTime = 0;
		MediaInfoVO media = null;
		
		Iterator itr = queue.iterator();
		while(itr.hasNext()){
			media = (MediaInfoVO)itr.next();
			if ( currentSeq <= media.getSeq()){
				try{runningTime += Integer.parseInt(""+media.getRuntime());}catch(Exception e){}
			}
		}
		
		return runningTime;
	}
	
	
	public synchronized void cleanQueue(){
		
		MediaInfoVO media = null;
		Iterator<MediaInfoVO> itr = queue.iterator();
		
		while(itr.hasNext()){
			
			try{
				media = itr.next();
				File file = media.getFile();
				file.delete();
				queue.remove(media);
			}catch(Exception e){				
				itr = queue.iterator();
				continue;
			}
		}
				
	}
	
		

	public Long removeQueueGap(Long currentSeq){
		
		Long last = getQueueLastSeq();
		Long gap = last - currentSeq;
		Long cnt = 0L;
		if ( gap > MAX_SIZE){
			logger.info("clean song queue gap(" + gap +") ,current seq("+ currentSeq +") last seq("+ last +")" ); 
			cleanQueue();			
			return null;
		}else{
		
			logger.info("remove song gap running size(" + queue.size() +")" ); 
			MediaInfoVO media = null;
			
			//check queue exist		
			Iterator<MediaInfoVO> itr = queue.iterator();
			
			while(itr.hasNext()){
				
				try{
					media = itr.next();
				}catch(Exception e){				
					itr = queue.iterator();
					continue;
				}
				
				if ( currentSeq.equals(media.getSeq()) ){
					break;
				}else{
					logger.info("remove song gap is current seq(" + currentSeq +") queue seq(" + media.getSeq() + ")"); 
					queue.remove(media);
					cnt++;
				}
			}
			
			logger.info("remove song gap end size(" + queue.size() +")"); 
		}
		return cnt;
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
												
							File file = new File(localDownloadPath + mediaInfo.getSongUid() +"." + aodType);
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
