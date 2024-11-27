package com.projectgame.projectgame;

import android.content.Context;
import android.media.MediaPlayer;

public class BackgroundMusicManager {

    private static BackgroundMusicManager instance;
    private MediaPlayer mediaPlayer;

    private BackgroundMusicManager() {
    }

    public static synchronized BackgroundMusicManager getInstance() {
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
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}
