package com.joshsera;

import java.io.Serializable;
import java.net.InetAddress;

public class Host implements Serializable {
    private String hostname;
    private InetAddress address;

    public Host(String s, InetAddress a) {
        hostname = s;
        address = a;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return getHostname();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Host)) {
            return false;
        } else {
            Host h = (Host) o;
            return h.hostname == this.hostname && h.address == this.address;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + hostname.hashCode();
        result = result * 31 + address.hashCode();
        return result;
    }
}
