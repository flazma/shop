package com.genie.shop.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

public class DiskTetst {
	public static void main(String[] args){
		File file = new File("D:\\1.mp3");
		
		System.out.println(file.length()/1024);
		
		
		FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel in = null;
        FileChannel out = null;
        
		File orgFile = new File("D:\\1.mp3");
        
		String to = "D:\\1" + System.currentTimeMillis() + ".mp3";
		String org = "D:\\1.mp3";
		
		try{
			Long startTime = System.nanoTime();
			
            fis = new FileInputStream(org);
            fos = new FileOutputStream(to);
            in = fis.getChannel();
            out = fos.getChannel();
 
            //MappedByteBuffer를 사용하여 복사하는 방법
            MappedByteBuffer m = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
            out.write(m);
            Thread.sleep(3*1000);
            
            Long endTime = System.nanoTime();
	        
	        Float totalTime = Float.valueOf(TimeUnit.NANOSECONDS.toSeconds(endTime - startTime));
	        
	        Long fileSize = file.length();
	        
	        System.out.println("download time is " + totalTime);
	        System.out.println("bandwidth is " + (fileSize/1024/1024)/totalTime +"Mbytes/sec");
 
            //메모리를 사용하여 복사하는 방법
            //in.transferTo(0, in.size(), out);
             
        }catch (Exception e) {
           e.printStackTrace();
        } finally {
            if(out != null) try{out.close();}catch(Exception e){}
            if(in != null) try{in.close();}catch(Exception e){}
            if(fos != null) try{fos.close();}catch(Exception e){}
            if(fis != null) try{fis.close();}catch(Exception e){}            
        }
		
	}

}
