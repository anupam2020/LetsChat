package com.sbdev.letschat;

public class ChatsListModel {

    String UID;

    public ChatsListModel(String UID) {
        this.UID = UID;
    }

    public ChatsListModel()
    {

    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
