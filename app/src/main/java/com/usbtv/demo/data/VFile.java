package com.usbtv.demo.data;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.DocumentsUtils;

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
    @DatabaseField
    String dLink;

    @JSONField(serialize=false)
    @DatabaseField(foreign = true,foreignAutoRefresh = true)
    Folder folder;

    @DatabaseField
    String aid;

    @DatabaseField
    String bvid;

    @DatabaseField
    String p;

    @DatabaseField
    Integer page;

    @DatabaseField
    int orderSeq;

    @DatabaseField
    int playCnt;

    private int type;

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

    public String getdLink() {
        return dLink;
    }

    public void setdLink(String dLink) {
        this.dLink = dLink;
    }

    @JSONField(serialize = false)
    public String getAbsPath() {
        if(folder!=null){
            if(folder.getRoot()!=null){
                return folder.getRoot().getP()+"/_/"+ (bvid==null?folder.getBvid():bvid)+"/"+page+".mp4";
               // else if(folder.getAid()!=null)
               // return folder.getRoot().getP() + File.separator + folder.getAid()+File.separator+aid + File.separator + getRelativePath();
               // else  return folder.getRoot().getP() + File.separator + aid+ File.separator + getRelativePath();
            }
        }
        return null;
    }

    public String getOldAbsPath() {
        if(folder!=null && folder.getAid()!=null){
            if(folder.getRoot()!=null){
                if(p!=null)
                    return folder.getRoot().getP()+ File.separator+folder.getP()+File.separator+p;
                else  return folder.getRoot().getP() + File.separator + aid+ File.separator + getRelativePath();
            }
        }
        return null;
    }


    @JSONField(serialize = false)
    public String getRelativePath() {
        return  File.separator + folder.getAid() +"_"+ ".mp4";
    }

    public boolean exists() {

       boolean isExits =  this.getAbsPath()!=null && new File(this.getAbsPath()).exists();

       if(!isExits){
           String oldPath = getOldAbsPath();
           if(oldPath!=null){
               File oldFile = new File(oldPath);
               if(oldFile.exists()){
                   try{
                       DocumentsUtils.renameTo(App.getInstance(),oldFile,new File(this.getAbsPath()));
                       isExits = new File(this.getAbsPath()).exists();
                   }catch (Throwable e){
                        e.printStackTrace();
                   }

               }
           }
       }
       return  isExits;
    }

    public int getOrderSeq() {
        return orderSeq;
    }

    public void setOrderSeq(int orderSeq) {
        this.orderSeq = orderSeq;
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

    public int getPlayCnt() {
        return playCnt;
    }

    public void setPlayCnt(int playCnt) {
        this.playCnt = playCnt;
    }
}