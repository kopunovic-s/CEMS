package com.example.companyloginapp;

public class ChatMessage {
    private String senderName;
    private String message;
    private String timestamp;

    // Constructor to initialize a ChatMessage object
    public ChatMessage(String senderName, String message, String timestamp) {
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getter methods
    public String getSenderName() { return senderName; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}
