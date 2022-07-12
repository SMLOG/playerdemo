package com.usbtv.demo.news;



import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;

import java.util.Date;


public class UploadItem {

    @DatabaseField(generatedId = true)
    @JSONField(serialize = false)
    Integer id;

    @JSONField(serialize = false)
    @DatabaseField
    String url;
    @DatabaseField
    String date;

    @JSONField(serialize = false)
    String content;

    @DatabaseField
    String title;

    @DatabaseField
    @JSONField(name = "i")
    int status;

    @DatabaseField
    String p;

    @DatabaseField
    String src;

    public UploadItem() {
        super();
        // TODO Auto-generated constructor stub
    }



    public UploadItem(String url,String date) {
        super();
        this.url = url;
        this.date=date;
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getP() {
        return  this.p;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }



    public void setP(String p) {
        this.p = p;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }
}