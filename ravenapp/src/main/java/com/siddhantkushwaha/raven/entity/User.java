package com.siddhantkushwaha.raven.entity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class User {

    private String phoneNumber;
    private UserProfile userProfile;
    private UserLocation userLocation;

    @ServerTimestamp
    private Timestamp timestamp;

    public User() {

    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void cloneObject(User user) {

        this.setPhoneNumber(user.getPhoneNumber());
        this.setUserProfile(user.getUserProfile());
        this.setUserLocation(user.getUserLocation());
    }
}
