package com.example.dntminesweeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dntminesweeper.Music.MusicService;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    public static boolean isSoundOn = true;
    public static boolean isVibrationOn = true;
    public static boolean isMusicOn = true;
    private ImageButton mBtnSound, mBtnMusic, mBtnVibration;
    private SharedPreferences userSettings;
    private MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        componentsInit();
    }

    private void componentsInit() {
        mBtnSound = findViewById(R.id.btn_SoundControl);
        mBtnSound.setOnClickListener(this);
        mBtnMusic = findViewById(R.id.btn_MusicControl);
        mBtnMusic.setOnClickListener(this);
        mBtnVibration = findViewById(R.id.btn_VibrateControl);
        mBtnVibration.setOnClickListener(this);
        findViewById(R.id.btn_back_settings).setOnClickListener(this);
        userSettings = getApplicationContext().getSharedPreferences("UserInfo", 0);
        isMusicOn = userSettings.getBoolean("isMusicOn", false);
        isSoundOn = userSettings.getBoolean("isSoundOn", false);
        isVibrationOn = userSettings.getBoolean("isVibrationOn", false);
        musicService = new MusicService(this);
        MusicService.startPlayingMusic(musicService, R.raw.kiss_the_rain);
        musicControl();
        soundControl();
        vibrateControl();
    }

    @Override
    public void onClick(View view) {
        SharedPreferences.Editor editor = userSettings.edit();
        switch (view.getId()) {
            case R.id.btn_MusicControl:
                isMusicOn = !isMusicOn;
                musicControl();
                editor.putBoolean("isMusicOn", isMusicOn);
                break;
            case R.id.btn_SoundControl:
                isSoundOn = !isSoundOn;
                soundControl();
                editor.putBoolean("isSoundOn", isSoundOn);
                break;
            case R.id.btn_VibrateControl:
                isVibrationOn = !isVibrationOn;
                vibrateControl();
                editor.putBoolean("isVibrationOn", isVibrationOn);
                break;
            case R.id.btn_back_settings:
                onBackPressed();
                break;
            default:
                Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
                break;
        }
        editor.commit();
    }

    public void musicControl() {
        int id;
        if (isMusicOn) {
            id = R.drawable.ic_music_note;
            musicService.resumeBgMusic();
        } else {
            id = R.drawable.ic_music_off;
            musicService.pauseBgMusic();
        }
        mBtnMusic.setImageDrawable(getResources().getDrawable(id));
    }

    public void soundControl() {
        int id = isSoundOn ? R.drawable.ic_sound_on : R.drawable.ic_sound_off;
        mBtnSound.setImageDrawable(getResources().getDrawable(id));
    }

    public void vibrateControl() {
        int id = isVibrationOn ? R.drawable.ic_vibration_on : R.drawable.ic_vibration_off;
        mBtnVibration.setImageDrawable(getResources().getDrawable(id));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SettingsActivity.isMusicOn) {
            musicService.resumeBgMusic();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        musicService.pauseBgMusic();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        musicService.stop();
    }
}
