package com.example.ravi.chatapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by ravi on 10/29/2017.
 */

public class ChatMessageAdapter extends ArrayAdapter {
    Context context;
    ArrayList<Message> messages;
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if(messages.get(position).isVideo == false) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.chatmessage, null);

            LinearLayout messageGravity = view.findViewById(R.id.chatMessages_layout_parent);
            if (messages.get(position).chkMine) {
                messageGravity.setGravity(Gravity.RIGHT);
            } else {
                messageGravity.setGravity(Gravity.LEFT);
            }
            TextView message = view.findViewById(R.id.message_text);
            //printMessage(position);
            message.setText(messages.get(position).content);
        }
        else{
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.videomessage, null);

            LinearLayout messageGravity = view.findViewById(R.id.videoMessages_layout_parent);
            if (messages.get(position).chkMine) {
                messageGravity.setGravity(Gravity.RIGHT);
            } else {
                messageGravity.setGravity(Gravity.LEFT);
            }
            final ImageButton message = view.findViewById(R.id.message_video);
            message.setVisibility(View.VISIBLE);
            //printMessage(position);
            message.setImageBitmap(ThumbnailUtils.createVideoThumbnail(messages.get(position).content,
                    MediaStore.Video.Thumbnails.MINI_KIND));

            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messages.get(position).content));
                    intent.setDataAndType(Uri.parse(messages.get(position).content), "video/*");
                    context.startActivity(intent);

                }
            });
        }
        return view;
    }

    public void printMessage(int position){
        Log.e("Message",messages.get(position).content);
    }

    public void add(Message m){
        messages.add(m);
        notifyDataSetChanged();
    }

    public ChatMessageAdapter(@NonNull Context context,ArrayList<Message> messages) {
        super(context, 0,messages);
        this.messages = messages;
        this.context = context;
    }
}
