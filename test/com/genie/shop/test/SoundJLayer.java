package com.genie.shop.test;

import java.io.FileInputStream;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public class SoundJLayer extends PlaybackListener implements Runnable
{
    private String filePath;
    private AdvancedPlayer player;
    private Thread playerThread;    

    public SoundJLayer(String filePath)//������ ��θ� filePath�� �Է��մϴ�.
    {
        this.filePath = filePath;//filePath�� ���� mp3�� ��� ������ �ʱ�ȭ.
    }

    public void play()
    {
        try
        {
            String urlAsString = this.filePath;//
            this.player = new AdvancedPlayer(new FileInputStream(urlAsString));

            this.player.setPlayBackListener(this);

            this.playerThread = new Thread(this, "AudioPlayerThread");

            this.playerThread.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    // PlaybackListener members

    public void playbackStarted(PlaybackEvent playbackEvent)
    {
        System.out.println("playbackStarted");
    }

    public void playbackFinished(PlaybackEvent playbackEvent)
    {
        System.out.println("playbackEnded");
    }    

    // Runnable members

    public void run()
    {
        try
        {
            this.player.play();
        }
        catch (javazoom.jl.decoder.JavaLayerException ex)
        {
            ex.printStackTrace();
        }

    }
}