package com.example.dntminesweeper;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dntminesweeper.Board.Board;
import com.example.dntminesweeper.Board.BoardUtils;
import com.example.dntminesweeper.Tiles.BombTile;
import com.example.dntminesweeper.Tiles.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements
        View.OnClickListener, MediaPlayer.OnCompletionListener {

    Map<Integer, Tile> boardSetup;
    Map<Integer, ImageView> tiles;
    Board board;

    private TextView mTvBombCounter, mTvTimer, mTvFlagCounter;
    private ImageView mImvGameFace;
    private ImageButton mBtnReveal;

    private boolean isGameOver = false, timerStarted = false, isGamePaused = false;
    private String gameMode;
    private int seconds = 0, minutes = 0, flagCount = 0;

    private MediaPlayer mMediaPlayer;
    private boolean isRevealingBomb = true, firstClick = true;

    private LinearLayout tileLayout;
    private ScrollView scrollView;
    private RelativeLayout gameLayout, bannerLayout, mRlGameDisplay;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        componentsInit();
        tiles = new HashMap<>();
        createGame();
        setContentView(createGameGrid(BoardUtils.NUM_BOARD_TILES, BoardUtils.BOARD_TILES_PER_ROW));
        mTvBombCounter.setText("/ ");
        mTvBombCounter.append(BoardUtils.NUM_BOMBS + "");

        createHashMapTiles();
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
        Toast.makeText(this, gameMode, Toast.LENGTH_SHORT).show();
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

    public void startTimer() {
        if (!timerStarted) {
            if (timer != null) {
                timer.cancel();
                timer = null;
                seconds = 0;
                minutes = 0;
                mTvTimer.setText(R.string.default_timer_text);
            }
            Thread timerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            String stringSeconds = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);
                            String stringMinutes = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
                            updateTimerUI(stringMinutes + ":" + stringSeconds);
                            seconds++;
                            if (seconds == 60) {
                                seconds = 0;
                                minutes = minutes + 1;
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
        restoreState();
    }

    private void restoreState() {
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onClick(View v) {
        if (v == mBtnReveal) {
            mBtnReveal.setImageResource(isRevealingBomb ? R.drawable.ic_classic_flag : R.drawable.ic_classic_mine_normal);
            isRevealingBomb = !isRevealingBomb;
        } else if (v == mImvGameFace) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Start a new game");
            builder.setMessage("Are you sure to play a new game?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timerStarted = false;
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
            if (tile.isRevealed()) {
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
        revealAllTiles();
        mImvGameFace.setImageDrawable(getResources().getDrawable(R.drawable.ic_deadface));
        gameOverDialog.setTitle("OOPS! You clicked the bomb!");
        int time = minutes * 60 + seconds;
        gameOverDialog.setMessage("Time played: " + time + " sec\n" +
                "Bomb found: " + flagCount + " / " + BoardUtils.NUM_BOMBS);
        gameOverDialog.setPositiveButton("Play again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timerStarted = false;
                playAgain();
            }
        });
        gameOverDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

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

        gameOverDialog.show();
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

                    AlertDialog.Builder winDialog = new AlertDialog.Builder(this);
                    winDialog.setTitle("Congratulations!");
                    revealAllTiles();
                    timer.cancel();
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("UserInfo", 0);
                    int currentRecord = minutes * 60 + seconds;
                    int lastRecord = pref.getInt(gameMode, -1);
                    if (currentRecord < lastRecord) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt(gameMode, currentRecord);
                        editor.commit();
                        String[] player = gameMode.split("R");
                        winDialog.setMessage("You've made a new record, " + player[0]
                                + ": " + currentRecord + " sec");
                    } else {
                        winDialog.setMessage("Time played: " + currentRecord + " sec\n" +
                                "Try harder to break the record of " + lastRecord + " sec");
                    }
                    winDialog.setPositiveButton(R.string.play_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            playAgain();
                        }
                    });

                    winDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

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
                    winDialog.show();
                }
            }
        }
    }

    private void playAgain() {
        for (int i = 0; i < tiles.size(); i++) {
            tiles.get(i).setImageDrawable(getResources().getDrawable(R.drawable.ic_classic_unrevealed_tile));
        }
        mImvGameFace.setImageDrawable(getResources().getDrawable(R.drawable.im_happy_face));
        flagCount = 0;
        mTvFlagCounter.setText(String.valueOf(flagCount));
        isRevealingBomb = true;
        mBtnReveal.setImageResource(R.drawable.ic_classic_mine_normal);
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
