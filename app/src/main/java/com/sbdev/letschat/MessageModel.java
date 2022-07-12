package com.sbdev.letschat;

public class MessageModel {

    String sender,receiver,text,time,key,favorite;

    public MessageModel(String sender, String receiver, String text, String time, String key, String favorite) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.time = time;
        this.key = key;
        this.favorite = favorite;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFavorite() {
        return favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }
}
