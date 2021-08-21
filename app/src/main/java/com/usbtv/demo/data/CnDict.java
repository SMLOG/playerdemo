package com.usbtv.demo.data;

import com.j256.ormlite.field.DatabaseField;


public class CnDict {

    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField(unique = true)
    String cnText;

    @DatabaseField
    String imgUrl;
    public CnDict() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCnText() {
        return cnText;
    }

    public void setCnText(String cnText) {
        this.cnText = cnText;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}