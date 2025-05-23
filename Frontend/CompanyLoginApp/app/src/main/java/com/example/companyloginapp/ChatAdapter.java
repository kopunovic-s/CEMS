package com.example.companyloginapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private final List<ChatMessage> messages;

    // Constructor
    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    // ViewHolder class to hold references to the views
    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatSender, chatMessage, chatTimestamp;

        ChatViewHolder(View itemView) {
            super(itemView);
            chatSender = itemView.findViewById(R.id.chat_sender);
            chatMessage = itemView.findViewById(R.id.chat_message);
            chatTimestamp = itemView.findViewById(R.id.chat_timestamp);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout from the XML file
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        // Get the current chat message
        ChatMessage message = messages.get(position);

        // Set the sender, message, and timestamp in the corresponding TextViews
        holder.chatSender.setText(message.getSenderName());
        holder.chatMessage.setText(message.getMessage());
        holder.chatTimestamp.setText(message.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // This method can be used to add a new message to the chat
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);  // Notify that a new message was added
    }
}
