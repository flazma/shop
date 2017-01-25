package com.genie.shop;

import java.io.File;

import javazoom.jlgui.basicplayer.BasicPlayer;

public class TestJMF {
	//public static void main(String[] args){
		/*try{
			AudioInputStream audioInputStream =AudioSystem.getAudioInputStream(new FileInputStream(new File("D:\\1.mp3")));
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			//clip.
			clip.start( );
	    }
	   catch(Exception ex)
	   {  
		   ex.printStackTrace();
	   }*/
		
		/*try{
			MP4Container container = new MP4Container(new FileInputStream(new File("D:\\1.mp3")));
			container.getMovie();
		}catch(Exception e){
			e.printStackTrace();
		}*/
		
		
		

		
		
	//}
	
	public static void main(String[] args) {
		try{
			BasicPlayer bp = new BasicPlayer();
			bp.open(new File("D:\\1.mp3"));
			//bp.run();
			bp.play();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
