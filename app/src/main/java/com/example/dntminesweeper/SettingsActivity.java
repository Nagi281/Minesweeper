package com.example.dntminesweeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    public static boolean isSoundOn = true;
    public static boolean isVibrationOn = true;
    public static boolean isMusicOn = true;
    private ImageButton mBtnSound, mBtnMusic, mBtnVibration;
    private SharedPreferences userSettings;

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
        musicControl(!isMusicOn);
        soundControl(!isSoundOn);
        vibrateControl(!isVibrationOn);
        userSettings = getApplicationContext().getSharedPreferences("UserInfo", 0);
    }

    @Override
    public void onClick(View view) {
        SharedPreferences.Editor editor = userSettings.edit();
        switch (view.getId()) {
            case R.id.btn_MusicControl:
                soundControl(isMusicOn);
                isMusicOn = !isMusicOn;
                editor.putBoolean("isMusicOn", isMusicOn);
                break;
            case R.id.btn_SoundControl:
                musicControl(isSoundOn);
                isSoundOn = !isSoundOn;
                editor.putBoolean("isSoundOn", isSoundOn);
                break;
            case R.id.btn_VibrateControl:
                vibrateControl(isVibrationOn);
                isVibrationOn = !isVibrationOn;
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

    public void soundControl(boolean sound) {
        if (sound) {
            mBtnMusic.setImageDrawable(getResources().getDrawable(R.drawable.ic_music_off));
        } else {
            mBtnMusic.setImageDrawable(getResources().getDrawable(R.drawable.ic_music_note));
        }
    }

    public void musicControl(boolean music) {
        if (music) {
            mBtnSound.setImageDrawable(getResources().getDrawable(R.drawable.ic_sound_off));
        } else {
            mBtnSound.setImageDrawable(getResources().getDrawable(R.drawable.ic_sound_on));
        }
    }

    public void vibrateControl(boolean vibrate) {
        if (vibrate) {
            mBtnVibration.setImageDrawable(getResources().getDrawable(R.drawable.ic_vibration_off));
        } else {
            mBtnVibration.setImageDrawable(getResources().getDrawable(R.drawable.ic_vibration_on));
        }
    }

}
