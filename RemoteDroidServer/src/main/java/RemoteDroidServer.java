/**
 * TODO: add mouse sensitivity scroller
 */


public class RemoteDroidServer {

    public static void main(String[] args) {
        AppFrame frame = new AppFrame();
        frame.setVisible(true);

        OSCWorld world = new OSCWorld();
        world.onEnter();
    }
}