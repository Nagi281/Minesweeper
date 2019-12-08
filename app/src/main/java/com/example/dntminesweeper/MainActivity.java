package com.example.dntminesweeper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dntminesweeper.Board.BoardUtils;
import com.example.dntminesweeper.Music.MusicService;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Button mBtnResume, mBtnNewGame, mBtnHighScore, mBtnSettings, mBtnHelp;
    private SharedPreferences userSettings;
    private String username, gameMode;
    private int rows = 9, cols = 9, mines = 10;
    private int GAME_START = 113;
    private View dialogView;
    private SeekBar mSbMines;
    private TextView mTvMines;
    private MusicService musicService;

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
        musicService = new MusicService(this);
        MusicService.startPlayingMusic(musicService, R.raw.heart_of_courage);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.select_difficulty_view, option);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Select Difficulty Level");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                boolean isValid = true;
                switch (position) {
                    case 0:
                        setGameMode(9, 81, 10, 1);
                        gameMode = "fresherRecord";
                        Toast.makeText(MainActivity.this, R.string.fresherGame,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        setGameMode(12, 144, 20, 2);
                        gameMode = "juniorRecord";
                        Toast.makeText(MainActivity.this, R.string.juniorGame,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        setGameMode(16, 256, 40, 3);
                        gameMode = "seniorRecord";
                        Toast.makeText(MainActivity.this, R.string.seniorGame,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        setGameMode(16, 480, 99, 4);
                        gameMode = "expertRecord";
                        Toast.makeText(MainActivity.this, R.string.expertGame,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        customizeGame();
                        isValid = false;
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
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void customizeGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.customize_game_dialog, null);
        mSbMines = dialogView.findViewById(R.id.sb_mines);
        mSbMines.setMax(36);
        mSbMines.setProgress(0);
        mTvMines = dialogView.findViewById(R.id.tv_custom_mines);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        final TextView mTvRows = dialogView.findViewById(R.id.tv_custom_rows);
        final TextView mTvCols = dialogView.findViewById(R.id.tv_custom_columns);

        final SeekBar mSbRows = dialogView.findViewById(R.id.sb_rows);
        final SeekBar mSbCols = dialogView.findViewById(R.id.sb_columns);
        seekBarChangeListener(0, mSbMines, mTvMines, 10, 24);
        seekBarChangeListener(1, mSbRows, mTvRows, 9, 60);
        seekBarChangeListener(2, mSbCols, mTvCols, 9, 20);
        setAdjustButton(R.id.btn_rows_down, mSbRows, false, 1);
        setAdjustButton(R.id.btn_rows_up, mSbRows, true, 1);
        setAdjustButton(R.id.btn_columns_down, mSbCols, false, 2);
        setAdjustButton(R.id.btn_columns_up, mSbCols, true, 2);
        setAdjustButton(R.id.btn_mines_down, mSbMines, false, 0);
        setAdjustButton(R.id.btn_mines_up, mSbMines, true, 0);

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showDifficultyMenu();
            }
        });
        dialogView.findViewById(R.id.btn_OK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                gameMode = "customRecord";
                setGameMode(cols, rows * cols, mines, 3);
                Toast.makeText(MainActivity.this, R.string.customGame,
                        Toast.LENGTH_SHORT).show();
                final Intent newGameIntent = new Intent(MainActivity.this, GameActivity.class);
                newGameIntent.putExtra("gameMode", gameMode);
                newGameIntent.putExtra("gameState", "newGame");
                startActivityForResult(newGameIntent, GAME_START);
            }
        });
        dialog.show();
    }

    private void setAdjustButton(int buttonId, final SeekBar seekBar,
                                 final boolean isIncreasing, final int seek) {
        dialogView.findViewById(buttonId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int min = (seek == 0) ? 10 : 9;
                adjustProgress(seek, seekBar, isIncreasing, min);
            }
        });
    }

    private void adjustProgress(int seek, SeekBar seekBar, boolean isIncreasing, int min) {
        int value = 1;
        int progress = seekBar.getProgress();
        if (!isIncreasing) {
            if (progress - value < 0) {
                seekBar.setProgress(0);
            } else {
                seekBar.setProgress(progress - value);
            }
        } else {
            if (progress - value > seekBar.getMax()) {
                seekBar.setProgress(seekBar.getMax());
            } else {
                seekBar.setProgress(progress + value);
            }
        }
    }

    public void seekBarChangeListener(final int seek, SeekBar seekBar,
                                      final TextView textView,
                                      final int min, final int max) {
        seekBar.setMax((max - min));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue + min;
                setCustomValue(seek, progress);
                textView.setText(String.valueOf(progress));
                int mineMax = rows * cols * 3 / 10;
                seekBarChangeListener(0, mSbMines, mTvMines, 10, mineMax);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText(seek == 0 ? mines + "" : progress + "");
            }
        });
    }

    public void setCustomValue(int seek, int value) {
        switch (seek) {
            case 0:
                mines = value;
                break;
            case 1:
                rows = value;
                break;
            case 2:
                cols = value;
                break;
            default:
                break;
        }
    }

    private void setGameMode(int rows, int total, int bombs, int mode) {
        BoardUtils.BOARD_TILES_PER_ROW = rows;
        BoardUtils.NUM_BOARD_TILES = total;
        BoardUtils.NUM_BOMBS = bombs;
        BoardUtils.GAME_MODE = BoardUtils.DIFFICULTY = mode;
        SharedPreferences gameSettings = getSharedPreferences("gameInfo", 0);
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
