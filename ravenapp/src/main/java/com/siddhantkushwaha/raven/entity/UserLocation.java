package com.siddhantkushwaha.raven.entity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class UserLocation {

    private double latitude;
    private double longitude;
    private String privacyStatus;

    @ServerTimestamp
    private Timestamp timestamp;

    public UserLocation() {

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPrivacyStatus() {
        return privacyStatus;
    }

    public void setPrivacyStatus(String privacyStatus) {
        this.privacyStatus = privacyStatus;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
