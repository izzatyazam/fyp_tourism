package com.example.tourism_cif;

//module class Posts
public class Posts {

    //must same name as in firebase database
    public String uid;
    public String time;
    public String date;
    public String postImage;
    public String status;
    public String uname;

    //default constructor
    public Posts(){

    }


    public Posts(String uid, String time, String date, String postImage, String status, String uname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postImage = postImage;
        this.status = status;
        this.uname = uname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }
}
