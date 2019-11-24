package com.example.dntminesweeper;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
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
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

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

    private TextView mTvBombCounter;
    private TextView mTvTimer;
    private TextView mTvFlagCounter;
    private ImageView mImvGameFace;
    private ImageButton mBtnReveal;

    private boolean isGameOver = false;
    private boolean timerStarted = false;
    private boolean isGamePaused = false;

    private int seconds = 0;
    private int minutes = 0;
    private int flagCount = 0;

    private MediaPlayer mMediaPlayer;
    private boolean isRevealingBomb = true;
    private boolean firstClick = true;

    private LinearLayout tileLayout;
    private ConstraintLayout gameLayout;
    private RelativeLayout bannerLayout;

    Timer timer;

    @SuppressLint({"CommitPrefEdits", "UseSparseArrays"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        tileLayout = findViewById(R.id.tileLayout);
        gameLayout = findViewById(R.id.gameLayout);
        bannerLayout = findViewById(R.id.lo_bannerLayout);
        mTvBombCounter = findViewById(R.id.tv_bomb);

        mBtnReveal = findViewById(R.id.btn_reveal);
        mBtnReveal.setOnClickListener(this);
        tiles = new HashMap<>();
        createGame();
        setContentView(createGameGrid(BoardUtils.NUM_BOARD_TILES, BoardUtils.BOARD_TILES_PER_ROW));

        mTvFlagCounter = findViewById(R.id.tv_flagCounter);
        mTvTimer = findViewById(R.id.tv_timer);
        mImvGameFace = findViewById(R.id.imv_gameFace);
        mTvBombCounter.setText("/ ");
        mTvBombCounter.append(BoardUtils.NUM_BOMBS + "");

        createHashMapTiles();
    }

    private View createGameGrid(int numberOfTiles, int tilesPerRow) {
        gameLayout.removeAllViews();
        tileLayout.removeAllViews();

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
                tile.setImageDrawable(getResources().getDrawable(R.drawable.buttonv2));
                l.addView(tile);
            }
        }

        gameLayout.addView(MainLinearLayout);
        gameLayout.addView(bannerLayout);

        ConstraintSet set = new ConstraintSet();
        set.clone(gameLayout);
        set.connect(bannerLayout.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(MainLinearLayout.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 90);
        set.connect(MainLinearLayout.getId(), ConstraintSet.TOP, bannerLayout.getId(), ConstraintSet.BOTTOM, 50);
        set.connect(MainLinearLayout.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 25);
        set.connect(MainLinearLayout.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 25);
        set.applyTo(gameLayout);
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

    Thread timerThread;

    public void startTimer(boolean timerOn) {
        if (!timerOn) {
            if (timer != null) {
                timer.cancel();
                timer = null;
                seconds = 0;
                minutes = 0;
                mTvTimer.setText(R.string.default_timer_text);
            }
            timerThread = new Thread(new Runnable() {
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
        startTimer(timerStarted);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onClick(View v) {
        if (v == mBtnReveal) {
            mBtnReveal.setImageResource(isRevealingBomb ? R.drawable.flag2 : R.drawable.bomb_normal);
            isRevealingBomb = !isRevealingBomb;
        } else {
            Tile tile = boardSetup.get(v.getTag());
//            if (tile.isRevealed()) {
//                int bombCount = tile.getTileImageint();
//                for (int j : tile.getNeighbours()) {
//                    if (boardSetup.get(j).isFlagged()) {
//                        bombCount--;
//                    }
//                }
//                if(bombCount==0) {
//                    for (int j : tile.getNeighbours()) {
//                        if (!boardSetup.get(j).isRevealed() && !boardSetup.get(j).isFlagged()) {
//                            revealTile(tiles.get(j), boardSetup.get(j));
//                        }
//                    }
//                    v.setOnClickListener(null);
//                }
//            }
            if (isRevealingBomb) {
                if (SettingsActivity.clickvibration) {
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
                    if (!tile.isRevealed() && !tile.isFlagged()) {
                        revealTile((ImageView) v, tile);
                        tile.setRevealed(true);
                        startTimer(timerStarted);
                        checkGameState();
                        firstClick = false;
                    }
                }
            } else {
                int tag = (int) v.getTag();
                ImageView i = tiles.get(tag);

                mMediaPlayer = MediaPlayer.create(this, R.raw.flagselector);
                mMediaPlayer.setOnCompletionListener(this);

                if (!tile.isFlagged() && !tile.isRevealed()) {
                    i.setImageResource(R.drawable.flag2);
                    tile.setFlagged(true);
                    updateFlag(true);

                    if (SettingsActivity.flagVibrationOn) {
                        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        assert vib != null;
                        vib.vibrate(100);
                    }

                    if (SettingsActivity.flagSoundOn) {
                        mMediaPlayer.start();
                    }


                } else if (tile.isFlagged() && !tile.isRevealed()) {
                    i.setImageDrawable(getResources().getDrawable(R.drawable.buttonv2));
                    mMediaPlayer.start();
                    tile.setFlagged(false);
                    updateFlag(false);

                    if (SettingsActivity.flagVibrationOn) {
                        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        assert vib != null;
                        vib.vibrate(100);
                    }
                }
            }
        }
    }


    private void revealTile(final ImageView v, final Tile t) {
        ObjectAnimator animation = ObjectAnimator.ofFloat(v, "rotationY", 0.0f, 180);
        final ObjectAnimator translateBack = ObjectAnimator.ofFloat(v, "rotationY", 0);
        animation.setDuration(250);
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
                v.setImageDrawable(getResources().getDrawable(R.drawable.emptytile));
                for (int i : t.getNeighbours()) {
                    if (!(boardSetup.get(i) instanceof BombTile) && (i != -1)) {
                        tiles.get(i).callOnClick();
                    }
                }
                break;
            case 1:
                v.setImageDrawable(getResources().getDrawable(R.drawable.number_1));
                break;
            case 2:
                v.setImageDrawable(getResources().getDrawable(R.drawable.number_2));
                break;
            case 3:
                v.setImageDrawable(getResources().getDrawable(R.drawable.number_3));
                break;
            case 4:
                v.setImageDrawable(getResources().getDrawable(R.drawable.number_4));
                break;
            case 5:
                v.setImageDrawable(getResources().getDrawable(R.drawable.number_5));
                break;
            case 6:
                v.setImageDrawable(getResources().getDrawable(R.drawable.number_6));
                break;
            case 7:
                v.setImageDrawable(getResources().getDrawable(R.drawable.number_7));
                break;
            case 8:
                v.setImageDrawable(getResources().getDrawable(R.drawable.number_8));
                break;
            case -1:
                v.setImageDrawable(getResources().getDrawable(R.drawable.bomb_exploded));
                t.setTileImageint(-2);
                timer.cancel();
                isGameOver();
                break;
        }
    }

    private void isGameOver() {
        isGameOver = true;
        AlertDialog.Builder isGameOverDiag = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        revealAllTiles();
        mImvGameFace.setImageDrawable(getResources().getDrawable(R.drawable.deadface));
        isGameOverDiag.setTitle("YOU LOST");
        isGameOverDiag.setPositiveButton("Play again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timerStarted = false;
                playAgain();
            }
        });
        isGameOverDiag.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        if (SettingsActivity.gameOverSoundOn) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.gameoversound);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.start();
        }

        if (SettingsActivity.gameOverVibrationOn) {
            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            assert vib != null;
            vib.vibrate(300);
        }

        isGameOverDiag.show();
    }

    private void revealAllTiles() {
        for (int i = 0; i < boardSetup.size(); i++) {
            Tile t = boardSetup.get(i);
            if (!t.isRevealed()) {
                t.setRevealed(true);
                ImageView v = tiles.get(i);
                switch (t.getTileImageint()) {
                    case 0:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.emptytile));
                        break;
                    case 1:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.number_1));
                        break;
                    case 2:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.number_2));
                        break;
                    case 3:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.number_3));
                        break;
                    case 4:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.number_4));
                        break;
                    case 5:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.number_5));
                        break;
                    case 6:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.number_6));
                        break;
                    case 7:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.number_7));
                        break;
                    case 8:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.number_8));
                        break;
                    case -1:
                        v.setImageDrawable(getResources().getDrawable(R.drawable.bomb_normal));
                        break;
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

                    AlertDialog.Builder winDiag = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();

                    revealAllTiles();
                    timer.cancel();

                    winDiag.setTitle("YOU WON");
                    winDiag.setPositiveButton(R.string.play_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            playAgain();
                        }
                    });

                    winDiag.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    });

                    if (SettingsActivity.winGameSoundOn) {
                        mMediaPlayer = MediaPlayer.create(this, R.raw.winsound);
                        mMediaPlayer.setOnCompletionListener(this);
                        mMediaPlayer.start();
                    }

                    if (SettingsActivity.winGameVibrationOn) {
                        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        assert vib != null;
                        vib.vibrate(300);
                    }
                    winDiag.show();
                }
            }
        }
    }

    private void playAgain() {
        for (int i = 0; i < tiles.size(); i++) {
            tiles.get(i).setImageDrawable(getResources().getDrawable(R.drawable.buttonv2));
        }
        mImvGameFace.setImageDrawable(getResources().getDrawable(R.drawable.happy_face));
        flagCount = 0;
        mTvFlagCounter.setText(String.valueOf(flagCount));
        timerStarted = false;
        isGameOver = false;
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
