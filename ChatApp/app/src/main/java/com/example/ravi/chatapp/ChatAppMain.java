package com.example.ravi.chatapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ChatAppMain extends AppCompatActivity {
    EditText myMessage;
    ListView myChat;
    static public ChatMessageAdapter chatAdapter;
    static public ArrayList<Message> messages = new ArrayList<>();
    String[] s1 = {"ravi","teja"};
    String[] s2 = {"teja","ravi"};
    int s_index = 0;

    Uri selectedMediaUri = null;

    ChatSocket chatSocket;


    public ChatAppMain(){
        try {
            chatSocket = new ChatSocket(new URI("http://10.0.2.2:3000"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatSocket.getSocket().connect();


        try {
            chatSocket.sendDetails(s1[s_index],"0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_chat_app_main);

        myMessage = findViewById(R.id.messageEditText);
        ImageButton sendMyMessage = findViewById(R.id.sendMessageButton);
        ImageButton attachVideo = findViewById(R.id.sendVideoButton);
        myChat = findViewById(R.id.chatMessages);

        myChat.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        myChat.setStackFromBottom(true);

        chatAdapter = new ChatMessageAdapter(this,messages);

        myChat.setAdapter(chatAdapter);

        sendMyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Button","pressed");
                sendMessage(true,s1[s_index],s2[s_index],"1",String.valueOf(myMessage.getText()));
                myMessage.setText("");
            }
        });

        attachVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT,
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("video/*");
                startActivityForResult(pickIntent, 100);

            }
        });

        chatSocket.getSocket().on("textMessage",onTextMessage);
        chatSocket.getSocket().on("videoMessage",onVideoMessage);
        chatSocket.getSocket().on("videoIndexAck",onVideoIndexAck);
        chatSocket.getSocket().on("videoAck",onVideoAck);

    }

    public void sendMessage(boolean chkMine,String sender,String receiver,String userID,String content){

        Message myMessage = new Message(chkMine,sender,receiver,userID,content,false);
        chatAdapter.add(myMessage);
        try {
            chatSocket.sendMessageNodeJs(myMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onTextMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ChatAppMain.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Message receivedMessage = chatSocket.getMessage(new JSONObject((String)args[0]));
                        Log.e("reveived",receivedMessage.sender);
                        chatAdapter.add(receivedMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });

        }
    };

    private Emitter.Listener onVideoMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ChatAppMain.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject receivedMessage = new JSONObject((String)args[0]);
                        chatSocket.addVideo(receivedMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });

        }
    };

    private Emitter.Listener onVideoIndexAck = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            ChatAppMain.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("ack","true");
                    chatSocket.ackReceived();

                }

            });

        }
    };

    private Emitter.Listener onVideoAck = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ChatAppMain.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject receivedMessage = null;
                    try {
                        receivedMessage = new JSONObject((String)args[0]);
                        File videoFile = chatSocket.getFile((String) receivedMessage.get("video"));
                        chatAdapter.add(new Message(false,receivedMessage.getString("sender"),
                                receivedMessage.getString("receiver"),"1",videoFile.getAbsolutePath(),true));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == 100) {
            selectedMediaUri = data.getData();
            chatAdapter.add(new Message(true,s1[s_index],
                    s2[s_index],"1",selectedMediaUri.toString(),true));
            sendVideo send = new sendVideo(ChatAppMain.this,s1[s_index],s2[s_index]);
            send.execute(chatSocket,selectedMediaUri);

        }
    }



}
