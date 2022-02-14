package com.example.catchat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server thread listening for connections
 */
public class ServerThread extends Thread {
    private ConnectActivity activity;  // created by
    private ServerSocket listener = null;

    /**
     * Creates a new server thread.
     * @param activity the activity that started this thread.
     */
    public ServerThread(ConnectActivity activity) {
        this.activity = activity;
    }

    /**
     * Listens for socket connections to this device's IP and Globals.port
     */
    @Override
    public void run() {

        try {
            // create the listener that accepts requests
            listener = new ServerSocket(Globals.port);
        } catch (IOException e) {
            e.printStackTrace();
            activity.updateStatus("Cannot receive connection requests");
            return;
        }

        // listen until interrupted
        while (!this.isInterrupted()) {
            Socket heard;

            // listen for connections
            try {
                heard = listener.accept();
            } catch (IOException e) {
                break;
            }

            if (heard != null) {
                activity.addIncoming(heard);
            }
        }

        // interrupt() should close the listener, but close it here again just in case
        try {
            // close the listener
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Interrupts the thread, closes the ServerSocket to unblock accept() operation
     */
    @Override
    public void interrupt() {
        super.interrupt();
        try {
            listener.close();
        } catch (IOException e) {
            // do nothing
        }
    }
}
