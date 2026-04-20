package com.example.businessidea.RecycleView;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.businessidea.Module.AiChatsMessage;
import com.example.businessidea.R;

import java.util.List;

public class AiChatAdapter extends RecyclerView.Adapter<AiChatAdapter.ChatViewHolder> {
    private List<AiChatsMessage> chatList;
    private Context context;

    public AiChatAdapter(Context context,List<AiChatsMessage> chatList) {
        this.chatList = chatList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) { // User message (right side)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_right, parent, false);
            view.findViewById(R.id.copy_icon).setVisibility(View.GONE);
        } else { // AI response (left side)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_left, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        AiChatsMessage message = chatList.get(position);
        holder.messageTextView.setText(message.getMessage());
        holder.copy_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Message", message.getMessage());
                clipboard.setPrimaryClip(clip);

            }
        });
        if (message.isWaitingMessage()) {
            holder.copy_icon.setVisibility(View.GONE);
        } else {
            // Show the copy icon only for received messages (left side)
            if (message.isUserMessage()) {
                holder.copy_icon.setVisibility(View.GONE); // Hide for sent messages
            } else {
                holder.copy_icon.setVisibility(View.VISIBLE); // Show for received messages
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).isUserMessage() ? 0 : 1; // 0 for user, 1 for AI
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
    public void updateMessages(List<AiChatsMessage> newMessages) {
        chatList.clear();
        chatList.addAll(newMessages);
        notifyDataSetChanged(); // Refresh RecyclerView

    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView copy_icon;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text);
            copy_icon = itemView.findViewById(R.id.copy_icon);
        }
    }
}
