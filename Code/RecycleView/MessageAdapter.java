package com.example.businessidea.RecycleView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.businessidea.Activity.MessageActivity;
import com.example.businessidea.Module.Chats;
import com.example.businessidea.Module.Idea;
import com.example.businessidea.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<Chats> mChat;
    String imageUrl;
FirebaseUser firebaseUser;
    public MessageAdapter(Context context,List<Chats> mChat,String imageUrl) {
        this.mChat = mChat;
        this.context = context;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_right, parent, false);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.item_left, parent, false);
        }
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageHolder holder, int position) {
        Chats chats = mChat.get(position);
        holder.message_text.setText(chats.getMessage());
        if (imageUrl != null && !imageUrl.isEmpty()){
            Glide.with(context).load(imageUrl).transform(new CircleCrop()).into(holder.Chat_Image_User);
        }
        holder.icon.setVisibility(View.GONE);
        // Allow deleting only if the message is from the current user (right side)
        if (getItemViewType(position) == MSG_TYPE_RIGHT) {
            holder.itemView.setOnLongClickListener(v -> {
                showDeleteDialog(chats.getMessageId(), position);
                return true;
            });
        }
    }

    private void showDeleteDialog(String messageId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteMessage(messageId, position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteMessage(String messageId, int position) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(messageId);
        chatRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    mChat.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }
    public class MessageHolder extends RecyclerView.ViewHolder{
        ImageView Chat_Image_User,icon;
        TextView message_text;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            Chat_Image_User = itemView.findViewById(R.id.Chat_Image_User);
            message_text = itemView.findViewById(R.id.message_text);
            icon = itemView.findViewById(R.id.copy_icon);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }
}

