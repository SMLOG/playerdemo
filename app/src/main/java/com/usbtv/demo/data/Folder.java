package com.usbtv.demo.data;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;


public class Folder {

    public static final int IMAGE = 1;
    public static final int VIDEO = 0;
    public static final int AUDIO = 2;

    @DatabaseField(generatedId = true)
    int id;

    String cat;
    @DatabaseField
    String name;
    @DatabaseField
    String p;
    @DatabaseField
    String coverUrl;

    @DatabaseField
    String bvid;
    @DatabaseField(uniqueCombo = true)
    String aid;

    @ForeignCollectionField
    private ForeignCollection<VFile> files;

    @JSONField(serialize=false)

    @DatabaseField(foreign = true,foreignAutoRefresh = true,uniqueCombo = true)
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
}