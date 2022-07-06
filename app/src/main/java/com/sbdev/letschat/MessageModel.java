package com.sbdev.letschat;

public class MessageModel {

    String sender,receiver,text;
    boolean isSeen;

    public MessageModel(String sender, String receiver, String text, boolean isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.isSeen = isSeen;
    }

    public MessageModel()
    {

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
