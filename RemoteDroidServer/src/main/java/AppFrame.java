import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


public class AppFrame extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Border BORDER = new EmptyBorder(5, 5, 5, 5);
    private static final String APP_NAME = "RemoteDroid Server";

    public AppFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle(APP_NAME);

        // get local IP
        String sHost;
        try {
            sHost = InetAddressUtil.getLocalHost().getHostAddress();
        } catch (UnknownHostException | SocketException ex) {
            sHost = "Error finding local IP.";
        }

        String textLines = "<html>The RemoteDroid server application is now running.<br><br>Your IP address is: " + sHost + "<br><br>Enter this IP address on the start screen of the<br>RemoteDroid application on your phone to begin.</html>";
        Image imLogo = getImage("icon.gif");

        JPanel contentPane = new JPanel();
        contentPane.setBorder(BORDER);
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(5, 5));
        setIconImage(imLogo);

        JPanel panel = new JPanel();
        panel.setBorder(BORDER);
        contentPane.add(panel, BorderLayout.NORTH);
        panel.setLayout(new BorderLayout(5, 5));

        JLabel lblLogo = new JLabel();
        lblLogo.setIcon(new ImageIcon(imLogo));
        panel.add(lblLogo, BorderLayout.WEST);

        JLabel lbAppName = new JLabel(APP_NAME);
        panel.add(lbAppName, BorderLayout.CENTER);

        JLabel lbtextLines = new JLabel(textLines);
        contentPane.add(lbtextLines, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });

        if (SystemTray.isSupported()) {
            try {
                BufferedImage icon =
                        ImageIO.read(RemoteDroidServer.class.
                                getResourceAsStream("icon.gif"));
                TrayIcon tray = new TrayIcon(icon);
                tray.setImageAutoSize(true);

                tray.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (isVisible())
                            setVisible(false);
                        else
                            setVisible(true);
                    }
                });

                SystemTray.getSystemTray().add(tray);
                addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowIconified(WindowEvent e) {
                        setVisible(false);
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

        pack();
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
}