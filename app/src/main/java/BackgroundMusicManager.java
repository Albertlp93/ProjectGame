package com.projectgame.projectgame;

import android.content.Context;
import android.media.MediaPlayer;

public class BackgroundMusicManager {
    private static BackgroundMusicManager instance;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    private BackgroundMusicManager() {
    }

    public static BackgroundMusicManager getInstance() {
        if (instance == null) {
            instance = new BackgroundMusicManager();
        }
        return instance;
    }

    public void startMusic(Context context, int musicResId) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, musicResId);
            mediaPlayer.setLooping(true);
        }
        if (!isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }
}
