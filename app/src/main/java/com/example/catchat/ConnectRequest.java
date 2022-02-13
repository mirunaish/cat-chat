package com.example.catchat;

import java.io.IOException;
import java.net.Socket;

/**
 * Holds data about a single incoming connection request.
 * Used by the ConnectRequestAdapter, which stores an ArrayList of ConnectRequests.
 */
public class ConnectRequest {
    private BetterSocket sock = null;  // the socket to the requester
    private CheckConnectionAliveThread thread = null;

    /**
     * Creates a new ConnectRequest instance.
     * @param sock the socket for this connection request
     */
    public ConnectRequest(Socket sock) throws IOException {
        this.sock = new BetterSocket(sock);
    }

    /**
     * @return the socket
     */
    public BetterSocket getSocket() {
        return sock;
    }

    /**
     * @return the caller's IP address
     */
    public String getIp() {
        return sock.getSocket().getRemoteSocketAddress().toString();
    }

    /**
     * Sets and starts the thread checking for socket closure
     * @param thread an unstarted CheckConnectionAliveThread
     */
    public void setThread(CheckConnectionAliveThread thread) {
        this.thread = thread;
        thread.setRequest(this);
        thread.start();
    }

    /**
     * Stops this request's CheckConnectionAliveThread.
     * Called when this connection is accepted, or another connection is accepted.
     */
    public void interruptThread() {
        if (thread != null) thread.interrupt();
    }

    /**
     * Closes this ConnectionRequest and interrupts its CheckConnectionAliveThread
     */
    public void close() {
        interruptThread();

        try {
            sock.destroy();
        } catch (IOException e) {
            e.printStackTrace();
            // socket was probably already closed
        }
    }
}
