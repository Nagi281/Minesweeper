package com.example.dntminesweeper;

import android.app.Activity;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dntminesweeper.Board.BoardUtils;

public class MainActivity extends AppCompatActivity {
    private Button mBtnResume, mBtnNewGame, mBtnHighScore, mBtnSettings, mBtnHelp;
    private SharedPreferences userSettings, gameSettings;
    private String username, gameMode;
    private int GAME_START = 113;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        componentInit();
        startEventListening();
    }

    private void componentInit() {
        mBtnResume = findViewById(R.id.btn_ResumeGame);
        mBtnNewGame = findViewById(R.id.btn_NewGame);
        mBtnHighScore = findViewById(R.id.btn_HighScore);
        mBtnSettings = findViewById(R.id.btn_Settings);
        mBtnHelp = findViewById(R.id.btn_Help);
    }

    private void startEventListening() {
        mBtnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent newGameIntent = new Intent(MainActivity.this, GameActivity.class);
                newGameIntent.putExtra("gameState", "resume");
                newGameIntent.putExtra("gameMode", gameMode);
                startActivityForResult(newGameIntent, GAME_START);
            }
        });
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
                boolean isValid = true;
                switch (position) {
                    case 0:
                        setGameMode(9, 81, 10, 1);
                        gameMode = "fresherRecord";
                        Toast.makeText(MainActivity.this, R.string.fresherGame, Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        setGameMode(12, 144, 20, 2);
                        gameMode = "juniorRecord";
                        Toast.makeText(MainActivity.this, R.string.juniorGame, Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        setGameMode(16, 256, 40, 3);
                        gameMode = "seniorRecord";
                        Toast.makeText(MainActivity.this, R.string.seniorGame, Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        setGameMode(16, 480, 99, 4);
                        gameMode = "expertRecord";
                        Toast.makeText(MainActivity.this, R.string.expertGame, Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        setGameMode(16, 480, 99, 4);
                        gameMode = "customRecord";
                        Toast.makeText(MainActivity.this, R.string.customGame, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        isValid = false;
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        break;
                }
                if (isValid) {
                    final Intent newGameIntent = new Intent(MainActivity.this, GameActivity.class);
                    newGameIntent.putExtra("gameMode", gameMode);
                    newGameIntent.putExtra("gameState", "newGame");
                    startActivityForResult(newGameIntent, GAME_START);
                } else {
                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setGameMode(int rows, int total, int bombs, int mode) {
        BoardUtils.BOARD_TILES_PER_ROW = rows;
        BoardUtils.NUM_BOARD_TILES = total;
        BoardUtils.NUM_BOMBS = bombs;
        BoardUtils.GAME_MODE = BoardUtils.DIFFICULTY = mode;
        gameSettings = getSharedPreferences("gameInfo", 0);
        SharedPreferences.Editor editor = gameSettings.edit();
        editor.putInt("rows", rows);
        editor.putInt("total", total);
        editor.putInt("bombs", bombs);
        editor.putInt("mode", mode);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GAME_START) {
            if (resultCode != Activity.RESULT_OK) {
                mBtnResume.setVisibility(View.GONE);
                return;
            }
            String gameState = data.getExtras().getString("gameState");
            SharedPreferences.Editor editor = userSettings.edit();
            if (gameState.equals("paused")) {
                mBtnResume.setVisibility(View.VISIBLE);
                editor.putString("gameState", "paused");
                editor.apply();
            } else {
                editor.remove("gameState");
                editor.apply();
            }
        }
    }
}
