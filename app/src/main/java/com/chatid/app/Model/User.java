package com.chatid.app.Model;

public class User {

    private String id;
    private String username;
    private String email;
    private String imageURL;
    private String idImageURL;
    private String status;

    public User(String id, String username, String imageURL, String status, String Email) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;

        this.idImageURL = idImageURL;

        this.status = status;
        this.email = email;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdImageURL() {
        return idImageURL;
    }

    public void setIdImageURL(String idImageURL) {
        this.idImageURL = idImageURL;
    }
}
