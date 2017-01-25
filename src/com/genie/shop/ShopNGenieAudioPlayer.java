package com.genie.shop;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.Player;

public class ShopNGenieAudioPlayer extends Player{
	
	/*public ShopNGenieAudioPlayer(File arg0) throws JavaLayerException {
		FileInputStream fis = new FileInputStream(arg0);
		super(fis);
		// TODO Auto-generated constructor stub
	}*/
	
	
	public ShopNGenieAudioPlayer(InputStream arg0, AudioDevice arg1) throws JavaLayerException {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	
	public ShopNGenieAudioPlayer(InputStream stream) throws JavaLayerException {
		super(stream);
		// TODO Auto-generated constructor stub
	}

	public boolean IS_PLAY = false;
	public boolean IS_DOWNPLAY = true;
	
	@Autowired
	public ShopDownloadManager shopDownloadManager;
	
	public void play() throws JavaLayerException {
		super.play();		
	}
	
}
