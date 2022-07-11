package com.sbdev.letschat;

public class UserModel {

    String Email,Name,ProfilePic,UID,status,isDisconnected,lastMsg,last_text_time;

    public UserModel(String email, String name, String profilePic, String UID, String status, String isDisconnected, String lastMsg, String last_text_time) {
        Email = email;
        Name = name;
        ProfilePic = profilePic;
        this.UID = UID;
        this.status = status;
        this.isDisconnected = isDisconnected;
        this.lastMsg = lastMsg;
        this.last_text_time = last_text_time;
    }

    public UserModel()
    {

    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getProfilePic() {
        return ProfilePic;
    }

    public void setProfilePic(String profilePic) {
        ProfilePic = profilePic;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsDisconnected() {
        return isDisconnected;
    }

    public void setIsDisconnected(String isDisconnected) {
        this.isDisconnected = isDisconnected;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getLast_text_time() {
        return last_text_time;
    }

    public void setLast_text_time(String last_text_time) {
        this.last_text_time = last_text_time;
    }
}
