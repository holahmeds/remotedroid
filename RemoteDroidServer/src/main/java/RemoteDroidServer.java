import java.awt.AWTException;
import java.net.SocketException;

final class RemoteDroidServer {

    private RemoteDroidServer() {
    }

    public static void main(String[] args) {
        AppFrame frame = new AppFrame();
        OSCWorld world = new OSCWorld();
        try {
            world.onEnter();
            frame.setVisible(true);
        } catch (AWTException | SocketException e) {
            System.out.println("Error starting app.");
            e.printStackTrace();
        }
    }
}