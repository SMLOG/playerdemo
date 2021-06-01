package com.usbtv.demo.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.j256.ormlite.field.DatabaseField;


public class ResItem {

    public static final int IMAGE=1;
    public static final int VIDEO=0;
    public static final int AUDIO=2;

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
    String imgUrl;
    @DatabaseField
    String sound;

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
}