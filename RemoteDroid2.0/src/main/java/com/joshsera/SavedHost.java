package com.joshsera;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by ahmed on 16/04/15.
 */
public class SavedHost implements Serializable {
    String hostname;
    InetAddress address;

    public SavedHost(String s, InetAddress a) {
        hostname = s;
        address = a;
    }
}
