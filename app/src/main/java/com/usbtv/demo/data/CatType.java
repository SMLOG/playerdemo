package com.usbtv.demo.data;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.usbtv.demo.comm.SSLSocketClient;

import java.io.File;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;


public class CatType {


    @DatabaseField(id=true,generatedId = false)
    int typeId;
    @DatabaseField
    int orderSeq;

    @DatabaseField
    String name;
    @DatabaseField
    String status;

    @DatabaseField
    String job;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getOrderSeq() {
        return orderSeq;
    }

    public void setOrderSeq(int orderSeq) {
        this.orderSeq = orderSeq;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
}