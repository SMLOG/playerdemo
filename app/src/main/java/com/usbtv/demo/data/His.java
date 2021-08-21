package com.usbtv.demo.data;

import com.j256.ormlite.field.DatabaseField;


public class His {

    public static final int LANG_EN = 1;
    public static final int LANG_JP = 2;

    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField(foreign = true,foreignAutoRefresh = true)
    CnDict cn;

    @DatabaseField(uniqueCombo = true)
    int langId;

    @DatabaseField(defaultValue = "0")
    int level;

    @DatabaseField(defaultValue = "0")
    int orderN;

    @DatabaseField
    String cat;

    @DatabaseField(uniqueCombo = true)
    String langText;

    @DatabaseField
    long updateTime;

    @DatabaseField(defaultValue = "0")
    int rTimes;

    public His() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLangId() {
        return langId;
    }

    public void setLangId(int langId) {
        this.langId = langId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getOrderN() {
        return orderN;
    }

    public void setOrderN(int orderN) {
        this.orderN = orderN;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getLangText() {
        return langText;
    }

    public void setLangText(String langText) {
        this.langText = langText;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getrTimes() {
        return rTimes;
    }

    public void setrTimes(int rTimes) {
        this.rTimes = rTimes;
    }

    public CnDict getCn() {
        return cn;
    }

    public void setCn(CnDict cn) {
        this.cn = cn;
    }

    public String getLang() {
        return  langId==LANG_EN?"en":"jp";
    }
}