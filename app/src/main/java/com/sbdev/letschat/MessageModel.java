package com.sbdev.letschat;

import android.net.Uri;

public class MessageModel {

    String sender,receiver,text,time,key,senderPic,receiverPic,senderName,receiverName,imgURI;
    boolean isSeen;

    public MessageModel(String sender, String receiver, String text, String time, String key, String senderPic, String receiverPic, String senderName, String receiverName, String imgURI, boolean isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.time = time;
        this.key = key;
        this.senderPic = senderPic;
        this.receiverPic = receiverPic;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.imgURI = imgURI;
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

    public String getSenderPic() {
        return senderPic;
    }

    public void setSenderPic(String senderPic) {
        this.senderPic = senderPic;
    }

    public String getReceiverPic() {
        return receiverPic;
    }

    public void setReceiverPic(String receiverPic) {
        this.receiverPic = receiverPic;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getImgURI() {
        return imgURI;
    }

    public void setImgURI(String imgURI) {
        this.imgURI = imgURI;
    }

    public boolean getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }
}
