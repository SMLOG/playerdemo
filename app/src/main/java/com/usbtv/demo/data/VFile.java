package com.usbtv.demo.data;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;

import java.io.File;


public class VFile {


    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    int typeId;
    @DatabaseField
    String cat;
    @DatabaseField
    String name;

    @DatabaseField
    String coverUrl;

    @JSONField(serialize=false)
    @DatabaseField(foreign = true,foreignAutoRefresh = true)
    Folder folder;
    @DatabaseField
    String p;

    @DatabaseField
    Integer page;


    public VFile() {
    }

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

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    @JSONField(serialize = false)
    public String getAbsPath() {
        if(folder!=null){
            if(folder.getRoot()!=null){
                if(p!=null)
                return folder.getRoot().getP()+ File.separator+folder.getP()+File.separator+p;
                else return folder.getRoot().getP()+ File.separator+folder.getAid()+File.separator+page+File.separator+folder.getAid()+".mp4";
            }
        }
        return null;
    }
}