package com.example.catchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

/**
 * Call Activity
 * Started by ConnectActivity when a connection is successfully established.
 * Starts ConnectActivity if error or when the connection ends.
 * Starts an InCommThread and an OutCommThread, which handle all the network communications as well
 * as recording and playing audio.
 */
public class CallActivity extends AppCompatActivity {

    private InCommThread inth;
    private OutCommThread outth;

    /**
     * Starts the two communication threads
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        findViewById(R.id.hang_up_button).setOnClickListener(v -> endCall("hung up."));

        inth = new InCommThread(this);
        outth = new OutCommThread(this);

        inth.start();
        outth.start();
    }

    /**
     * Ends the call and switches back to the connect activity
     * @param reason a string representing the reason, to be printed on the screen
     */
    public void endCall(String reason) {
        // end the communication threads
        if (inth != null) inth.interrupt();
        if (outth != null) outth.interrupt();

        // destroy the socket connection
        try {
            Globals.sock.destroy();
            Globals.sock = null;
        } catch (IOException | NullPointerException e) {
            System.out.println("socket is already destroyed");
        }

        // restart connect activity
        // https://developer.android.com/training/basics/firstapp/starting-activity
        Intent intent = new Intent(this, ConnectActivity.class);
        intent.putExtra(Globals.intentReason, reason); // tell activity why the call was ended
        startActivity(intent);
    }
}
