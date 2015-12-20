package com.joshsera;

import java.io.Serializable;
import java.net.InetAddress;

class Host implements Serializable {
    private static final long serialVersionUID = 2603465075386381805L;

    private final String hostname;
    private final InetAddress address;

    Host(String s, InetAddress a) {
        super();
        this.hostname = s;
        this.address = a;
    }

    public String getHostname() {
        return this.hostname;
    }
    public InetAddress getAddress() {
        return this.address;
    }

    @Override
    public String toString() {
        return this.hostname + " ("  + this.address + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Host) {
            Host h = (Host) o;
            return h.hostname.equals(this.hostname) && h.address.equals(this.address);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = (result * 31) + this.hostname.hashCode();
        result = (result * 31) + this.address.hashCode();
        return result;
    }
}
