package com.example.dntminesweeper;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static boolean flagSoundOn = true;
    public static boolean flagVibrationOn = true;
    public static boolean gameOverSoundOn = true;
    public static boolean gameOverVibrationOn = true;
    public static boolean winGameSoundOn = true;
    public static boolean winGameVibrationOn = true;
    public static boolean clickvibration = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);


    }

    public static void printthevalues() {
        Log.d("SETTINGS", "printthevalues: flag sound on " + flagSoundOn + " " + "flag vibration on " + flagVibrationOn);
        Log.d("SETTINGS", "printthevalues: gameover sound on " + gameOverSoundOn + " " + "game over vibration on " + gameOverVibrationOn);
        Log.d("SETTINGS", "printthevalues: win game sound on " + winGameSoundOn + " " + "win vibration on " + winGameVibrationOn);
    }
}
