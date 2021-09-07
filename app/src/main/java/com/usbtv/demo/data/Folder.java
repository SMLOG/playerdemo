package com.usbtv.demo.data;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import app.com.tvrecyclerview.RowItem;


public class Folder extends RowItem {



    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField
    int typeId;

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
    String bvid;
    @DatabaseField(uniqueCombo = true)
    String aid;

    @ForeignCollectionField
    private ForeignCollection<VFile> files;

    @JSONField(serialize = false)

    @DatabaseField(foreign = true, foreignAutoRefresh = true, uniqueCombo = true)
    private Drive root;

    private Integer rootId;

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
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public ForeignCollection<VFile> getFiles() {
        return files;
    }

    public void setFiles(ForeignCollection<VFile> files) {
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
}