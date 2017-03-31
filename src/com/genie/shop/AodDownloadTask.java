package com.genie.shop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genie.shop.vo.MediaInfoVO;

public class AodDownloadTask implements Runnable{

	static Logger logger = LoggerFactory.getLogger(AodDownloadTask.class);
	
	public String localDownloadPath;
	public String aodFileType;
	public MediaInfoVO mediaInfo;
	public Queue queue;
	
	
	public AodDownloadTask(MediaInfoVO mediaInfo, String localDownloadPath,String aodFileType, Queue queue){
		this.mediaInfo = mediaInfo;
		this.localDownloadPath = localDownloadPath;
		this.aodFileType = aodFileType;
		this.queue = queue;
		
	}
	

	@Override
	public void run(){
		
		File file = new File(localDownloadPath + mediaInfo.getSongUid() +"." + aodFileType);
		
		HttpGet httpttsGet = null;
		HttpResponse response =null;
		HttpEntity httpEntity =  null;
		InputStream instream = null;
		FileOutputStream output = null;
		HttpClient client = new DefaultHttpClient();
		try{
			
			httpttsGet = new HttpGet(mediaInfo.getCdnPath());
			response = client.execute(httpttsGet);		
			
			httpEntity = response.getEntity();
			instream =  httpEntity.getContent();
				
			logger.info("\t ###########mp3 async disk write="+ file.getPath() +",seq(" + mediaInfo.getSeq() +")");
        
			Long startTime = System.nanoTime();
			
        	output = new FileOutputStream(file);
            int l;
            byte[] tmp = new byte[2048];
            while ( (l = instream.read(tmp)) != -1 ) {
                output.write(tmp, 0, l);
            }
             
            Long endTime = System.nanoTime();
	        
	        Float totalTime = Float.valueOf(TimeUnit.NANOSECONDS.toSeconds(endTime - startTime));
	        
	        Long fileSize = file.length(); //byte로 반환
	        
	        logger.info("\t ###########download time is " + totalTime +" sec, bandwidth is " + (fileSize/1024/1024)/totalTime +"Mbytes/sec");
	        logger.info("\t ###########mp3 async disk write end,seq(" + mediaInfo.getSeq() +")");
	        
        }catch(Exception e){
        	logger.warn("",e);
        }finally {
        	try{output.close();}catch(Exception e){}
        	try{instream.close();}catch(Exception e){}	            
        }
		
		mediaInfo.setFile(file);
		
		queue.add(mediaInfo);
		
		logger.info("\t mp3 Thread queue add end");
	}
	
}
