package com.usbtv.demo.live;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class M3u8 {

    static Pattern pattern = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");

    Integer id;

    String desktopUrl;
   
    String mobileUrl;

   
    String tvUrl;

   
    String referUrl;

   
    String title;

    String videoId;

   
    String coverImg;
   
    long dt;

   
    String org;

   
    String alias;

    public M3u8() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDesktopUrl() {
        return desktopUrl;
    }

    public void setDesktopUrl(String desktopUrl) {
        this.desktopUrl = desktopUrl;
    }

    public String getMobileUrl() {
        return mobileUrl;
    }

    public void setMobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }

    public String getTvUrl() {
        return tvUrl;
    }

    public void setTvUrl(String tvUrl) {
        this.tvUrl = tvUrl;
    }

    public String getReferUrl() {
        return referUrl;
    }

    public void setReferUrl(String referUrl) {
        this.referUrl = referUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public M3u8(String desktopUrl, String mobileUrl, String tvUrl, String referUrl, String title, String videoId,
                String coverImg, long dt,String org) {
        super();
        this.desktopUrl = desktopUrl;
        this.mobileUrl = mobileUrl;
        this.tvUrl = tvUrl;
        this.referUrl = referUrl;
        this.title = title;
        this.videoId = videoId;
        this.coverImg = coverImg;
        this.dt = dt;
        this.org = org;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getAlias() {
        if(alias == null && videoId!=null) {
            Matcher m = pattern.matcher(videoId);
            if(m.find())alias = m.group()+":"+title;
        }else alias = title;
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }




}