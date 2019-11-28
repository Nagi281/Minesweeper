package com.example.dntminesweeper;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
    private ImageView mIvBackground, mIvBack;
    private ImageButton mIBtnBack, mIBtnNext;
    private int imageIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        componentInit();
        startEventListening();
    }

    private void componentInit() {
        mIvBack = findViewById(R.id.help_back);
        mIBtnBack = findViewById(R.id.help_back1);
        mIBtnNext = findViewById(R.id.help_next1);
        mIvBackground = findViewById(R.id.help_bgr);
        mIvBackground.setScaleType(ImageView.ScaleType.CENTER);
        setImageBackground();
        mIBtnBack.setVisibility(View.GONE);
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
            }
        });
        mIBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageIndex++;
                setImageBackground();
            }
        });
    }

    private void setImageBackground() {
        switch (imageIndex) {
            case 1:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.help1));
                mIBtnBack.setVisibility(View.GONE);
                break;
            case 2:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.help2));
                mIBtnBack.setVisibility(View.VISIBLE);
                break;
            case 3:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.help3));
                break;
            case 4:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.help4));
                break;
            case 5:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.help5));
                break;
            case 6:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.help6));
                break;
            case 7:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.help7));
                break;
            case 8:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.winhelp));
                break;
            case 9:
                mIvBackground.setImageDrawable(getResources().getDrawable(R.drawable.losehelp));
                mIBtnNext.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_black_24dp));
                break;
            case 10:
                imageIndex = 1;
                finish();
                break;
            default:
                Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
