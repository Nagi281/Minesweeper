package com.example.dntminesweeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HighScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        setRecords();
    }

    private void setRecords() {
        (findViewById(R.id.btn_back_settings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        SharedPreferences pref = getApplicationContext().getSharedPreferences("UserInfo", 0);
        String username = pref.getString("username", null);
        ((TextView) findViewById(R.id.tv_Settings)).setText(username + "'s High Score");
        final String[] gameMode = new String[]{"fresherRecord", "juniorRecord",
                "seniorRecord", "expertRecord"};
        int[] record = new int[5];
        for (int i = 0; i < gameMode.length; i++) {
            record[i] = pref.getInt(gameMode[i], -1);
        }
        ((TextView) findViewById(R.id.tv_fresherRecord)).setText(record[0] + " sec");
        ((TextView) findViewById(R.id.tv_juniorRecord)).setText(record[1] + " sec");
        ((TextView) findViewById(R.id.tv_seniorRecord)).setText(record[2] + " sec");
        ((TextView) findViewById(R.id.tv_expertRecord)).setText(record[3] + " sec");
    }
}
