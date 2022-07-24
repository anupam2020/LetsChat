package com.sbdev.letschat;

public class ActivitiesModel {

    String name,profilePic,status,key,UID,dateTime,timestamp;

    public ActivitiesModel(String name, String profilePic, String status, String key, String UID, String dateTime, String timestamp) {
        this.name = name;
        this.profilePic = profilePic;
        this.status = status;
        this.key = key;
        this.UID = UID;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
    }

    public ActivitiesModel()
    {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
