package com.usbtv.demo.data;

import com.j256.ormlite.field.DatabaseField;


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
    @DatabaseField
    String url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}