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


    @DatabaseField(uniqueCombo = true)
    int typeId;
    @DatabaseField(uniqueCombo = true)
    String cat;
    @DatabaseField
    String name;
    @DatabaseField(unique = true)
    String p;
    @DatabaseField
    String coverUrl;

    @ForeignCollectionField
    private ForeignCollection<VFile> files;

    @JSONField(serialize=false)
    @DatabaseField(foreign = true,foreignAutoRefresh = true)
    private Drive root;

    private Integer rootId;

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
}