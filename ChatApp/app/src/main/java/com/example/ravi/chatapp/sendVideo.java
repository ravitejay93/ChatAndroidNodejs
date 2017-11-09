package com.example.ravi.chatapp;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import org.json.JSONException;

import java.io.IOException;


/**
 * Created by ravi on 11/2/2017.
 */

public class sendVideo extends AsyncTask<Object,Void,Void> {

    Activity activity;
    String sender;
    String receiver;
    sendVideo(Activity activity, String sender, String receiver){
        this.activity = activity;
        this.sender = sender;
        this.receiver = receiver;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected Void doInBackground(Object... objects) {
        ChatSocket chatSocket = (ChatSocket)objects[0];
        Uri uri = (Uri)objects[1];
        try {
            chatSocket.sendVideoStream(sender,receiver,"1",uri,activity);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }
}
