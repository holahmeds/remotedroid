import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.Timer;


public class AppFrame extends Frame {
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;

	public static InetAddress localAddr;
	
	//
	private String[] textLines = new String[6];
	//
	private Image imLogo;
	private Image imHelp;
	private Font fontTitle;
	private Font fontText;
	//
	private Timer timer;
	//
	private int height = 510;
	private int width = 540;
	//
	private OSCWorld world;
	//
	private String appName = "RemoteDroid Server R2"; //added R2 so that version 2 of client will not confuse users as R2 is not needed for all features, and a future Client v3.0 might still use R2/v2.0 of the server
	
	public AppFrame() {
		super();
		GlobalData.oFrame = this;
		this.setSize(this.width, this.height);
		
		//this.init();
		// get local IP
		String sHost = "";
		try {
			localAddr = InetAddress.getLocalHost();
			if (localAddr.isLoopbackAddress()) {
				localAddr = LinuxInetAddress.getLocalHost();
			}
			sHost = localAddr.getHostAddress();
		} catch (UnknownHostException ex) {
			sHost = "Error finding local IP.";
		}
		//
		this.textLines[0] = "The RemoteDroid server application is now running.";
		this.textLines[1] = "";
		this.textLines[2] = "Your IP address is: "+sHost;
		this.textLines[3] = "";
		this.textLines[4] = "Enter this IP address on the start screen of the";
		this.textLines[5] = "RemoteDroid application on your phone to begin.";
	}
	
	public Image getImage(String sImage) {
		Image imReturn = null;
		try {
			InputStream inputStream = getClass().getResourceAsStream(sImage);
			imReturn = ImageIO.read(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imReturn;
	}
	
	public void init() {
		imHelp = getImage("helpphoto.jpg");
		imLogo = getImage("icon.gif");
		this.fontTitle = new Font("Verdana", Font.BOLD, 16);
		this.fontText = new Font("Verdana", Font.PLAIN, 11);
		this.setBackground(Color.BLACK);
		this.setForeground(Color.WHITE);
		//
		this.timer = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				world = new OSCWorld();
				world.onEnter();
				//
				repaint();
				timer.stop();
			}
		});
		this.timer.start();
		
	}
	
	public void paint(Graphics g) {
		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.width, this.height);
		g.setColor(this.getForeground());
		//
		g.drawImage(this.imLogo, 10, 30, this);
		g.setFont(this.fontTitle);
		g.drawString(this.appName, 70, 55);
		//
		g.setFont(this.fontText);
		int startY = 90;
		int l = 6;
		for (int i = 0;i<l;++i) {
			g.drawString(this.textLines[i], 10, startY);
			startY += 13;
		}
		//
		g.drawImage(this.imHelp, 20, startY+10, this);
	}
	/*
	*/
}