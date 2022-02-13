package com.example.catchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Thread that connects to a web server returning the IP address of the caller.
 * Used to display the device's IP on the network on the connection screen
 */
public class IpFinderThread extends Thread {

    // the server we are getting the IP from
    private static final String address = "https://myip.dnsomatic.com/";

    private ConnectActivity activity = null;  // thread started by

    /**
     * Creates a new IpFinderThread instance.
     * @param activity the activity that started this thread
     */
    public IpFinderThread(ConnectActivity activity) {
        this.activity = activity;
    }

    /**
     * Gets the IP address and calls activity.setIp(ip)
     */
    @Override
    public void run() {
        int attemptNumber = 3;  // how many times to attempt getting IP address

        String ip = null;
        while (attemptNumber > 0 && ip == null) {
            // make request to a server that returns my ip address
            try {
                URL url = new URL(address);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                ip = in.readLine();
            } catch (IOException e) {
                attemptNumber--;
                // try again
            }
        }

        // tell activity to set the ipContainer text to this ip
        activity.setIp(ip == null ? "Could not get IP" : ip);
    }
}
