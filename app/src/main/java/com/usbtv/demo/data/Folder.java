package com.usbtv.demo.data;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.usbtv.demo.comm.SSLSocketClient;

import java.io.File;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;


public class Folder {

    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField
    int typeId;

    @DatabaseField
    int orderSeq;

    String cat;
    @DatabaseField
    String name;
    @DatabaseField
    String p;
    @DatabaseField
    String coverUrl;

    @DatabaseField
    String link;
    @DatabaseField
    String score;
    @DatabaseField
    String job;

    @DatabaseField
    String bvid;
    @DatabaseField(uniqueCombo = true)
    String aid;

    @DatabaseField
    float rate;
    @DatabaseField
    long pubTime;
    @DatabaseField
    long updateTime;


    @ForeignCollectionField(orderColumnName = "orderSeq", orderAscending = true)
    private Collection<VFile> files;

    @JSONField(serialize = false)

    @DatabaseField(foreign = true, foreignAutoRefresh = true, uniqueCombo = true)
    private Drive root;

    @DatabaseField
    int isFav;

    private List<VFile> listFiles;

    private Integer rootId;

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getP() {
        if (p == null) return aid;
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getCoverUrl() {
        String abPath = absPath();
        return coverUrl == null && abPath != null ? SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/thumb?id=" + id + "&path=" + URLEncoder.encode(abPath) : coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Collection<VFile> getFiles() {
        return files;
    }

    public void setFiles(Collection<VFile> files) {
        this.files = files;
    }

    public Drive getRoot() {
        return root;
    }

    public void setRoot(Drive root) {
        this.root = root;
    }

    public Integer getRootId() {
        return rootId;
    }

    public void setRootId(Integer rootId) {
        this.rootId = rootId;
    }

    public String getBvid() {
        return bvid;
    }

    public void setBvid(String bvid) {
        this.bvid = bvid;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String absPath() {
        if (this.getRoot() == null) return null;
        if (this.getP() == null) return null;

        return this.getRoot().getP() + "/" + this.getP();
    }

    public boolean exists() {
        return this.absPath() != null && new File(this.absPath()).exists();
    }

    public String getShortName() {

        return getName().length() < 10 ? getName() : getName().substring(0, 10);
    }

    public int getOrderSeq() {
        return orderSeq;
    }

    public void setOrderSeq(int orderSeq) {
        this.orderSeq = orderSeq;
    }

    public int getIsFav() {
        return isFav;
    }

    public void setIsFav(int isFav) {
        this.isFav = isFav;
    }

    public List<VFile> getListFiles() {
        return listFiles;
    }

    public void setListFiles(List<VFile> listFiles) {
        this.listFiles = listFiles;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public long getPubTime() {
        return pubTime;
    }

    public void setPubTime(long pubTime) {
        this.pubTime = pubTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}