package com.usbtv.demo.data;

import com.j256.ormlite.field.DatabaseField;


public class Cat {

    public static final int IMAGE = 1;
    public static final int VIDEO = 0;
    public static final int AUDIO = 2;

    @DatabaseField(generatedId = true)
    int catId;

    @DatabaseField(uniqueCombo = true)
    int typeId;
    @DatabaseField(uniqueCombo = true)
    String cat;
    @DatabaseField
    String enText;
    @DatabaseField
    String imgUrl;


    public Cat() {
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
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

}