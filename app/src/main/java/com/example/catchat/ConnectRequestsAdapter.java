package com.example.catchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

/**
 * An adapter for the ConnectRequest ListView.
 * Handles list actions, such as adding and removing, as well as tasks such as accepting and
 * declining calls.
 *
 * Also acts as an OnClickListener for the buttons on each list element fragment.
 */
public class ConnectRequestsAdapter extends ArrayAdapter<ConnectRequest> implements View.OnClickListener {
    private Context context;
    private ConnectActivity activity;  // adapter belongs to

    // holds the ConnectRequest's views
    private static class RequestViewHolder {
        Button acceptButton;
        Button declineButton;
        TextView callerIp;
    }

    /**
     * Creates the adapter.
     * @param context
     * @param data the ArrayList of ConnectRequests
     * @param activity the activity that created this adapter
     */
    public ConnectRequestsAdapter(@NonNull Context context, ArrayList<ConnectRequest> data, ConnectActivity activity) {
        super(context, R.layout.connect_request, data);
        this.context = context;
        this.activity = activity;
    }

    /**
     * onClick handler for buttons on list element fragments (accept and decline)
     * @param view the view clicked
     */
    @Override
    public void onClick(View view) {
        int index = (int) view.getTag();  // get this ConnectionRequest's position in the array
        ConnectRequest request = getItem(index);
        if (view.getId() == R.id.accept_button) accept(request);
        if (view.getId() == R.id.decline_button) decline(request);
    }

    /**
     * Accepts a connection request. Sets Global.sock to this request's socket and lets the
     * requester know their request was accepted, then starts the call.
     * Called when the accept button is clicked
     * @param request the connection request to be accepted
     */
    private void accept(ConnectRequest request) {
        Globals.sock = request.getSocket();

        activity.startCall();
    }

    /**
     * Declines a connection request. Closes its socket and removes the request from the list
     * @param request the request to be declined
     */
    private void decline(ConnectRequest request) {
        request.close();
        remove(request);
        notifyDataSetChanged();
    }

    /**
     * Returns the view of one of the requests
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ConnectRequest request = getItem(position);
        RequestViewHolder views = new RequestViewHolder();

        if (convertView == null) {  // this connectionRequest is new
            // https://stackoverflow.com/questions/9469174/set-theme-for-a-fragment
            getContext().getTheme().applyStyle(R.style.Theme_CatChat, true);
            LayoutInflater inflater = LayoutInflater.from(this.context);  // get a layoutInflater to inflate the view
            convertView = inflater.inflate(R.layout.connect_request, parent, false);  // inflate view

            views.acceptButton = convertView.findViewById(R.id.accept_button);
            views.declineButton = convertView.findViewById(R.id.decline_button);
            views.callerIp = convertView.findViewById(R.id.caller_ip);
            convertView.setTag(views);  // for later retrieval
        }
        else {  // the request is not new; its RequestViewHolder has been created already
            views = (RequestViewHolder) convertView.getTag();  // the aforementioned later retrieval
        }

        views.acceptButton.setTag(position);  // remember the index of the request associated with this button
        views.acceptButton.setOnClickListener(this); // call this class's OnCLick method

        views.declineButton.setTag(position);
        views.declineButton.setOnClickListener(this);

        views.callerIp.setText(request.getIp());

        return convertView;
    }
}
