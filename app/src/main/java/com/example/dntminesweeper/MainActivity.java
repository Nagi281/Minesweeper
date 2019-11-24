package com.example.dntminesweeper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.dntminesweeper.Board.BoardUtils;

public class MainActivity extends AppCompatActivity {
    private Button mBtnNewGame;
    private Button mBtnHighScore;
    private Button mBtnOption;
    private Button mBtnHelp;

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
        mBtnOption = findViewById(R.id.btn_Options);
        mBtnHelp = findViewById(R.id.btn_Help);
    }

    private void startEventListening() {
        mBtnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDifficultyMenu();
            }
        });
    }

    public void showDifficultyMenu() {
        final String[] option = new String[]{"Fresher", "Junior", "Senior", "Expert",
                "Custom ..."};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, option);
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
                        BoardUtils.GAME_MODE = 1;
                        BoardUtils.DIFFICULTY = 1;
                        Toast.makeText(MainActivity.this, "Fresher", Toast.LENGTH_SHORT).show();
                        isValid = true;
                        break;
                    case 1:
                        BoardUtils.BOARD_TILES_PER_ROW = 12;
                        BoardUtils.NUM_BOARD_TILES = 144;
                        BoardUtils.NUM_BOMBS = 20;
                        BoardUtils.GAME_MODE = 2;
                        BoardUtils.DIFFICULTY = 2;
                        Toast.makeText(MainActivity.this, "Junior", Toast.LENGTH_SHORT).show();
                        isValid = true;
                        break;
                    case 2:
                        BoardUtils.BOARD_TILES_PER_ROW = 16;
                        BoardUtils.NUM_BOARD_TILES = 256;
                        BoardUtils.NUM_BOMBS = 40;
                        BoardUtils.GAME_MODE = 3;
                        BoardUtils.DIFFICULTY = 3;
                        Toast.makeText(MainActivity.this, "Senior", Toast.LENGTH_SHORT).show();
                        isValid = true;
                        break;
                    case 3:
                        BoardUtils.BOARD_TILES_PER_ROW = 24;
                        BoardUtils.NUM_BOARD_TILES = 480;
                        BoardUtils.NUM_BOMBS = 99;
                        BoardUtils.GAME_MODE = 4;
                        BoardUtils.DIFFICULTY = 4;
                        Toast.makeText(MainActivity.this, "Expert", Toast.LENGTH_SHORT).show();
                        isValid = true;
                        break;
                    case 4:
                        BoardUtils.BOARD_TILES_PER_ROW = 24;
                        BoardUtils.NUM_BOARD_TILES = 480;
                        BoardUtils.NUM_BOMBS = 99;
                        BoardUtils.GAME_MODE = 4;
                        BoardUtils.DIFFICULTY = 4;
                        isValid = true;
                        Toast.makeText(MainActivity.this, "Unavailable, default to Expert", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        break;
                }

                try {
                    if (isValid) {
                        final Intent newGameIntent = new Intent(MainActivity.this, GameActivity.class);
                        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, newGameIntent, 0);
                        pendingIntent.send();
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
