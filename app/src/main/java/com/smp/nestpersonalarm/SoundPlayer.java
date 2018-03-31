package com.smp.nestpersonalarm;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by steve on 3/30/18.
 */

public class SoundPlayer {

    private Context context;

    private MediaPlayer player;

    public SoundPlayer(Context context) {
        this.context = context.getApplicationContext();
    }

    public void start() {
        if (player != null) release();
        try {
            AssetFileDescriptor afd = null;
            afd = context.getAssets().openFd("siren.mp3");
            player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor());
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop() {
        release();
    }
    private void release() {
        if(player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
}
