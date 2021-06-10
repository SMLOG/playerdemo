package com.usbtv.demo.data;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;


public class Drive {


    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField(unique = true)
    String p;

    public Drive() {
    }

    public Drive(String absolutePath) {
        this.p = absolutePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }
}