package com.example.catchat;

import java.io.IOException;

/**
 * Tries to read an integer from the input stream. The other device sends an integer when it starts
 * the call, the read operation blocks until either this message is received (in which case it does
 * nothing), the thread is interrupted (because its associated connection request was removed in
 * removeAll) or -1 is returned (the socket on the other end is closed, in which case remove the
 * connectionRequest).
 */
public class CheckConnectionAliveThread extends Thread {
    ConnectActivity activity = null;  // running for
    ConnectRequest request = null;

    /**
     * Constructs a new thread
     * @param activity the ConnectActivity that started this thread
     */
    public CheckConnectionAliveThread(ConnectActivity activity) {
        this.activity = activity;
    }

    /**
     * Sets the request to check for closure, if this thread has not been started yet
     * @param request the ConnectRequest
     */
    public void setRequest(ConnectRequest request) {
        if (this.isAlive()) return;
        this.request = request;
    }

    /**
     * Wait until the stream ends (the socket on the other end was closed), this thread is
     * interrupted, or the other end of the socket sends a connection accepted message
     */
    @Override
    public void run() {
        try {
            // blocks until a message is sent, the thread is interrupted, or the socket is closed
            if (request.getSocket().readInt() != Globals.acceptCall) {
                throw new IOException("unexpected data");
            }
        } catch (IOException | NullPointerException e) {
            if (!this.isInterrupted())
                this.activity.removeIncoming(request);
            // else, do nothing
        }
        System.out.println("thread ended, no longer listening for input");
        request.getSocket().printStatus();
    }
}