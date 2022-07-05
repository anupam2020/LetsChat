package com.sbdev.letschat;

public class UserModel {

    String Email,Name,ProfilePic,UID,status,isDisconnected;

    public UserModel(String email, String name, String profilePic, String UID, String status, String isDisconnected) {
        Email = email;
        Name = name;
        ProfilePic = profilePic;
        this.UID = UID;
        this.status = status;
        this.isDisconnected = isDisconnected;
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
}
