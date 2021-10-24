package com.usbtv.demo.data;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;

import java.io.File;


public class Cache {


    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    String key;
    @DatabaseField
    String content;


    public Cache() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}