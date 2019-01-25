package com.siddhantkushwaha.raven.entity;

import android.support.annotation.NonNull;

import com.google.firebase.database.PropertyName;

public class WallpaperMetadata {


    private String highResRef;
    private String lowResRef;
    private String info;
    private String contributedBy;


    public WallpaperMetadata() {

    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @PropertyName("high_res")
    public String getHighResRef() {
        return highResRef;
    }

    @PropertyName("high_res")
    public void setHighResRef(String highResRef) {
        this.highResRef = highResRef;
    }

    @PropertyName("low_res")
    public String getLowResRef() {
        return lowResRef;
    }

    @PropertyName("low_res")
    public void setLowResRef(String lowResRef) {
        this.lowResRef = lowResRef;
    }

    @PropertyName("contributed_by")
    public String getContributedBy() {
        return contributedBy;
    }

    @PropertyName("contributed_by")
    public void setContributedBy(String contributedBy) {
        this.contributedBy = contributedBy;
    }

    public void cloneObject(@NonNull WallpaperMetadata wallpaperMetadata) {

        setHighResRef(wallpaperMetadata.getHighResRef());
        setLowResRef(wallpaperMetadata.getLowResRef());
        setInfo(wallpaperMetadata.getInfo());
        setContributedBy(wallpaperMetadata.getContributedBy());
    }
}
