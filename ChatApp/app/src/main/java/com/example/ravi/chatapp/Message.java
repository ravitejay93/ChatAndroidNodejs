package com.example.ravi.chatapp;

/**
 * Created by ravi on 10/29/2017.
 */

public class Message {

    public String content;
    public String sender;
    public String receiver;
    public String userID;
    boolean chkMine;
    boolean isVideo;

    public Message(boolean chkMine,String sender,String receiver,String userID,String content,boolean isVideo){
        this.chkMine = chkMine;
        this.sender = sender;
        this.receiver = receiver;
        this.userID = userID;
        this.content = content;
        this.isVideo = isVideo;
    }
}
