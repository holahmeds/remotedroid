import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

public class DiscoverableThread extends Thread {

	private static final int BUFFER_LENGTH = 1024;
	public static final String MULTICAST_ADDRESS = "230.6.6.6";
	private static final String ID_REQUEST = "RemoteDroid:AnyoneHome";
	private static final String ID_REQUEST_RESPONSE = "RemoteDroid:ImHome";
	
	//
	private int port = 57111;

	public DiscoverableThread(int port) {
		this.port = port;
	}
	
	public void run() {
		try {
			byte[] b = new byte[BUFFER_LENGTH];
			DatagramPacket packet = new DatagramPacket(b, b.length);
			MulticastSocket socket = new MulticastSocket(this.port);
			socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
			while (true) {
				socket.receive(packet);
				this.handlePacket(packet);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void handlePacket(DatagramPacket packet) throws IOException, InterruptedException {
		String data = new String(packet.getData(), 0, packet.getLength(), Charset.defaultCharset());

		if (data.equals(ID_REQUEST)) {
			byte[] b = ID_REQUEST_RESPONSE.getBytes();
			DatagramPacket p = new DatagramPacket(b, b.length);
			p.setAddress(packet.getAddress());
			p.setPort(this.port + 1);

			DatagramSocket outSocket = new DatagramSocket();
			outSocket.send(p);
			outSocket.close();
		}
	}
}
