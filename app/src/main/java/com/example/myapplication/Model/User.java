package com.example.myapplication.Model;


import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class User extends FirebaseKey implements Serializable {

    private String firstname;
    private String email;
    private String lastname;
    private String password;
    private String phoneNumber;
    private int account_type;
    private String imagePath;
    private String imageUrl;
    private String myId;

    public void setMyId(String setId){ this.myId = setId; }

    public String getMyId(){return myId; }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    @Exclude
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    @Exclude
    public String getPassword() {
        return password;
    }

    public  int getAccount_type(){return account_type;}

    public void setPassword(String password) {
        this.password = password;
    }

    public  void setAccount_type(int account_type){this.account_type = account_type;}

    public User(){this.account_type = 0;}

    @Exclude
    public boolean isValid(){
        if(this.firstname == null || this.firstname.isEmpty()) return false;
        if(this.lastname == null || this.lastname.isEmpty()) return false;
        if(this.email == null || this.email.isEmpty()) return false;
        if(this.password == null || this.password.isEmpty()) return false;
        return this.phoneNumber != null && !this.phoneNumber.isEmpty();
    }

    public String getKey(){return key;}

    public void setKeyOn(String key){this.key= key;}

}