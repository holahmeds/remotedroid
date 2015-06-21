package com.joshsera;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author jsera
 */

public class PadActivity extends Activity {
    public static final String CONNECT_IP = "com.remotedroid.CONNECT_IP";
    //
    private static final int TAP_NONE = 0;
    private static final int TAP_FIRST = 1;
    private static final int TAP_SECOND = 2;
    private static final int TAP_DOUBLE = 3;
    private static final int TAP_DOUBLE_FINISH = 4;
    private static final int TAP_RIGHT = 5;
    private static final String TAG = "PadActivity";

    private OSCPortOut sender;
    // thread and graphics stuff
    private Handler handler = new Handler();
    //
    private FrameLayout flLeftButton;
    private boolean leftToggle = false;
    private Runnable rLeftDown;
    private Runnable rLeftUp;
    //
    private FrameLayout flRightButton;
    private boolean rightToggle = false;
    private Runnable rRightDown;
    private Runnable rRightUp;

    private float xHistory;
    private float yHistory;
    //
    private int lastPointerCount = 0;

    // toggles
    private boolean toggleButton = false;
    // tap to click
    private long lastTap = 0;
    private int tapState = TAP_NONE;
    private Timer tapTimer;
    // multitouch scroll
    // private float scrollX = 0f;
    private float scrollY = 0f;

    /**
     * Mouse sensitivity power
     */
    private double mMouseSensitivityPower;

    private static final float sScrollStepMax = 6f;
    private static final float sScrollStepMin = 45f;
    private static final float sScrollMaxSettingsValue = 100f;

    private float mScrollStep;// = 12f;

    private SharedPreferences prefs;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // UI runnables
        this.rLeftDown = new Runnable() {
            public void run() {
                drawButtonOn(flLeftButton);
            }
        };
        this.rLeftUp = new Runnable() {
            public void run() {
                drawButtonOff(flLeftButton);
            }
        };
        this.rRightDown = new Runnable() {
            public void run() {
                drawButtonOn(flRightButton);
            }
        };
        this.rRightUp = new Runnable() {
            public void run() {
                drawButtonOff(flRightButton);
            }
        };

        //
        try {
            //
            setContentView(R.layout.pad_layout);
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

            this.sender = new OSCPortOut(
                    (InetAddress) getIntent().getSerializableExtra(CONNECT_IP),
                    OSCPort.defaultSCOSCPort());

            this.initTouchpad();
            this.initLeftButton();
            this.initRightButton();
        } catch (Exception ex) {
            Log.d(TAG, ex.toString(), ex);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.pad_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_prefs:
                startActivity(new Intent(this, PadPreferences.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup accelerations
        mMouseSensitivityPower = 1
                + Integer.parseInt(prefs.getString(PadPreferences.SENSITIVITY, "0")) / 100d;
        mScrollStep = (sScrollStepMin - sScrollStepMax)
                * (sScrollMaxSettingsValue - Integer.parseInt(prefs.getString(PadPreferences.SCROLL_SENSITIVITY, "50")))
                / sScrollMaxSettingsValue
                + sScrollStepMax;

        Log.d(TAG, "mScrollStep=" + mScrollStep);
        Log.d(TAG, "Settings.sensitivity=" + prefs.getString(PadPreferences.SENSITIVITY, "0"));
    }

    private void initTouchpad() {
        FrameLayout fl = (FrameLayout) this.findViewById(R.id.flTouchPad);

        // let's set up a touch listener
        fl.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent ev) {
                return onMouseMove(ev);
            }
        });
    }

    private void initLeftButton() {
        FrameLayout fl = (FrameLayout) this.findViewById(R.id.flLeftButton);
        android.view.ViewGroup.LayoutParams lp = fl.getLayoutParams();
        fl.setLayoutParams(lp);
        // listener
        fl.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent ev) {
                return onLeftTouch(ev);
            }
        });
        this.flLeftButton = fl;
    }

    private void initRightButton() {
        FrameLayout iv = (FrameLayout) this.findViewById(R.id.flRightButton);
        android.view.ViewGroup.LayoutParams lp = iv.getLayoutParams();
        iv.setLayoutParams(lp);
        // listener
        iv.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent ev) {
                return onRightTouch(ev);
            }
        });
        this.flRightButton = iv;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.sender.close();
    }

    // mouse events
    boolean scrollTag = false;
    int scrollCount = 0;

    private boolean onMouseMove(MotionEvent ev) {
        int type = 0;
        float xMove = 0f;
        float yMove = 0f;

        int pointerCount = ev.getPointerCount();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // scrollX = 0;
                scrollY = 0;
                //
                if (prefs.getBoolean(PadPreferences.TAP_TO_CLICK, true)
                        && (pointerCount == 1)) {
                    if (this.tapState == TAP_NONE) {
                        // first tap
                        this.lastTap = System.currentTimeMillis();
                        //
                    } else if (this.tapState == TAP_FIRST) {
                        // second tap - check if we've fired the button up
                        if (this.tapTimer != null) {
                            // up has not been fired
                            this.tapTimer.cancel();
                            this.tapTimer = null;
                            this.tapState = TAP_SECOND;
                            this.lastTap = System.currentTimeMillis();
                        }
                    }
                }
                //
                type = 0;
                xMove = 0;
                yMove = 0;
                //
                this.xHistory = ev.getX();
                this.yHistory = ev.getY();
                //
                break;
            case MotionEvent.ACTION_UP:
                if (prefs.getBoolean(PadPreferences.TAP_TO_CLICK, true)
                        && (pointerCount == 1)) {
                    // it's a tap!
                    long now = System.currentTimeMillis();
                    long elapsed = now - this.lastTap;
                    if (elapsed <= Integer.parseInt(prefs.getString(PadPreferences.TAP_TIME, "200"))) {
                        if (this.tapState == TAP_NONE) {
                            this.lastTap = now;
                            //
                            this.tapTimer = new Timer();
                            this.tapTimer.scheduleAtFixedRate(new TimerTask() {
                                public void run() {
                                    firstTapUp();
                                }
                            }, 0, Integer.parseInt(prefs.getString(PadPreferences.TAP_TIME, "200")));
                        } else if (this.tapState == TAP_SECOND) {
                            // double-click
                            this.tapTimer = new Timer();
                            this.tapTimer.scheduleAtFixedRate(new TimerTask() {
                                public void run() {
                                    secondTapUp();
                                }
                            }, 0, 10);
                        }

                    } else {
                        // too long
                        this.lastTap = 0;
                        if (this.tapState == TAP_SECOND) {
                            // release the button
                            this.tapState = TAP_NONE;
                            this.lastTap = 0;
                            this.leftButtonUp();
                        }
                    }
                }
                //
                type = 1;
                xMove = 0;
                yMove = 0;
                //scrollX= 0;
                scrollY = 0;
                scrollTag = false; //clear multi-touch event
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerCount == 1) {
                    // move
                    type = 2;
                    if (lastPointerCount == 1) {
                        xMove = ev.getX() - this.xHistory;
                        yMove = ev.getY() - this.yHistory;
                    }
                    this.xHistory = ev.getX();
                    this.yHistory = ev.getY();
                } else if (pointerCount == 2) {
                    // multi-touch scroll
                    type = -1;

                    float posY = ev.getY(0);

                    // only consider the second pointer if I had a previous history
                    if (lastPointerCount == 2) {
                        posY += ev.getY(1);
                        posY /= 2;

                        // xMove = posX - this.xHistory;
                        yMove = posY - this.yHistory;
                    } else {
                        // xMove = posX - this.xHistory;
                        yMove = posY - this.yHistory;

                        posY += ev.getY(1);
                        posY /= 2;
                    }

                    // this.xHistory = posX;
                    this.yHistory = posY;
                }
                break;
        }
        if (type == -1) {
            // scrollX += xMove;
            scrollY += yMove;
            int dir = 0;
            // if (Math.abs(scrollX) > SCROLL_STEP) {
            // // can't deal with X scrolling yet
            // scrollX = 0f;
            // }
            if (Math.abs(scrollY) > mScrollStep) {
                if (scrollY > 0f) {
                    dir = 1;
                } else {
                    dir = -1;
                }

                if (prefs.getBoolean(PadPreferences.SCROLL_INVERTED, false)) {
                    dir = -dir;
                }

                scrollY = 0f;
            }
            if (scrollTag) scrollCount++;
            else scrollCount = 0;
            scrollTag = true; //flag multi touch state for next up event
            if (dir != 0)
                this.sendScrollEvent(dir); //lets only send scroll events if there is distance to scroll
        } else if (type == 2) {
            // if type is 0 or 1, the server will not do anything with it, so we
            // only send type 2 events
            this.sendMouseEvent(type, xMove, yMove);
        }
        lastPointerCount = pointerCount;
        return true;
    }

    private void firstTapUp() {
        this.leftToggle = false;
        if (this.tapState == TAP_NONE) {
            // single click
            // counts as a tap
            this.tapState = TAP_FIRST;
            this.leftButtonDown();
        } else if (this.tapState == TAP_FIRST) {
            this.leftButtonUp();
            this.tapState = TAP_NONE;
            this.lastTap = 0;
            this.tapTimer.cancel();
            this.tapTimer = null;
        } else if (this.tapState == TAP_RIGHT) {
            this.rightButtonUp();
            this.tapState = TAP_NONE;
            this.lastTap = 0;
            this.tapTimer.cancel();
            this.tapTimer = null;
        }
    }

    private void secondTapUp() {
        this.leftToggle = false;
        if (this.tapState == TAP_SECOND) {
            // mouse up
            this.leftButtonUp();
            this.lastTap = 0;
            this.tapState = TAP_DOUBLE;
        } else if (this.tapState == TAP_DOUBLE) {
            this.leftButtonDown();
            this.tapState = TAP_DOUBLE_FINISH;
        } else if (this.tapState == TAP_DOUBLE_FINISH) {
            this.leftButtonUp();
            this.tapState = TAP_NONE;
            this.tapTimer.cancel();
            this.tapTimer = null;
        }
    }

    // abstract mouse event

    private void sendMouseEvent(int type, float x, float y) {

        int xDir = x >= 0 ? 1 : -1;
        int yDir = y >= 0 ? 1 : -1;

        Object[] args = new Object[1];
        args[0] = String.valueOf(type)
                + ":" + String.valueOf(Math.pow(Math.abs(x), mMouseSensitivityPower) * xDir)
                + ":" + String.valueOf(Math.pow(Math.abs(y), mMouseSensitivityPower) * yDir);
        // Log.d(TAG, String.valueOf(Settings.getSensitivity()));
        //
        OSCMessage msg = new OSCMessage("/mouse", Arrays.asList(args));
        try {
            this.sender.send(msg);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString(), ex);
        }
    }

    private void sendScrollEvent(int dir) {
        Object[] args = new Object[1];
        args[0] = dir;
        //
        OSCMessage msg = new OSCMessage("/wheel", Arrays.asList(args));
        try {
            this.sender.send(msg);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString(), ex);
        }
    }

    private boolean onLeftTouch(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //
                if (!this.toggleButton) {
                    if (this.leftToggle) {
                        this.leftButtonUp();
                        this.leftToggle = false;
                    }
                    this.leftButtonDown();
                }
                break;
            case MotionEvent.ACTION_UP:
                //
                if (!this.toggleButton) {
                    this.leftButtonUp();
                } else {
                    if (this.leftToggle) {
                        this.leftButtonUp();
                    } else {
                        this.leftButtonDown();
                    }
                    this.leftToggle = !this.leftToggle;
                }
                break;
        }
        //
        return true;
    }

    private void leftButtonDown() {
        Object[] args = new Object[1];
        args[0] = 0;
        OSCMessage msg = new OSCMessage("/leftbutton", Arrays.asList(args));
        try {
            this.sender.send(msg);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString(), ex);
        }
        // graphical feedback
        this.handler.post(this.rLeftDown);
    }

    private void leftButtonUp() {
        Object[] args = new Object[1];
        args[0] = 1;
        OSCMessage msg = new OSCMessage("/leftbutton", Arrays.asList(args));
        try {
            this.sender.send(msg);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString(), ex);
        }
        // graphical feedback
        this.handler.post(this.rLeftUp);
    }

    private boolean onRightTouch(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //
                if (!this.toggleButton) {
                    if (this.rightToggle) {
                        this.rightButtonUp();
                        this.rightToggle = false;
                    }
                    this.rightToggle = false;
                    this.rightButtonDown();
                }
                break;
            case MotionEvent.ACTION_UP:
                //
                if (!this.toggleButton) {
                    this.rightButtonUp();
                } else {
                    // toggle magic!
                    if (this.rightToggle) {
                        this.rightButtonUp();
                    } else {
                        this.rightButtonDown();
                    }
                    this.rightToggle = !this.rightToggle;
                }
                break;
        }
        //
        return true;
    }

    private void rightButtonDown() {
        Object[] args = new Object[1];
        args[0] = 0;
        OSCMessage msg = new OSCMessage("/rightbutton", Arrays.asList(args));
        try {
            this.sender.send(msg);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString(), ex);
        }
        // graphical feedback
        this.handler.post(this.rRightDown);
    }

    private void rightButtonUp() {
        Object[] args = new Object[1];
        args[0] = 1;
        OSCMessage msg = new OSCMessage("/rightbutton", Arrays.asList(args));
        try {
            this.sender.send(msg);
        } catch (Exception ex) {
            Log.d(TAG, ex.toString(), ex);
        }
        // graphical feedback
        this.handler.post(this.rRightUp);
    }

    private void drawButtonOn(FrameLayout fl) {
        fl.setBackgroundResource(R.drawable.left_button_on);
    }

    private void drawButtonOff(FrameLayout fl) {
        fl.setBackgroundResource(R.drawable.left_button_off);
    }

}
