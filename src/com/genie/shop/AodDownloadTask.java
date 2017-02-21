package com.genie.shop;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

import com.genie.shop.vo.MediaInfoVO;

public class AodDownloadTask implements Callable<Queue>{

	public Queue<MediaInfoVO> queue = null;
	
	public AodDownloadTask(Queue<MediaInfoVO> queue){
		this.queue = queue;
	}
	
	public Queue call() throws Exception {
		return null;
	}
	
	
	
	
	/*public void run() {

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}*/
}
