package com.example.dntminesweeper;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dntminesweeper.Board.Board;
import com.example.dntminesweeper.Board.BoardUtils;
import com.example.dntminesweeper.Tiles.BombTile;
import com.example.dntminesweeper.Tiles.Tile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements
        View.OnClickListener, MediaPlayer.OnCompletionListener {
    private Map<Integer, Tile> boardSetup;
    private Map<Integer, ImageView> tiles;
    private Board board;

    private TextView mTvBombCounter, mTvTimer, mTvFlagCounter;
    private ImageView mImvGameFace;
    private ImageButton mBtnReveal;
    private MediaPlayer mMediaPlayer;
    private LinearLayout tileLayout;
    private ScrollView scrollView;
    private RelativeLayout gameLayout, bannerLayout, mRlGameDisplay;
    private View dialogView;

    private boolean isGameOver = false, timerStarted = false;
    private String gameMode;
    private int seconds = 0, minutes = 0, flagCount = 0;
    private boolean isRevealingBomb = true, firstClick = true, paused = false;
    private SharedPreferences gameData;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        componentsInit();
        tiles = new HashMap<>();
        String gameState = getIntent().getExtras().getString("gameState");
        setContentView(createGameGrid(BoardUtils.NUM_BOARD_TILES, BoardUtils.BOARD_TILES_PER_ROW));
        createHashMapTiles();
        if (gameState != null && gameState.equals("resume")) {
            loadGameData();
        } else {
            createGame();
        }
        mTvBombCounter.setText("/ ");
        mTvBombCounter.append(BoardUtils.NUM_BOMBS + "");

    }

    private void loadGameData() {
        try {
            firstClick = false;
            gameData = getSharedPreferences("gameData", 0);
            String gameDataBoard = gameData.getString("gameDataBoard", "");
            seconds = gameData.getInt("seconds", -1);
            minutes = gameData.getInt("minutes", -1);
            flagCount = gameData.getInt("flagCount", 0) - 1;
            updateFlag(true);
            isRevealingBomb = gameData.getBoolean("isRevealingBomb", false);
            mBtnReveal.setImageResource(isRevealingBomb ? R.drawable.ic_classic_mine_normal
                    : R.drawable.ic_classic_flag);
            Gson gson = new Gson();
            boardSetup = new HashMap<>();
            boardSetup = gson.fromJson(gameDataBoard, new TypeToken<Map<Integer, Tile>>() {
            }.getType());
            for (int i : boardSetup.keySet()) {
                if (boardSetup.get(i).isRevealed()) {
                    startReveal(tiles.get(i), boardSetup.get(i));
                }
                if (boardSetup.get(i).isFlagged()) {
                    tiles.get(i).setImageResource(R.drawable.ic_classic_flag);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void componentsInit() {
        tileLayout = findViewById(R.id.tileLayout);
        gameLayout = findViewById(R.id.gameLayout);
        scrollView = findViewById(R.id.sv_gameScroll);
        bannerLayout = findViewById(R.id.lo_bannerLayout);
        mRlGameDisplay = findViewById(R.id.rl_gameDisplay);
        mTvBombCounter = findViewById(R.id.tv_bomb);
        mBtnReveal = findViewById(R.id.btn_reveal);
        mBtnReveal.setOnClickListener(this);
        mTvFlagCounter = findViewById(R.id.tv_flagCounter);
        mTvTimer = findViewById(R.id.tv_timer);
        mImvGameFace = findViewById(R.id.imv_gameFace);
        mImvGameFace.setOnClickListener(this);

        gameMode = getIntent().getExtras().getString("gameMode");
        gameData = getSharedPreferences("gameData", 0);

    }

    private View createGameGrid(int numberOfTiles, int tilesPerRow) {
        gameLayout.removeAllViews();
        tileLayout.removeAllViews();
        scrollView.removeAllViews();
        mRlGameDisplay.removeAllViews();
        int rows = numberOfTiles / tilesPerRow;

        final LinearLayout MainLinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParamsMain = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsMain.setMargins(0, 0, 0, 20);
        MainLinearLayout.setLayoutParams(layoutParamsMain);
        MainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        MainLinearLayout.setId(R.id.tileLayout);
        MainLinearLayout.setWeightSum(rows);

        tileLayout = MainLinearLayout;

        ArrayList<LinearLayout> linearLayouts = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            LinearLayout linearLayout = new LinearLayout(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
            layoutParams.setMargins(0, 0, 0, 0);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setWeightSum(tilesPerRow);

            linearLayouts.add(linearLayout);
            MainLinearLayout.addView(linearLayout);
        }

        for (final LinearLayout l : linearLayouts) {
            for (int i = 0; i < tilesPerRow; i++) {
                final ImageView tile = new ImageView(this);

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                int height = size.y;

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                        ((height / 100) * 5, (height / 100) * 5, 1);
                tile.setLayoutParams(layoutParams);
                tile.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_unrevealed_tile));
                l.addView(tile);
            }
        }
        gameLayout.addView(bannerLayout);
        scrollView.addView(MainLinearLayout);
        mRlGameDisplay.setGravity(Gravity.CENTER);
        mRlGameDisplay.addView(scrollView);
        gameLayout.addView(mRlGameDisplay);
        return gameLayout;
    }

    private void createHashMapTiles() {
        int count = 0;
        for (int i = 0; i < tileLayout.getChildCount(); i++) {
            LinearLayout childLayout = (LinearLayout) tileLayout.getChildAt(i);
            for (int j = 0; j < childLayout.getChildCount(); j++) {
                ImageView im = (ImageView) childLayout.getChildAt(j);
                im.setCropToPadding(false);
                im.setScaleType(ImageView.ScaleType.FIT_XY);
                im.setTag(count);
                im.getLayoutParams().width = 0;
                im.setOnClickListener(this);
                tiles.put(count, im);
                count++;
            }
        }
    }

    private void createGame() {
        board = new Board();
        boardSetup = board.getGameBoard();
    }

    Thread timerThread = null;

    public void startTimer() {
        if (!timerStarted) {
            timerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (!paused) {
                                String stringSeconds = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);
                                String stringMinutes = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
                                updateTimerUI(stringMinutes + ":" + stringSeconds);
                                seconds++;
                                if (seconds == 60) {
                                    seconds = 0;
                                    minutes = minutes + 1;
                                }
                            }
                        }
                    }, 0, 1000);
                }
            });
            timerThread.start();
            timerStarted = true;
        }
    }

    private void updateTimerUI(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvTimer.setText(s);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!firstClick) {
            restoreState();
        }
    }

    private void restoreState() {
        startTimer();
        paused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onBackPressed() {
        storeCurrentGameState();
        final Intent pausedIntent = new Intent();
        pausedIntent.putExtra("gameState", "paused");
        setResult(Activity.RESULT_OK, pausedIntent);
        finish();
    }

    private void storeCurrentGameState() {
        Gson gson = new Gson();
        String gameDataBoard = gson.toJson(boardSetup);
        SharedPreferences.Editor editor = gameData.edit();
        editor.putString("gameDataBoard", gameDataBoard);
        editor.putInt("seconds", seconds);
        editor.putInt("minutes", minutes);
        editor.putInt("flagCount", flagCount);
        editor.putBoolean("isRevealingBomb", isRevealingBomb);
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnReveal) {
            mBtnReveal.setImageResource(isRevealingBomb ? R.drawable.ic_classic_flag
                    : R.drawable.ic_classic_mine_normal);
            isRevealingBomb = !isRevealingBomb;
        } else if (v == mImvGameFace) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Start a new game");
            builder.setMessage("Are you sure to play a new game?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    playAgain();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.show();
        } else {
            Tile tile = boardSetup.get(v.getTag());
            if (tile != null && tile.isRevealed()) {
                int bombCount = tile.getTileImageint();
                for (int j : tile.getNeighbours()) {
                    if (boardSetup.get(j).isFlagged()) {
                        bombCount--;
                    }
                }
                if (bombCount == 0) {
                    for (int j : tile.getNeighbours()) {
                        if (!boardSetup.get(j).isRevealed() && !boardSetup.get(j).isFlagged()) {
                            revealTile(tiles.get(j), boardSetup.get(j));
                        }
                    }
                    v.setOnClickListener(null);
                }
            } else {
                if (isRevealingBomb) {
                    if (SettingsActivity.isVibrationOn) {
                        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        assert vib != null;
                        vib.vibrate(100);
                    }
                    if (firstClick && tile instanceof BombTile || (firstClick && tile.getNeighbourbombs() != 0)) {
                        board = new Board();
                        boardSetup = board.getGameBoard();
                        createHashMapTiles();
                        v.callOnClick();
                    } else {
                        if (!tile.isFlagged()) {
                            revealTile((ImageView) v, tile);
                            tile.setRevealed(true);
                            startTimer();
                            firstClick = false;
                        }
                    }
                } else {
                    int tag = (int) v.getTag();
                    ImageView i = tiles.get(tag);

                    mMediaPlayer = MediaPlayer.create(this, R.raw.flagselector);
                    mMediaPlayer.setOnCompletionListener(this);

                    if (!tile.isFlagged()) {
                        if (i != null) {
                            i.setImageResource(R.drawable.ic_classic_flag);
                        }
                        tile.setFlagged(true);
                        updateFlag(true);

                        if (SettingsActivity.isVibrationOn) {
                            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            assert vib != null;
                            vib.vibrate(100);
                        }

                        if (SettingsActivity.isVibrationOn) {
                            mMediaPlayer.start();
                        }


                    } else if (tile.isFlagged()) {
                        i.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_unrevealed_tile));
                        mMediaPlayer.start();
                        tile.setFlagged(false);
                        updateFlag(false);

                        if (SettingsActivity.isVibrationOn) {
                            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            assert vib != null;
                            vib.vibrate(100);
                        }
                    }
                }
            }

            checkGameState();
        }
    }


    private void revealTile(final ImageView v, final Tile t) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "rotationY", 0.0f, 180);
        final ObjectAnimator translateBack = ObjectAnimator.ofFloat(v, "rotationY", 0);
        animation.setDuration(200);
        animation.setRepeatCount(0);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        translateBack.setDuration(0);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                translateBack.start();
                startReveal(v, t);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        if (!t.isRevealed() && !(t instanceof BombTile)) {
            t.setRevealed(true);
            animation.start();
        } else if (t instanceof BombTile) {
            animation.end();
        }
    }

    private void startReveal(ImageView v, Tile t) {
        switch (t.getTileImageint()) {
            case 0:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_reveal
                ));
                boolean temp = isRevealingBomb;
                isRevealingBomb = true;
                for (int i : t.getNeighbours()) {
                    if (!(boardSetup.get(i) instanceof BombTile) && (i != -1)) {
                        tiles.get(i).callOnClick();
                    }
                }
                isRevealingBomb = temp;
                break;
            case 1:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_1));
                break;
            case 2:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_2));
                break;
            case 3:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_3));
                break;
            case 4:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_4));
                break;
            case 5:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_5));
                break;
            case 6:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_6));
                break;
            case 7:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_7));
                break;
            case 8:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_8));
                break;
            case -1:
                v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_mine_exploded));
                t.setTileImageint(-2);
                timer.cancel();
                isGameOver();
                break;
        }
    }

    private void isGameOver() {
        isGameOver = true;
        AlertDialog.Builder gameOverDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.lose_dialog, null);
        revealAllTiles();
        mImvGameFace.setImageDrawable(getResources().getDrawable(R.drawable.ic_deadface));

        gameOverDialog.setView(dialogView);
        final AlertDialog gameOver = gameOverDialog.create();
        dialogView.findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameOver.dismiss();
                finish();
            }
        });
        dialogView.findViewById(R.id.btn_playagain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerStarted = false;
                gameOver.dismiss();
                playAgain();
            }
        });
        loseAnnounce();
        if (SettingsActivity.isSoundOn) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.gameoversound);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.start();
        }

        if (SettingsActivity.isVibrationOn) {
            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            assert vib != null;
            vib.vibrate(300);
        }
        gameOver.show();
    }

    private void revealAllTiles() {
        for (int i = 0; i < boardSetup.size(); i++) {
            Tile t = boardSetup.get(i);
            if (!t.isRevealed()) {
                t.setRevealed(true);
                ImageView v = tiles.get(i);
                if (v != null) {
                    switch (t.getTileImageint()) {
                        case 0:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_reveal));
                            break;
                        case 1:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_1));
                            break;
                        case 2:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_2));
                            break;
                        case 3:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_3));
                            break;
                        case 4:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_4));
                            break;
                        case 5:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_5));
                            break;
                        case 6:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_6));
                            break;
                        case 7:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_7));
                            break;
                        case 8:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_num_8));
                            break;
                        case -1:
                            v.setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_mine_normal));
                            break;
                    }
                }
            }
        }
    }

    private void updateFlag(boolean increase) {
        if (increase) {
            flagCount++;
            mTvFlagCounter.setText(String.valueOf(flagCount));
        } else {
            flagCount--;
            mTvFlagCounter.setText(String.valueOf(flagCount));
        }
    }

    private void checkGameState() {
        if (!isGameOver) {
            int count = 0;
            for (int i = 0; i < BoardUtils.NUM_BOARD_TILES; i++) {
                if (boardSetup.get(i).isRevealed() && !(boardSetup.get(i) instanceof BombTile)) {
                    count++;
                }

                if (count == BoardUtils.NUM_BOARD_TILES - BoardUtils.NUM_BOMBS) {
                    isGameOver = true;
                    paused = true;
                    AlertDialog.Builder winDialog = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    dialogView = inflater.inflate(R.layout.win_dialog, null);
                    winDialog.setView(dialogView);
                    revealAllTiles();
                    timer.cancel();
                    final AlertDialog wingame = winDialog.create();
                    dialogView.findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            clearPausedData();
                            setResult(Activity.RESULT_CANCELED);
                            wingame.dismiss();
                            finish();
                        }
                    });
                    dialogView.findViewById(R.id.btn_playagain).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            wingame.dismiss();
                            playAgain();
                        }
                    });
                    winAnnounce();

                    if (SettingsActivity.isSoundOn) {
                        mMediaPlayer = MediaPlayer.create(this, R.raw.winsound);
                        mMediaPlayer.setOnCompletionListener(this);
                        mMediaPlayer.start();
                    }

                    if (SettingsActivity.isVibrationOn) {
                        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        assert vib != null;
                        vib.vibrate(300);
                    }
                    wingame.show();
                }
            }
        }
    }

    private void clearPausedData() {
        SharedPreferences.Editor editor = gameData.edit();
        editor.remove("gameDataBoard");
        editor.apply();
    }

    private void loseAnnounce() {
        TextView mTVMode = dialogView.findViewById(R.id.mode_game_lose);
        TextView mTvFlags = dialogView.findViewById(R.id.text_flags);
        TextView mTvTime = dialogView.findViewById(R.id.totaltime_lose);
        int time = minutes * 60 + seconds;
        mTvTime.setText(String.valueOf(time));
        String[] player = gameMode.split("R");
        mTVMode.setText(String.valueOf(player[0]));
        mTvFlags.setText(String.valueOf(flagCount));
    }

    private void winAnnounce() {
        TextView mTvMode = dialogView.findViewById(R.id.mode_game_win);
        TextView mTvTime = dialogView.findViewById(R.id.totaltime_win);
        TextView mTvBreakTime = dialogView.findViewById(R.id.break_time);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("UserInfo", 0);
        int currentRecord = minutes * 60 + seconds;
        mTvTime.setText(currentRecord + "");
        int lastRecord = pref.getInt(gameMode, -1);
        String[] record = gameMode.split("R");
        if (!record[0].equals("custom")) {
            if (currentRecord < lastRecord) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(gameMode, currentRecord);
                editor.commit();
                mTvBreakTime.setText("You have made a new Record");
            } else {
                mTvBreakTime.setText("Highest Record: " + lastRecord + " sec");
            }
        } else {
            mTvBreakTime.setText("Try something harder!");
        }
        mTvMode.setText(String.valueOf(record[0]));
    }

    private void playAgain() {
        paused = false;
        seconds = minutes = 0;
        for (int i = 0; i < tiles.size(); i++) {
            tiles.get(i).setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_unrevealed_tile));
        }
        mImvGameFace.setImageDrawable(getResources().getDrawable(R.drawable.im_happy_face));
        mTvFlagCounter.setText(String.valueOf(flagCount));
        mBtnReveal.setImageResource(R.drawable.ic_classic_mine_normal);
        isRevealingBomb = true;
        flagCount = -1;
        updateFlag(true);
        timerStarted = isGameOver = false;
        firstClick = true;
        createGame();
        mTvTimer.setText(R.string.default_timer_text);
        createHashMapTiles();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.release();
    }

}
