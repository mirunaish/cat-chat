package com.example.catchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.net.Socket;

/**
 * Call Activity
 * Started when app is opened.
 * Started by CallActivity when a call ends.
 * Starts CallActivity once a connection is successfully made.
 * Starts a ServerThread and a ClientThread, which handle the connection.
 */
public class ConnectActivity extends AppCompatActivity {

    private ClientThread clientThread = null;
    // the server thread is in the Globals class

    private EditText ipInput = null;  // the text box for partner ip input
    private Button connectButton = null;
    private TextView statusText = null;  // to print messages to the user

    private TextView myIp= null;

    private ConnectRequestsWrapper requestWrapper = null;  // list of incoming connection requests

    /**
     * Sets button onClicks and starts a server thread
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        statusText = findViewById(R.id.status);

        // if the activity was started by CallActivity, update the status
        Bundle bundle;
        if ((bundle = getIntent().getExtras()) != null){
            String reason = (String) bundle.get(Globals.intentReason);
            updateStatus("Call ended: " + reason);
        }

        ipInput = findViewById(R.id.outbound_ip_input);
        connectButton = findViewById(R.id.connect_button);

        // setup request list
        requestWrapper = new ConnectRequestsWrapper();
        requestWrapper.createAdapter(getApplicationContext(), this);  // create the adapter inside the wrapper
        ListView listView = findViewById(R.id.connection_list);
        listView.setAdapter(requestWrapper.getAdapter());

        // if the server thread does not exist, create and start it
        if (Globals.serverThread == null) {
            Globals.serverThread = new ServerThread(this);
            Globals.serverThread.setDaemon(true);
            Globals.serverThread.start();
        }

        connectButton.setOnClickListener(v -> attemptConnection());

        // start a thread to find the ip
        myIp = findViewById(R.id.ip_container);
        new IpFinderThread(this).start();
    }

    /**
     * Shows a status message to the user, in the status box at the top of the screen.
     * @param message message to display
     */
    public void updateStatus(String message) {
        // only the UI thread can modify its views (change the text in the status box)
        // https://stackoverflow.com/questions/16466521/modify-view-from-a-different-thread
        runOnUiThread(() -> statusText.setText(message));
    }

    /**
     * Displays this device's IP address on the screen.
     * Called by IpFinderThread once it finds the IP
     * @param ip a String representation of the device IP address
     */
    public void setIp(String ip) {
        runOnUiThread(() -> myIp.setText(ip));
    }

    /**
     * Starts a ClientThread; turns the connect button into a cancel button
     */
    private void attemptConnection() {
        connectButton.setText(R.string.cancel_button);
        connectButton.setOnClickListener(v -> connectionFailed("Cancelled."));
        updateStatus("Connecting...");

        clientThread = new ClientThread(this, ipInput.getText().toString());
        clientThread.start();
    }

    /**
     * The connection failed or was cancelled.
     * Stops the ClientThread and turns the cancel button back into a connect button.
     * @param reason a String describing the reason for failure; will be printed to the status box
     */
    public void connectionFailed(String reason) {
        if (clientThread != null && !clientThread.isInterrupted()) clientThread.interrupt();

        // https://stackoverflow.com/questions/16466521/modify-view-from-a-different-thread
        runOnUiThread(() -> {
            connectButton.setText(R.string.connect_button);
            connectButton.setOnClickListener(v -> attemptConnection());
            updateStatus(reason);
        });
    }

    /**
     * Adds an incoming connection request to the ListView.
     * Called by server thread when a socket is created.
     * Always runs on main UI thread.
     * @param sock the socket connection to the requester
     */
    public void addIncoming(Socket sock) {
        // https://stackoverflow.com/questions/16466521/modify-view-from-a-different-thread
        runOnUiThread(() -> requestWrapper.addRequest(sock));
    }

    /**
     * Starts the call activity after a connection is established.
     * Globals.sock must be set to a valid socket at the time of calling.
     */
    public void startCall() {
        // remove all incoming connection requests
        runOnUiThread(() -> requestWrapper.removeAll());

        // start the call activity
        Intent intent = new Intent(this, CallActivity.class);
        startActivity(intent);
    }
}