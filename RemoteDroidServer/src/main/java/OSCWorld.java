import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortIn;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * 
 * @author jsera
 * 
 * 
 */

public class OSCWorld {
    //
    private static final float sensitivity = 1.6f;

	private Robot robot;

    private Rectangle[] gBounds;

	private int scrollMod = -1;
	//
	private float xLeftover = 0; //for subpixel mouse accuracy
	private float yLeftover = 0; //for subpixel mouse accuracy

	public void onEnter() {
		try {
			this.robot = new Robot();
			this.robot.setAutoDelay(5);

            OSCPortIn receiver;
            receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
            OSCListener listener = new OSCListener() {
                public void acceptMessage(java.util.Date time, OSCMessage message) {
                    Object[] args = ((String)message.getArguments().get(0)).split(":");
					if (args.length == 3) {
						mouseEvent(Integer.parseInt(args[0].toString()), Float.parseFloat(args[1]
								.toString()), Float.parseFloat(args[2].toString()));
					}
				}
			};
            receiver.addListener("/mouse", listener);
            //
            listener = new OSCListener() {
                public void acceptMessage(java.util.Date time, OSCMessage message) {
					Object[] args = message.getArguments().toArray();
					if (args.length == 1) {
						buttonEvent(Integer.parseInt(args[0].toString()), 0);
					}
				}
			};
            receiver.addListener("/leftbutton", listener);
            //
            listener = new OSCListener() {
                public void acceptMessage(java.util.Date time, OSCMessage message) {
					Object[] args = message.getArguments().toArray();
					if (args.length == 1) {
						buttonEvent(Integer.parseInt(args[0].toString()), 2);
					}
				}
			};
            receiver.addListener("/rightbutton", listener);

			listener = new OSCListener() {
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					Object[] args = message.getArguments().toArray();
					if (args.length == 1) {
						scrollEvent(Integer.parseInt(args[0].toString()));
					}
				}
			};
            receiver.addListener("/wheel", listener);

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
			// discoverable stuff
            DiscoverableThread discoverable = new DiscoverableThread(OSCPort.defaultSCOSCPort() + 1);
            discoverable.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}

	private void mouseEvent(int type, float xOffset, float yOffset) {
		if (type == 2) {
			PointerInfo info = MouseInfo.getPointerInfo();
			if (info != null) {
				java.awt.Point p = info.getLocation();
				//for sub-pixel mouse accuracy, save leftover rounding value
				float ox = (xOffset * sensitivity) + xLeftover;
				float oy = (yOffset * sensitivity) + yLeftover;				
				int ix = Math.round(ox);
				int iy = Math.round(oy);
				xLeftover = ox-ix;
				yLeftover = oy-iy;
				//
				p.x += ix;
				p.y += iy;
                for (Rectangle r : gBounds) {
                    if (r.contains(p)) {
                        this.robot.mouseMove(p.x, p.y);
                        break;
                    }
				}
				
				try{
					this.robot.mouseMove(p.x, p.y);//for systems with quirky bounds checking, allow mouse to move smoothly along to and left edges
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

	private void buttonEvent(int type, int button) {
		if (button == 0) {
			button = InputEvent.BUTTON1_MASK;
		} else if (button == 2) {
			button = InputEvent.BUTTON3_MASK;
		}
		switch (type) {
		case 0:
			//
			this.robot.mousePress(button);
			this.robot.waitForIdle();
			break;
		case 1:
			//
			this.robot.mouseRelease(button);
			this.robot.waitForIdle();
			break;
		}
	}

	private void scrollEvent(int dir) {
		this.robot.mouseWheel(-dir * this.scrollMod);
	}

}