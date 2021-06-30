package com.usbtv.demo.data;

import com.j256.ormlite.field.DatabaseField;


public class ResItem {

    public static final int IMAGE = 1;
    public static final int VIDEO = 0;
    public static final int AUDIO = 2;

    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    int typeId;

    @DatabaseField
    String cat;
    @DatabaseField
    String cnText;
    @DatabaseField(unique = true)
    String enText;

    @DatabaseField
    String jpText;

    @DatabaseField
    String imgUrl;
    @DatabaseField
    String sound;

    @DatabaseField
    private int rTimes;


    public ResItem() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getCnText() {
        return cnText;
    }

    public void setCnText(String cnText) {
        this.cnText = cnText;
    }

    public String getEnText() {
        return enText;
    }

    public void setEnText(String enText) {
        this.enText = enText;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public int getrTimes() {
        return rTimes;
    }

    public void setrTimes(int rTimes) {
        this.rTimes = rTimes;
    }

    public String getJpText() {
        return jpText;
    }

    public void setJpText(String jpText) {
        this.jpText = jpText;
    }
}