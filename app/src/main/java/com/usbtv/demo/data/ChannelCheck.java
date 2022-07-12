package com.usbtv.demo.data;

import com.j256.ormlite.field.DatabaseField;


public class ChannelCheck {

    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    String url;
    @DatabaseField
    boolean ok;
    @DatabaseField
    long dt;


    public ChannelCheck() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }
}