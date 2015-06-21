package com.joshsera;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

public class DiscoverThread extends Thread {
    //
    private static final String TAG = "DiscoverThread";
    private static final int BUFFER_LENGTH = 1024;
    public static final String MULTICAST_ADDRESS = "230.6.6.6";
    private static final String ID_REQUEST = "RemoteDroid:AnyoneHome";
    private static final String ID_REQUEST_RESPONSE = "RemoteDroid:ImHome";

    //
    private int port = 57111;
    private MulticastSocket socket;
    private DatagramSocket inSocket;
    private DiscoverListener listener;

    public DiscoverThread(DiscoverListener listener) {
        this.listener = listener;
    }

    public DiscoverThread(int port, DiscoverListener listener) {
        this.port = port;
        this.listener = listener;
    }

    public void run() {
        try {
            this.socket = new MulticastSocket(this.port);
            this.socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
            this.inSocket = new DatagramSocket(this.port + 1);
            this.sendIDRequest();
            this.waitForResponse();
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
    }

    public void closeSocket() {
        this.socket.close();
        this.inSocket.close();
    }

    private void sendIDRequest() throws IOException {
        byte[] b = ID_REQUEST.getBytes();
        DatagramPacket packet = new DatagramPacket(b, b.length);
        packet.setAddress(InetAddress.getByName(MULTICAST_ADDRESS));
        packet.setPort(this.port);
        this.socket.send(packet);
    }

    private void waitForResponse() throws IOException {
        byte[] b = new byte[BUFFER_LENGTH];
        DatagramPacket packet = new DatagramPacket(b, b.length);
        //Log.d(TAG, "Going to wait for packet");
        while (true) {
            this.inSocket.receive(packet);
            this.handleReceivedPacket(packet);
        }
    }

    //

    private void handleReceivedPacket(DatagramPacket packet) {
        String data = new String(packet.getData(), 0, packet.getLength(), Charset.defaultCharset());

        if (data.equals(ID_REQUEST_RESPONSE)) {
            // We've received a response. Notify the listener
            this.listener.onAddressReceived(packet.getAddress());
        }
    }

    public interface DiscoverListener {
        void onAddressReceived(InetAddress address);
    }
}
