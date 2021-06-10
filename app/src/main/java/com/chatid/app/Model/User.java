package com.chatid.app.Model;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String id;
    private String username;
    private String email;
    private String imageURL;
    private String idImageURL;
    private String status;
    private List<ContactItem> contactList;

    public User() {
        contactList=new ArrayList<>();
    }

    public User(String id, String username, String email, String imageURL, String idImageURL, String status, List<ContactItem> contactList) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.imageURL = imageURL;
        this.idImageURL = idImageURL;
        this.status = status;
        this.contactList = contactList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getIdImageURL() {
        return idImageURL;
    }

    public void setIdImageURL(String idImageURL) {
        this.idImageURL = idImageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ContactItem> getContactList() {
        return contactList;
    }

    public void setContactList(List<ContactItem> contactList) {
        this.contactList = contactList;
    }

    public void addToContactList(ContactItem contactItem){
        this.contactList.add(contactItem);
    }
}
