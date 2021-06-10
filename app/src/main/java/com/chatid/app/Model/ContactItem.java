package com.chatid.app.Model;

import java.sql.Date;

public class ContactItem {
    private String id;
    private String status;
    private String username;
    private String imageUrl;

    public ContactItem() {
    }

    public ContactItem(String id, String status, String username, String imageUrl) {
        this.id = id;
        this.status = status;
        this.username = username;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
