package com.genie.shop;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.genie.shop.vo.MediaInfoVO;
import com.genie.shop.vo.UserVO;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

@Component
public class ShopAudioPlayerListener implements BasicPlayerListener{
	
	static Logger logger = LoggerFactory.getLogger(ShopAudioPlayerListener.class);
	
	@Autowired
	public ShopNGenieService sgc;
	
	@Autowired
	private ShopHttpClient shopHttpClient;
	
	private MediaInfoVO media;
	
	public boolean IS_PLAY = false;
	public boolean IS_DOWNPLAY = true;
	
	public boolean IS_SEND_LOG = false;
	
	
	public MediaInfoVO getMedia() {
		return media;
	}

	public void setMedia(MediaInfoVO media) {
		this.media = media;
	}

	public void opened(Object stream, Map properties){
		display("opened : "+properties.toString());	
	}

	    /**
	     * Progress callback while playing.
	     * 
	     * This method is called severals time per seconds while playing.
	     * properties map includes audio format features such as
	     * instant bitrate, microseconds position, current frame number, ... 
	     * 
	     * @param bytesread from encoded stream.
	     * @param microseconds elapsed (<b>reseted after a seek !</b>).
	     * @param pcmdata PCM samples.
	     * @param properties audio stream parameters.
	     */
	    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties){
	    	//display("progress : "+properties.toString());
	    	
	    	Long position = (Long)properties.get("mp3.position.microseconds");
	    	
	    	position = position/1000/1000;
	    	
	    	if ( position > 5 ){
	    	//if ( position > 60 ){
	    		if ( IS_SEND_LOG == false){
	    			IS_SEND_LOG = true;
	    			logger.info("#####is play log time:" + position + ":(Long)properties.get(\"mp3.position.microseconds\"):" + (Long)properties.get("mp3.position.microseconds")+":");
	    			
	    			UserVO userVO = sgc.getUserAccountInfo();
	    			try{
	    				shopHttpClient.sendPlayLog(userVO,media);
	    				
	    			}catch(Exception e){
	    				logger.info(e.toString());
	    			}
	    			
	    			Iterator itr = properties.keySet().iterator();
	    	    	while(itr.hasNext()){
	    	    		String key = (String)itr.next();
	    	    		logger.info("progress:is:key="+key+",value="+properties.get(key));
	    	    	}
	    			
	    		}
	    	}	    	
	    }

	    /**
	     * Notification callback for basicplayer events such as opened, eom ...
	     *  
	     * @param event
	     */
	    public void stateUpdated(BasicPlayerEvent event){
	    	// Notification of BasicPlayer states (opened, playing, end of media, ...)
	    	
			if (event.getCode()==BasicPlayerEvent.STOPPED )
			{
				synchronized (sgc) {					
					sgc.notifyAll();
				}			
			}
	    }

	    /**
	     * A handle to the BasicPlayer, plugins may control the player through
	     * the controller (play, stop, ...)
	     * @param controller : a handle to the player
	     */
	    public void setController(BasicController controller){
	    	
	    }
	    
	    public void display(String msg)
		{
			logger.info(msg);
		}
}
