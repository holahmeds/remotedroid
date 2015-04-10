import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

public class LinuxInetAddress {

    /**
     * Returns an InetAddress representing the address of the localhost.
     * Every attempt is made to find an address for this host that is not
     * the loopback address.  If no other address can be found, the
     * loopback will be returned.
     *
     * @return InetAddress - the address of localhost
     * @throws UnknownHostException - if there is a problem determing the address
     */
    public static InetAddress getLocalHost() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        if (!localHost.isLoopbackAddress()) return localHost;
        InetAddress[] addrs = getAllLocalUsingNetworkInterface();
        for (InetAddress addr : addrs) {
            //Check for "." to ensure IPv4
            if (!addr.isLoopbackAddress() && addr.getHostAddress().contains("."))
                return addr;
        }
        return localHost;
    }

    /**
     * This method attempts to find all InetAddresses for this machine in a
     * conventional way (via InetAddress).  If only one address is found
     * and it is the loopback, an attempt is made to determine the addresses
     * for this machine using NetworkInterface.
     *
     * @return InetAddress[] - all addresses assigned to the local machine
     * @throws UnknownHostException - if there is a problem determining addresses
     */
    public static InetAddress[] getAllLocal() throws UnknownHostException {
        InetAddress[] iAddresses = InetAddress.getAllByName("127.0.0.1");
        if (iAddresses.length != 1) return iAddresses;
        if (!iAddresses[0].isLoopbackAddress()) return iAddresses;
        return getAllLocalUsingNetworkInterface();
    }

    /**
     * Utility method that delegates to the methods of NetworkInterface to
     * determine addresses for this machine.
     *
     * @return InetAddress[] - all addresses found from the NetworkInterfaces
     * @throws UnknownHostException - if there is a problem determining addresses
     */
    private static InetAddress[] getAllLocalUsingNetworkInterface() throws UnknownHostException {
        ArrayList<InetAddress> addresses = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            throw new UnknownHostException("127.0.0.1");
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            for (Enumeration<InetAddress> e2 = ni.getInetAddresses(); e2.hasMoreElements(); ) {
                addresses.add(e2.nextElement());
            }
        }

        return addresses.toArray(new InetAddress[addresses.size()]);
    }
}
