import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

final class InetAddressUtil {

    private InetAddressUtil() {
    }

    /**
     * Returns an InetAddress representing the address of the localhost.
     * Every attempt is made to find an address for this host that is not
     * the loopback address.  If no other address can be found, the
     * loopback will be returned.
     *
     * @return InetAddress - the address of localhost
     * @throws UnknownHostException - if there is a problem determing the address
     * @throws SocketException - if an I/O error occurs
     */
    public static InetAddress getLocalHost() throws SocketException, UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        if (!localHost.isLoopbackAddress()) {
            return localHost;
        }

        ArrayList<InetAddress> addrs = InetAddressUtil.getAllLocalUsingNetworkInterface();
        for (InetAddress addr : addrs) {
            // ensure IPv4
            if (!addr.isLoopbackAddress() && (addr instanceof Inet4Address)) {
                return addr;
            }
        }

        return localHost;
    }

    /**
     * Utility method that delegates to the methods of NetworkInterface to
     * determine addresses for this machine.
     *
     * @return ArrayList<InetAddress> - all addresses found from the NetworkInterfaces
     * @throws SocketException - if an I/O error occurs
     */
    private static ArrayList<InetAddress> getAllLocalUsingNetworkInterface() throws SocketException {
        ArrayList<InetAddress> addresses = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            Enumeration<InetAddress> e2 = ni.getInetAddresses();
            while (e2.hasMoreElements()) {
                addresses.add(e2.nextElement());
            }
        }

        return addresses;
    }
}
