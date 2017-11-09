package com.example.ravi.chatapp;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by ravi on 10/29/2017.
 */


public class ChatSocket {

    static private com.github.nkzawa.socketio.client.Socket mSocket = null;
    public volatile boolean ack = false;



    ChatSocket(URI uri) {

        try {
            mSocket = IO.socket(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageNodeJs(Message m) throws JSONException {

        JSONObject messageJSON = new JSONObject();

        messageJSON.put("sender",m.sender);
        messageJSON.put("receiver",m.receiver);
        messageJSON.put("userID",m.userID);
        messageJSON.put("content",m.content);

        mSocket.emit("Message",messageJSON.toString());
    }

    public void sendDetails(String name, String userID) throws JSONException {
        JSONObject userInfo = new JSONObject();
        userInfo.put("name",name);
        userInfo.put("userID",userID);
        mSocket.emit("details",userInfo.toString());
    }
    public Message getMessage(JSONObject m) throws JSONException {
        Message receivedMessage;

        receivedMessage = new Message(false,m.getString("sender"),
                m.getString("receiver"),m.getString("userID"),m.getString("content"),false);
        return receivedMessage;
    }

    public void addVideo(JSONObject message){
        //append the message to already existing video
        boolean appendFile = true;

        try {
            if((int)message.get("index") == 0){
                appendFile = false;
            }
            File videoFile = getFile((String) message.get("name"));
            FileOutputStream outputStream = new FileOutputStream(videoFile,appendFile);
            String data = (String) message.get("data");
            byte[] byteData = Base64.decode(data,Base64.DEFAULT);
            outputStream.write(byteData);
            outputStream.close();
            sendIndexAck(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendIndexAck(JSONObject message) throws JSONException {

        JSONObject ack = new JSONObject();
        ack.put("name",message.get("name"));
        ack.put("index",(int)message.get("index"));
        ack.put("receiver",message.get("sender"));
        mSocket.emit("videoIndexAck",ack.toString());
    }

    public void sendVideoAck(String sender,String receiver,String userID,String video) throws JSONException {

        JSONObject message = new JSONObject();
        message.put("sender",sender);
        message.put("receiver",receiver);
        message.put("userID",userID);
        message.put("video",video);

        mSocket.emit("videoAck",message.toString());

    }

    public File getFile(String name) throws IOException {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),name);

        Log.e("Path",file.getPath());
        Log.e("name",name);
        if(!file.exists()){
            file.createNewFile();
        }

        return file;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void sendVideoStream(String sender, String receiver, String userID,
                                Uri uri, final Activity activity) throws IOException, JSONException {
        Pair parameters = VideoMetaData(uri,activity);

        InputStream inputStream = activity.getContentResolver().openInputStream(uri);

        int bufferSize = 1000*1024;
        byte[] buffer = new byte[bufferSize];

        int size = 0;
        int index = 0;
        while((size = inputStream.read(buffer)) != -1){
            String base64String;
            if(size == bufferSize) {
                base64String = Base64.encodeToString(buffer,Base64.DEFAULT);
            }
            else{
                base64String = Base64.encodeToString(Arrays.copyOf(buffer,size),Base64.DEFAULT);
            }
            JSONObject videoData = new JSONObject();
            videoData.put("sender",sender);
            videoData.put("receiver",receiver);
            videoData.put("userID",userID);
            videoData.put("name",parameters.first);
            videoData.put("size",parameters.second);
            videoData.put("len",size);
            videoData.put("index",index);
            videoData.put("data",base64String);

            ack = false;
            mSocket.emit("VideoMessage", videoData.toString());
            Log.e("ACK","False");
            long previosuTimeMillis= System.currentTimeMillis();
            long currentTimeMillis = System.currentTimeMillis();
            while(!ack){
                if(currentTimeMillis-previosuTimeMillis >= 10000){
                    Log.e("Server Time-out","Did not receive message");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "Error occured while sending", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                currentTimeMillis = System.currentTimeMillis();

            }
            Log.e("ACK","True");
            index+= 1;
        }

        sendVideoAck(sender,receiver,userID, (String) parameters.first);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Pair<String,String> VideoMetaData(Uri uri, Activity activity) {

        Cursor cursor = activity.getContentResolver()
                .query(uri, null, null, null, null, null);

        String displayName = null;
        String size = null;
        try {
            if (cursor != null && cursor.moveToFirst()) {

                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.e("Display Name", "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                if (!cursor.isNull(sizeIndex)) {
                    size = cursor.getString(sizeIndex);
                }
                Log.e("Size", "Size: " + size);
            }
        } finally {
            cursor.close();
        }

        return new Pair<>(displayName,size);
    }

    public com.github.nkzawa.socketio.client.Socket getSocket(){

        return mSocket;
    }

    public void ackReceived(){
        ack = true;
    }
}


