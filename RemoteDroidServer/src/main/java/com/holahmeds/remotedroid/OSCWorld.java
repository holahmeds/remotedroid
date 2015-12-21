package com.holahmeds.remotedroid;

import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortIn;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.net.SocketException;

/**
 * @author jsera
 */

class OSCWorld {
    //
    private static final float sensitivity = 1.6f;

    private Robot robot;

    private Rectangle[] gBounds;

    private int scrollMod = -1;
    //
    private float xLeftover = 0; //for subpixel mouse accuracy
    private float yLeftover = 0; //for subpixel mouse accuracy

    public void onEnter() throws AWTException, SocketException {
        this.robot = new Robot();
        this.robot.setAutoDelay(5);

        OSCPortIn receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
        receiver.addListener("/mouse", (time, message) -> {
            Object[] args = ((String) message.getArguments().get(0)).split(":");
            if (args.length == 3) {
                this.mouseEvent(Integer.parseInt(args[0].toString()), Float.parseFloat(args[1]
                        .toString()), Float.parseFloat(args[2].toString()));
            }
        });
        receiver.addListener("/leftbutton", (time, message) -> {
            Object[] args = message.getArguments().toArray();
            if (args.length == 1) {
                this.buttonEvent(Integer.parseInt(args[0].toString()), 0);
            }
        });
        receiver.addListener("/rightbutton", (time, message) -> {
            Object[] args = message.getArguments().toArray();
            if (args.length == 1) {
                this.buttonEvent(Integer.parseInt(args[0].toString()), 2);
            }
        });
        receiver.addListener("/wheel", (time, message) -> {
            Object[] args = message.getArguments().toArray();
            if (args.length == 1) {
                this.scrollEvent(Integer.parseInt(args[0].toString()));
            }
        });
        receiver.startListening();

        GraphicsDevice[] gDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        int l = gDevices.length;
        this.gBounds = new Rectangle[l];
        for (int i = 0; i < l; ++i) {
            this.gBounds[i] = gDevices[i].getDefaultConfiguration().getBounds();
        }

        if (System.getProperty("os.name").compareToIgnoreCase("Mac OS X") == 0) {
            // hack for robot class bug.
            this.scrollMod = 1;
        }
    }

    private void mouseEvent(int type, float xOffset, float yOffset) {
        if (type == 2) {
            PointerInfo info = MouseInfo.getPointerInfo();
            if (info != null) {
                Point p = info.getLocation();
                //for sub-pixel mouse accuracy, save leftover rounding value
                float ox = (xOffset * OSCWorld.sensitivity) + this.xLeftover;
                float oy = (yOffset * OSCWorld.sensitivity) + this.yLeftover;
                int ix = Math.round(ox);
                int iy = Math.round(oy);
                this.xLeftover = ox - ix;
                this.yLeftover = oy - iy;
                //
                p.x += ix;
                p.y += iy;
                for (Rectangle r : this.gBounds) {
                    if (r.contains(p)) {
                        this.robot.mouseMove(p.x, p.y);
                        break;
                    }
                }

                try {
                    this.robot.mouseMove(p.x, p.y);//for systems with quirky bounds checking, allow mouse to move smoothly along to and left edges
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void buttonEvent(int type, int button) {
        int buttonMask = 0;
        if (button == 0) {
            buttonMask = InputEvent.BUTTON1_MASK;
        } else if (button == 2) {
            buttonMask = InputEvent.BUTTON3_MASK;
        }

        switch (type) {
            case 0:
                this.robot.mousePress(buttonMask);
                this.robot.waitForIdle();
                break;
            case 1:
                this.robot.mouseRelease(buttonMask);
                this.robot.waitForIdle();
                break;
            default:
                break;
        }
    }

    private void scrollEvent(int dir) {
        this.robot.mouseWheel(-dir * this.scrollMod);
    }

}