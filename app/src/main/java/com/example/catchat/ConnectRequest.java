package com.example.catchat;

import java.io.IOException;
import java.net.Socket;

/**
 * Holds data about a single incoming connection request.
 * Used by the ConnectRequestAdapter, which stores an ArrayList of ConnectRequests.
 */
public class ConnectRequest {
    private Socket sock;  // the socket to the requester

    /**
     * Creates a new ConnectRequest instance.
     * @param sock the socket for this connection request
     */
    public ConnectRequest(Socket sock) {
        this.sock = sock;
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return sock;
    }

    /**
     * @return the caller's IP address
     */
    public String getIp() {
        return sock.getRemoteSocketAddress().toString();
    }

    /**
     * Closes this ConnectionRequest
     */
    public void close() {
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
            // socket was probably already closed
        }
    }
}
