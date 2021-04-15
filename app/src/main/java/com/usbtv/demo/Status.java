package com.usbtv.demo;

public class Status {
    public static final int PLAYING=1;
    public static final  int STOPED=0;
    private int status;
    private long curPostion;
    private long duration;
    private int aIndex;
    private int bIndex;
    private int mode;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCurPostion() {
        return curPostion;
    }

    public void setCurPostion(long curPostion) {
        this.curPostion = curPostion;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getaIndex() {
        return aIndex;
    }

    public void setaIndex(int aIndex) {
        this.aIndex = aIndex;
    }

    public int getbIndex() {
        return bIndex;
    }

    public void setbIndex(int bIndex) {
        this.bIndex = bIndex;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
