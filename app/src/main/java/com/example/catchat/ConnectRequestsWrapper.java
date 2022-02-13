package com.example.catchat;

import android.content.Context;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * A wrapper around the ConnectRequestAdapter and its related data
 */
public class ConnectRequestsWrapper {
    private ArrayList<ConnectRequest> requests;  // the requests to show in the list
    private ConnectRequestsAdapter adapter;

    /**
     * Creates a new ConnectRequestWrapper.
     * Initializes the requests array to an empty array.
     */
    public ConnectRequestsWrapper() {
        requests = new ArrayList<>();
    }

    /**
     * Creates this instance's adapter.
     * @param context
     * @param activity the activity that created the adapter
     */
    public void createAdapter(Context context, ConnectActivity activity) {
        adapter = new ConnectRequestsAdapter(context, requests, activity);
    }

    /**
     * @return the adapter
     */
    public ConnectRequestsAdapter getAdapter() {
        return adapter;
    }

    /**
     * Adds a new request to the list.
     * @param sock the request socket
     * @return the created ConnectRequest
     */
    public ConnectRequest addRequest(Socket sock) throws IOException {
        ConnectRequest request = new ConnectRequest(sock);
        requests.add(request);
        adapter.notifyDataSetChanged();

        return request;
    }

    /**
     * Closes a connection request and removes it from the ListView.
     */
    public void removeRequest(ConnectRequest request) {
        request.close();

        // remove it from the list
        requests.remove(request);
        adapter.notifyDataSetChanged();
    }

    /**
     * Closes all connection requests and removes them from the ListView.
     * Does not close Globals.sock.
     */
    public void removeAll() {
        // close all sockets except the global one
        for (ConnectRequest request: requests) {
            if (request.getSocket() != Globals.sock ) request.close();
        }

        // clear array
        requests.clear();
        adapter.notifyDataSetChanged();
    }
}
