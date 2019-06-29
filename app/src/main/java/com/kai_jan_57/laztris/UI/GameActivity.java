package com.kai_jan_57.laztris.UI;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kai_jan_57.laztris.Part;
import com.kai_jan_57.laztris.PlayingField;
import com.kai_jan_57.laztris.R;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements PlayingField.LostEvent, PlayingField.LineCompletedEvent, PlayingFieldView.TickEvent {

    Handler uiHandler;

    TextView textViewScore;
    TextView textViewLevel;
    TextView textViewLines;

    TextView textViewCountDown;

    PlayingFieldView imageViewPlayingField;
    ImageView imageViewNextPart;

    ImageButton imageButtonDrop;
    ImageButton imageButtonLeft;
    ImageButton imageButtonRight;
    ImageButton imageButtonRotate;
    ImageButton imageButtonQuickTick;

    MenuItem menuItemGameActivity;

    Bitmap bitmapPlayingField;
    Bitmap bitmapNextPart;
    PlayingField playingField;

    CountDownTimer countDownTimer;

    int playingFieldWidth = 10;
    int playingFieldHeight = 20;

    float tickDelay = 500;
    float dropDelay = 50;
    int quickTickBonus = 1;
    boolean dropping;
    boolean gameRunning;
    boolean newGame = true;

    int score;
    int linesCompleted;
    int firstLevel;

    int colorBackgroundLight = Color.rgb(70, 20, 50);
    int colorBackgroundDark = Color.rgb(0, 0, 0);


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        uiHandler = new Handler();

        setNavigationBarVisibility(false);

        textViewScore = (TextView) findViewById(R.id.textViewScore);
        textViewLevel = (TextView) findViewById(R.id.textViewLevel);
        textViewLines = (TextView) findViewById(R.id.textViewLines);

        textViewCountDown = (TextView) findViewById(R.id.textViewCountDown);

        imageViewPlayingField = (PlayingFieldView) findViewById(R.id.imageViewPlayingField);
        imageViewNextPart = (ImageView) findViewById(R.id.imageViewNextPart);

        imageButtonDrop = (ImageButton) findViewById(R.id.imageButtonDrop);
        imageButtonLeft = (ImageButton) findViewById(R.id.imageButtonLeft);
        imageButtonRight = (ImageButton) findViewById(R.id.imageButtonRight);
        imageButtonRotate = (ImageButton) findViewById(R.id.imageButtonRotate);
        imageButtonQuickTick = (ImageButton) findViewById(R.id.imageButtonQuickTick);

        imageButtonLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onLeftClick(view);
                }
                return false;
            }
        });

        imageButtonQuickTick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onQuickTickClick(view);
                }
                return false;
            }
        });

        imageButtonRotate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onRotateClick(view);
                }
                return false;
            }
        });

        imageButtonDrop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onDropClick(view);
                }
                return false;
            }
        });

        imageButtonRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onRightClick(view);
                }
                return false;
            }
        });

        imageViewPlayingField.registerTickEvent(this);
        invalidateOptionsMenu();
    }

    void setNavigationBarVisibility(boolean visible) {
        int flags = 0;
        if (!visible) {
            flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(flags);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if ((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    uiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //setNavigationBarVisibility(false);
                        }
                    }, 3000);
                }
            }
        });
    }

    void setButtonsEnabled(boolean enabled) {
        imageButtonDrop.setEnabled(enabled);
        imageButtonLeft.setEnabled(enabled);
        imageButtonRight.setEnabled(enabled);
        imageButtonRotate.setEnabled(enabled);
        imageButtonQuickTick.setEnabled(enabled && !dropping);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gameactivity, menu);

        MenuItem menuItemTemp = menu.findItem(R.id.menuItemPauseResume);
        if (menuItemTemp != null) {
            menuItemGameActivity = menu.findItem(R.id.menuItemPauseResume);
        }
        resetGame();
        return true;
    }

    void resetGame() {
        newGame = true;
        if (gameRunning) {
            pauseGame();
        }
        playingField = new PlayingField(playingFieldWidth, playingFieldHeight);
        playingField.registerLineCompletedEvent(this);
        playingField.registerLostEvent(this);

        score = 0;
        linesCompleted = 0;
        dropping = false;
        tickDelay = calculateTickDelay();
        dropDelay = tickDelay / 10;
        newGame = true;

        displayScore(0);
        displayLines(0);
        displayLevel(0);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewCountDown.setVisibility(View.INVISIBLE);
            }
        });

        displayPlayingField(null);
        displayNextPart(null);
    }

    boolean cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            textViewCountDown.setVisibility(View.INVISIBLE);
            return true;
        }
        return false;
    }

    void skipCountDown() {
        if (cancelCountdown()) {
            textViewCountDown.setVisibility(View.INVISIBLE);
            gameRunning = true;
            imageViewPlayingField.setNextTick(PlayingFieldView.getTime());
            countDownTimer = null;
        }
    }

    void startGame(boolean resume) {
        setNavigationBarVisibility(false);
        if (resume) {
            textViewCountDown.setText(String.valueOf(3000/1000));
            textViewCountDown.setVisibility(View.VISIBLE);
            countDownTimer = new CountDownTimer(3000, 500) {

                @Override
                public void onTick(long l) {
                    final long time = l;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewCountDown.setText(String.valueOf((time + 1000) / 1000));
                        }
                    });
                }

                @Override
                public void onFinish() {
                    textViewCountDown.setVisibility(View.INVISIBLE);
                    gameRunning = true;
                    imageViewPlayingField.setNextTick(PlayingFieldView.getTime());
                    countDownTimer = null;
                }
            };
            countDownTimer.start();
        } else {
            resetGame();
            newGame = false;
            gameRunning = true;
            draw(false);
            imageViewPlayingField.setNextTick(PlayingFieldView.getTime() + (long)tickDelay);
        }
        setButtonsEnabled(true);
        menuItemGameActivity.setIcon(R.drawable.ic_button_pause);
        menuItemGameActivity.setTitle(R.string.menu_item_pause);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    void pauseGame() {
        if (gameRunning) {
            gameRunning = false;
        } else {
            cancelCountdown();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setButtonsEnabled(false);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                menuItemGameActivity.setIcon(R.drawable.ic_button_resume);
                if (newGame) {
                    menuItemGameActivity.setTitle(R.string.menu_item_start);
                }
                else {
                    menuItemGameActivity.setTitle(R.string.menu_item_resume);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseGame();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!gameRunning && !newGame && /*autoresume = */ false) {
            startGame(true);
        }
    }

    @Override
    public boolean onTick(PlayingFieldView caller, long time) {
        if (gameRunning) {
            playingField.tick();

            if (!newGame) {
                tickDelay = calculateTickDelay();
                dropDelay = tickDelay / 10;

                if (dropping) {
                    caller.setNextTick(caller.getNextTick() + (long)dropDelay);
                    score += 2;
                    displayScore(score);
                } else {
                    caller.setNextTick(caller.getNextTick() + (long)tickDelay);
                }
                if (!playingField.canFall(1)) {
                    dropping = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageButtonQuickTick.setEnabled(true);
                        }
                    });
                }
            }

            draw(newGame);
        }
        return gameRunning;
    }

    float calculateTickDelay() {
        return 3 / (linesCompleted / 10.0f + firstLevel + 3.5f) * 1000;
    }

    void displayScore(int score) {
        final int fscore = score;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewScore.setText(String.format(getString(R.string.textview_score), fscore));
            }
        });
    }

    void displayLevel(int level) {
        final int flevel = level;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewLevel.setText(String.format(getString(R.string.textview_level), flevel));
            }
        });
    }

    void displayLines(int lines) {
        final int flines = lines;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewLines.setText(String.format(getString(R.string.textview_lines), flines));
            }
        });
    }

    void draw(boolean updateInformationOnly) {
        if (!updateInformationOnly) {
            List<PlayingField.ColorPoint> colorPoints = new ArrayList<>(playingField.getGridContent());
            for (Point point : playingField.getPartCurrent().getPoints()) {
                colorPoints.add(new PlayingField.ColorPoint(point, playingField.getPartCurrent().color));
            }
            displayPlayingField(colorPoints);
            displayNextPart(playingField.getPartNext());
        }
    }

    void displayPlayingField(List<PlayingField.ColorPoint> colorPoints) {

        bitmapPlayingField = Bitmap.createBitmap(playingField.width, playingField.height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < playingField.height; y++) {
            for (int x = 0; x < playingField.width; x++) {
                bitmapPlayingField.setPixel(x, y, (x % 2 == 0) ? colorBackgroundLight : colorBackgroundDark);
            }
        }

        if (colorPoints != null) {
            for (PlayingField.ColorPoint colorPoint : colorPoints) {
                if ((colorPoint.point.x >= 0) && (colorPoint.point.x < playingField.width) && (colorPoint.point.y >= 0) && (colorPoint.point.y < playingField.height)) {
                    bitmapPlayingField.setPixel(colorPoint.point.x, colorPoint.point.y, colorPoint.color);
                }
            }
        }
        final BitmapDrawable bd = new BitmapDrawable(getResources(), bitmapPlayingField);
        bd.setAntiAlias(false);
        bd.setDither(false);
        bd.setFilterBitmap(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageViewPlayingField.setImageDrawable(bd);
                imageViewPlayingField.invalidate();
            }
        });
    }

    void displayNextPart(Part part) {
        if (part != null) {
            int maxPartWidth = 0;
            int maxPartHeight = 0;
            for (int i = 0; i < Part.Manager.getPartCount(); i++) {
                Part check = Part.Manager.getPart(i);
                if (maxPartWidth < check.getWidth()) {
                    maxPartWidth = check.getWidth();
                }
                if (maxPartHeight < check.getHeight()) {
                    maxPartHeight = check.getHeight();
                }
            }


            if ((maxPartWidth - part.getWidth()) % 2 != 0) {
                if ((maxPartWidth - part.getWidth()) / 2 > 0) {
                    maxPartWidth -= 1;
                } else {
                    maxPartWidth += 1;
                }
            }
            if ((maxPartHeight - part.getHeight()) % 2 == 0) {
                if ((maxPartHeight - part.getHeight()) / 2 > 0) {
                    maxPartHeight -= 1;
                } else {
                    maxPartHeight += 1;
                }
            }
            maxPartHeight += 1;
            bitmapNextPart = Bitmap.createBitmap(maxPartWidth, maxPartHeight, Bitmap.Config.ARGB_8888);
            for (Point point : part.getPoints()) {
                bitmapNextPart.setPixel(maxPartWidth / 2 - part.getWidth() / 2 + point.x + playingField.getPartNext().getWidth() / 2, maxPartHeight / 2 - part.getHeight() / 2 + point.y, part.color);
            }
        } else {
            bitmapNextPart = Bitmap.createBitmap(3, 3, Bitmap.Config.ARGB_8888);
        }
        final BitmapDrawable bd = new BitmapDrawable(getResources(), bitmapNextPart);
        bd.setAntiAlias(false);
        bd.setDither(false);
        bd.setFilterBitmap(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageViewNextPart.setImageDrawable(bd);
                imageViewNextPart.invalidate();
            }
        });
    }

    @Override
    public void onLose() {
        newGame = true;
        pauseGame();
    }

    @Override
    public void onLineCompleted(int amount) {
        linesCompleted += amount;
        int temp = linesCompleted / 10 + firstLevel + 1;
        switch (amount) {
            case 1: {
                score += 40 * temp;
                break;
            }
            case 2: {
                score += 100 * temp;
                break;
            }
            case 3: {
                score += 300 * temp;
                break;
            }
            case 4: {
                score += 1200 * temp;
                break;
            }
        }
        displayLines(linesCompleted);
        displayLevel(firstLevel + linesCompleted / 10);
        displayScore(score);
    }

    public void onRotateClick(View view) {
        skipCountDown();
        playingField.rotate(Part.RotationDirection.RIGHT);
        draw(newGame);
    }

    public void onDropClick(View view) {
        skipCountDown();
        dropping = !dropping;
        if (dropping) {
            imageButtonQuickTick.setEnabled(false);
            imageViewPlayingField.setNextTick(PlayingFieldView.getTime());
        }
        else {
            imageButtonQuickTick.setEnabled(true);
        }
    }

    public void onQuickTickClick(View view) {
        skipCountDown();
        imageViewPlayingField.setNextTick(PlayingFieldView.getTime());
        score += quickTickBonus;
        displayScore(score);
        draw(newGame);
    }

    public void onLeftClick(View view) {
        skipCountDown();
        playingField.move(-1);
        draw(newGame);
    }

    public void onRightClick(View view) {
        skipCountDown();
        playingField.move(1);
        draw(newGame);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            event.getKeyCode();
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_SPACE: {
                    if (gameRunning || countDownTimer != null) {
                        pauseGame();
                    } else {
                        startGame(!newGame);
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_D:
                case KeyEvent.KEYCODE_DPAD_RIGHT: {
                    if (imageButtonRight.isEnabled()) {
                        onRightClick(null);
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_A:
                case KeyEvent.KEYCODE_DPAD_LEFT: {
                    if (imageButtonLeft.isEnabled()) {
                        onLeftClick(null);
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_W:
                case KeyEvent.KEYCODE_DPAD_UP: {
                    if (imageButtonRotate.isEnabled()) {
                        onRotateClick(null);
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_S:
                case KeyEvent.KEYCODE_DPAD_DOWN: {
                    if (imageButtonQuickTick.isEnabled()) {
                        onQuickTickClick(null);
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_TAB:
                case KeyEvent.KEYCODE_ENTER: {
                    if (imageButtonDrop.isEnabled()) {
                        onDropClick(null);
                    }
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();

        setNavigationBarVisibility(false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menuItemPauseResume: {
                if (gameRunning || countDownTimer != null) {
                    pauseGame();
                } else {
                    startGame(!newGame);
                }
                return true;
            }
            case R.id.menuItemReset: {
                if (gameRunning || !newGame) {
                    pauseGame();

                    new AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_reset_title)
                            .setMessage(R.string.dialog_reset)
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    startGame(true);
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    setNavigationBarVisibility(false);
                                }
                            })
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    newGame = true;
                                    pauseGame();
                                }
                            })
                            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startGame(true);
                                }
                            })
                            .create().show();
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}