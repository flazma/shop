package com.genie.shop.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
							        "file:resources/application-context.xml"
							      })

public class TestDownload {
	
	/*@Autowired
	private ShopDownloadManager shopDownloadManager;*/
	
	@Test
    public void test(){
		
		/*UserVO userVO = new UserVO();
		ChannelVO channelInfo = new ChannelVO();
		Long seq = 320L;
		
		try{
			shopDownloadManager.addQueueMedia(userVO,channelInfo,seq);
		}catch(Exception e){
			e.printStackTrace();
		}*/
		
    }
	
}
