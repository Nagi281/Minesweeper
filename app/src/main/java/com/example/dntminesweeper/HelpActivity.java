package com.example.dntminesweeper;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dntminesweeper.Music.MusicService;

public class HelpActivity extends AppCompatActivity {
    private ImageView mIvBackground, mIvBack;
    private ImageButton mIBtnBack, mIBtnNext;
    private TextView mEdtStep;
    private int imageIndex = 1;
    private MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        componentInit();
        startEventListening();
    }

    private void componentInit() {
        mIvBack = findViewById(R.id.btn_back_settings);
        mIBtnBack = findViewById(R.id.help_back1);
        mIBtnNext = findViewById(R.id.help_next1);
        mIvBackground = findViewById(R.id.help_bgr);
        setImageBackground();
        mIBtnBack.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.tv_Settings)).setText("Help");
        mEdtStep = findViewById(R.id.tv_step);
        musicService = new MusicService(this);
        MusicService.startPlayingMusic(musicService, R.raw.kiss_the_rain);
    }

    private void startEventListening() {
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mIBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageIndex--;
                setImageBackground();
                updateSteps();
            }
        });
        mIBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageIndex++;
                setImageBackground();
                updateSteps();
            }
        });
    }

    public void updateSteps() {
        mEdtStep.setText(imageIndex + " / 10");
    }

    public void setImageBackground() {
        switch (imageIndex) {
            case 1:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.im_help1));
                mIBtnBack.setVisibility(View.GONE);
                break;
            case 2:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.im_help2));
                mIBtnBack.setVisibility(View.VISIBLE);
                break;
            case 3:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.im_help3));
                break;
            case 4:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.im_help4));
                break;
            case 5:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.im_help5));
                break;
            case 6:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.im_help6));
                break;
            case 7:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.im_help7));
                break;
            case 8:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.ic_help_win));
                break;
            case 9:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.ic_help_losing));
                mIBtnNext.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_forward));
                break;
            case 10:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.ic_help_done_tutorial));
                mIBtnNext.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_black_24dp));
                break;
            case 11:
                mIBtnNext.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_black_24dp));
                finish();
                break;
            default:
                Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
                break;
        }
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
