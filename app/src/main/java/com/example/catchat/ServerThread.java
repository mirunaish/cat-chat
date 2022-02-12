package com.example.catchat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server thread listening for connections
 */
public class ServerThread extends Thread {
    private ConnectActivity activity;  // created by

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
        ServerSocket listener;

        try {
            // try to accept a new connection
            listener = new ServerSocket(Globals.port);

            // listen forever
            while (true) {
                Socket heard;

                // listen for connections until one is accepted
                if ((heard = listener.accept()) != null) {
                    activity.addIncoming(heard);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
