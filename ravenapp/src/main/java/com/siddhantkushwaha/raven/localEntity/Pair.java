package com.siddhantkushwaha.raven.localEntity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Pair extends RealmObject {

    @PrimaryKey
    private String ref;
    private String uri;

    public Pair() {

    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}