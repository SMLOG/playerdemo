package com.usbtv.demo.vurl;

import android.net.Uri;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class VUrlList {

    private  int curIndex;
    private String name;
    private List<String> urls;



    public VUrlList(String name,int index,String[] urls) {
        this.curIndex = index;
        this.urls = new ArrayList<>();
        for(String url:urls){
            add(url);
        }
    }

    public void add(String url){
        urls.add(url);
    }

    public Uri getCurVideoUrl() {
        return Uri.parse(this.urls.get(curIndex));
    }

    public void curNext() {
        curIndex++;
        if(curIndex>=urls.size())curIndex=0;
    }
}