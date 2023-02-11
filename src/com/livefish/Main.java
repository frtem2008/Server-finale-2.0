package com.livefish;

import com.livefish.Online.Server;

/**
 * com.livefish.Main program class
 * Entry point
 *
 * @author NAUMENKO-ZHIVOY ARTEM
 * @version 1.0
 * @see Server#getInstance()
 */
public class Main {

    /**
     * Empty default constructor
     */
    public Main() {
    }

    /**
     * com.livefish.Main program method
     *
     * @param args command line arguments
     * @see Server#getInstance()
     */
    public static void main(String[] args) {
        Server.getInstance();
    }
}
