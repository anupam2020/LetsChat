package com.sbdev.letschat;

public class UserModel {

    String Email,Name,ProfilePic,UID;

    public UserModel(String email, String name, String profilePic, String UID) {
        Email = email;
        Name = name;
        ProfilePic = profilePic;
        this.UID = UID;
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
}
