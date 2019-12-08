package com.example.dntminesweeper.Music;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.example.dntminesweeper.R;
import com.example.dntminesweeper.SettingsActivity;

import java.util.ArrayList;
import java.util.Random;


public class MusicService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String STATE_MUSIC = "MUSIC";
    public static final int PLAYER_IDLE = -1;
    public static final int PLAYER_PLAY = 1;
    public static final int PLAYER_PAUSE = 2;
    private int stateS, stateBg;

    private MediaPlayer mediaPlayer;
    private MediaPlayer bgMusic;
    private SharedPreferences preferences;
    private Context c;
    private boolean stateMusic;
    private boolean stateSound;
    private ArrayList<int[]> arrIdQues;

    public MusicService(Context context) {
        this.c = context;
        getPreferenceSetting();
    }

    public void setStateMusic(boolean stateMusic) {
        this.stateMusic = stateMusic;
    }

    public boolean getStateMusic() {
        return stateMusic;
    }


    private void getPreferenceSetting() {
        preferences = c.getSharedPreferences("UserInfo", 0);
        setStateMusic(SettingsActivity.isMusicOn);
    }

    public void setting(boolean stateMusic, boolean stateSound) {
        setStateMusic(stateMusic);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(STATE_MUSIC, stateMusic);
        editor.apply();
    }

    public void playBgMusic(int idSound) {
        if (!getStateMusic()) {
            return;
        }
        stopBgMusic();
        stateBg = PLAYER_IDLE;
        bgMusic = new MediaPlayer();
        bgMusic = MediaPlayer.create(c, idSound);
        bgMusic.setVolume(50, 50);
        bgMusic.setAudioStreamType(AudioManager.STREAM_MUSIC);
        bgMusic.setLooping(true);
        bgMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                if (stateBg == PLAYER_IDLE) {
                    mp.start();
                    stateBg = PLAYER_PLAY;
                }
            }
        });

    }

    public void pauseBgMusic() {
        if (stateBg == PLAYER_PLAY) {
            bgMusic.pause();
            stateBg = PLAYER_PAUSE;
        }
    }

    public void resumeBgMusic() {
        if (stateBg == PLAYER_PAUSE && stateS != PLAYER_PLAY) {
            bgMusic.start();
            stateBg = PLAYER_PLAY;
        }
    }

    public void stopBgMusic() {
        if (stateBg == PLAYER_PLAY || stateBg == PLAYER_PAUSE) {
            bgMusic.release();
            bgMusic = null;
            stateBg = PLAYER_IDLE;
        }
    }

    public void stop() {
        if (stateS == PLAYER_PLAY || stateS == PLAYER_PAUSE) {
            mediaPlayer.release();
            mediaPlayer = null;
            stateS = PLAYER_IDLE;
        }
        pauseBgMusic();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (stateS == PLAYER_IDLE) {
            mp.start();
            stateS = PLAYER_PLAY;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer.release();
        mediaPlayer = null;
        stateS = PLAYER_IDLE;
    }

    public static void startPlayingMusic(final MusicService musicService, final int songId) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                musicService.playBgMusic(songId);
                if (!SettingsActivity.isSoundOn) {
                    musicService.pauseBgMusic();
                }
            }
        }, 2500);
    }
}
