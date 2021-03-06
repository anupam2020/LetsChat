package com.sbdev.letschat;

public class UserModel {

    private String Email,Name,ProfilePic,UID,status,lastMsg,last_text_time,token,isLoggedIn;

    public UserModel(String email, String name, String profilePic, String UID, String status, String lastMsg, String last_text_time, String token, String isLoggedIn) {
        Email = email;
        Name = name;
        ProfilePic = profilePic;
        this.UID = UID;
        this.status = status;
        this.lastMsg = lastMsg;
        this.last_text_time = last_text_time;
        this.token = token;
        this.isLoggedIn = isLoggedIn;
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

    public String getToken() {return token;}

    public void setToken(String token) {this.token = token;}

    public String getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(String isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }
}
