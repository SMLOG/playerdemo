package com.usbtv.demo.data;

import com.j256.ormlite.field.DatabaseField;


public class Video {


    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField(unique = true)
    String videoId;
    @DatabaseField
    String title;

    @DatabaseField
    String coverUrl;
    @DatabaseField
    String url;

    @DatabaseField
    Integer dt;


    public Video() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }
}