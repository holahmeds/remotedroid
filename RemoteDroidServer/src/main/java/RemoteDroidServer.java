/**
 * to-do:
 * add mouse sensitivity scroller
 */


public class RemoteDroidServer {
    private static AppFrame frame;
    private static OSCWorld world;

    public static void main(String[] args) {
        frame = new AppFrame();
        frame.setVisible(true);

        System.out.println(System.getProperty("os.name"));

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