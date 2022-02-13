package com.example.catchat;

import java.io.IOException;
import java.net.Socket;

/**
 * Client thread attempting connection to another device's server thread
 */
public class ClientThread extends Thread {
    ConnectActivity activity;  // thread running for
    String partnerIP;  // attempting connect to

    /**
     * Creates a new client thread.
     * @param activity the activity that started this thread (this)
     * @param ip ip of client attempting connection to
     */
    public ClientThread(ConnectActivity activity, String ip) {
        this.activity = activity;
        this.partnerIP = ip;
    }

    /**
     * Tries to establish a socket connection to partnerIP
     */
    public void run() {
        Socket sock = null;

        try {
            // create a socket to the other user
            sock = new Socket(partnerIP, Globals.port);
        } catch (IOException e) {
            // socket could not be created
            activity.connectionFailed("Connection failed.");
            return;
        }

        // try to create the global sock object, get input and output streams
        try {
            Globals.sock = new BetterSocket(sock);
        } catch (IOException e) {
            activity.connectionFailed("Connection failed.");
            return;
        }

        activity.updateStatus("Ringing...");

        // wait for other side to accept call
        if (awaitResponse()) {
            activity.startCall();
        } else if (!this.isInterrupted()) {
            activity.connectionFailed("Declined.");
        } // else, thread was interrupted (call cancelled), do nothing
    }

    /**
     * After the socket is established, waits for the partner to send a message.
     * The partner sends the connection message in ConnectRequestsAdapter::accept().
     * @return true if connection made
     *         false if declined / cancelled
     */
    private boolean awaitResponse() {
        int message;

        try {
            message = Globals.sock.readInt();
        } catch (IOException | NullPointerException e) {
            return false;
        }

        return message == Globals.acceptCall;
    }
}
