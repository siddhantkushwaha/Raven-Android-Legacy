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

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void cloneObject(User user) {

        this.setPhoneNumber(user.getPhoneNumber());
        this.setUserProfile(user.getUserProfile());
        this.setUserLocation(user.getUserLocation());
    }
}
