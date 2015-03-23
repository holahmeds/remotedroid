
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * to-do:
 * add mouse sensitivity scroller
 */


public class RemoteDroidServer {
	private static AppFrame frame;
	private static OSCWorld world;

	public static void main(String[] args) {
		frame = new AppFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);;
				System.exit(0);
			}
		});
		frame.setVisible(true);

		System.out.println(System.getProperty("os.name"));

		if (SystemTray.isSupported()) {
			try {
				BufferedImage icon = 
						ImageIO.read(RemoteDroidServer.class.
								getResourceAsStream("icon.gif"));
				TrayIcon tray = new TrayIcon(
						icon.getScaledInstance(24, 24, Image.SCALE_DEFAULT));

				tray.addMouseListener(new MouseAdapter(){
					@Override
					public void mouseClicked(MouseEvent e) {
						if(frame.isVisible())
							frame.setVisible(false);
						else
							frame.setVisible(true);
					}
				});

				SystemTray.getSystemTray().add(tray);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowIconified(WindowEvent e) {
						frame.setVisible(false);
					}
				});
			} catch (AWTException e) {
				System.err.println("Enable to add tray to system tray.");
				e.printStackTrace();
			} catch (IOException e1) {
				System.err.println("Unable to load tray icon.\nTray disabled.");
				e1.printStackTrace();
			}
		}
		
		// The following used to be delayed by javax.swing.Timer
		// Not sure why. Switched to Thread.sleep
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			System.out.println("Woke up from sleep early.\nContinueing.");
		}
		world = new OSCWorld();
		world.onEnter();
	}
}