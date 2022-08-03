package com.sbdev.letschat;

public class FriendClass {

    String friendUID;

    public FriendClass(String friendUID) {
        this.friendUID = friendUID;
    }

    public FriendClass() {

    }

    public String getFriendUID() {
        return friendUID;
    }

    public void setFriendUID(String friendUID) {
        this.friendUID = friendUID;
    }
}
