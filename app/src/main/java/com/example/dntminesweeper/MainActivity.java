package com.example.dntminesweeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dntminesweeper.Board.BoardUtils;

public class MainActivity extends AppCompatActivity {
    private Button mBtnNewGame, mBtnHighScore, mBtnSettings, mBtnHelp;
    SharedPreferences userSettings;
    private String username, gameMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        componentInit();
        startEventListening();
    }

    private void componentInit() {
        mBtnNewGame = findViewById(R.id.btn_NewGame);
        mBtnHighScore = findViewById(R.id.btn_HighScore);
        mBtnSettings = findViewById(R.id.btn_Settings);
        mBtnHelp = findViewById(R.id.btn_Help);
    }

    private void startEventListening() {
        mBtnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDifficultyMenu();
            }
        });
        mBtnHighScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, HighScoreActivity.class));
            }
        });
        mBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        mBtnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            }
        });
        userSettings = getApplicationContext().getSharedPreferences("UserInfo", 0);
        username = userSettings.getString("username", "");
        if (!username.equals("")) {
            Toast.makeText(MainActivity.this, "Welcome, " + username, Toast.LENGTH_SHORT).show();
            SettingsActivity.isMusicOn = userSettings.getBoolean("isMusicOn", true);
            SettingsActivity.isSoundOn = userSettings.getBoolean("isSoundOn", true);
            SettingsActivity.isVibrationOn = userSettings.getBoolean("isVibrationOn", true);
        } else {
            welcomeNewPlayer();
        }
    }

    public void welcomeNewPlayer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome to Minesweeper!");
        builder.setMessage("Enter you username");
        final EditText input = new EditText(this);
        input.setGravity(Gravity.CENTER);
        input.setWidth(50);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username = input.getText().toString();
                SharedPreferences.Editor editor = userSettings.edit();
                editor.putString("username", username);
                editor.putInt("fresherRecord", 999);
                editor.putInt("juniorRecord", 999);
                editor.putInt("seniorRecord", 999);
                editor.putInt("expertRecord", 999);
                editor.putInt("customRecord", 999);
                editor.putInt("customRows", 16);
                editor.putInt("customCols", 30);
                editor.putInt("customMines", 99);
                editor.putBoolean("isSoundOn", true);
                editor.putBoolean("isMusicOn", true);
                editor.putBoolean("isVibrationOn", true);
                editor.apply();
                Toast.makeText(MainActivity.this, "Welcome, " + username, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public void showDifficultyMenu() {
        final String[] option = new String[]{"Fresher", "Junior", "Senior", "Expert",
                "Custom ..."};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Difficulty Level");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                boolean isValid = false;
                switch (position) {
                    case 0:
                        BoardUtils.BOARD_TILES_PER_ROW = 9;
                        BoardUtils.NUM_BOARD_TILES = 81;
                        BoardUtils.NUM_BOMBS = 10;
                        BoardUtils.GAME_MODE = BoardUtils.DIFFICULTY = 1;
                        gameMode = "fresherRecord";
                        Toast.makeText(MainActivity.this, R.string.fresherGame, Toast.LENGTH_SHORT).show();
                        isValid = true;
                        break;
                    case 1:
                        BoardUtils.BOARD_TILES_PER_ROW = 12;
                        BoardUtils.NUM_BOARD_TILES = 144;
                        BoardUtils.NUM_BOMBS = 20;
                        BoardUtils.GAME_MODE = BoardUtils.DIFFICULTY = 2;
                        gameMode = "juniorRecord";
                        Toast.makeText(MainActivity.this, R.string.juniorGame, Toast.LENGTH_SHORT).show();
                        isValid = true;
                        break;
                    case 2:
                        BoardUtils.BOARD_TILES_PER_ROW = 16;
                        BoardUtils.NUM_BOARD_TILES = 256;
                        BoardUtils.NUM_BOMBS = 40;
                        BoardUtils.GAME_MODE = BoardUtils.DIFFICULTY = 3;
                        gameMode = "seniorRecord";
                        Toast.makeText(MainActivity.this, R.string.seniorGame, Toast.LENGTH_SHORT).show();
                        isValid = true;
                        break;
                    case 3:
                        BoardUtils.BOARD_TILES_PER_ROW = 16;
                        BoardUtils.NUM_BOARD_TILES = 480;
                        BoardUtils.NUM_BOMBS = 99;
                        BoardUtils.GAME_MODE = BoardUtils.DIFFICULTY = 4;
                        gameMode = "expertRecord";
                        Toast.makeText(MainActivity.this, R.string.expertGame, Toast.LENGTH_SHORT).show();
                        isValid = true;
                        break;
                    case 4:
                        BoardUtils.BOARD_TILES_PER_ROW = 16;
                        BoardUtils.NUM_BOARD_TILES = 480;
                        BoardUtils.NUM_BOMBS = 99;
                        BoardUtils.GAME_MODE = BoardUtils.DIFFICULTY = 3;
                        gameMode = "customRecord";
                        isValid = true;
                        Toast.makeText(MainActivity.this, R.string.customGame, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        break;
                }

                try {
                    if (isValid) {
                        final Intent newGameIntent = new Intent(MainActivity.this, GameActivity.class);
                        newGameIntent.putExtra("gameMode", gameMode);
                        startActivity(newGameIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }
}
