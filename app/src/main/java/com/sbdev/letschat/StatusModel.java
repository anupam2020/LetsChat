package com.sbdev.letschat;

public class StatusModel {

    String uid, imageURL, name, date;

    public StatusModel(String uid, String imageURL, String name, String date) {
        this.uid = uid;
        this.imageURL = imageURL;
        this.name = name;
        this.date = date;
    }

    public StatusModel()
    {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
