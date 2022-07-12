package com.usbtv.demo.vurl;

import android.net.Uri;

import com.usbtv.demo.comm.SSLSocketClient;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class VUrlList {

    private  int curIndex;
    private String name;
    private List<String> urls;



    public VUrlList(String name,int index,String[] urls) {
        this.curIndex = index;
        this.name = name;
        this.urls = new ArrayList<>();
        for(String url:urls){
            add(url);
        }
    }

    public void add(String url){
        urls.add(url);
    }

    public Uri getCurVideoUrl() {
        return Uri.parse(SSLSocketClient.ServerManager.getServerHttpAddress()+"/api/r/"+ URLEncoder.encode(name)+"/"+curIndex+"/index.m3u8?url="+URLEncoder.encode(urls.get(curIndex)));
    }

    public String getCurUrl() {
        return this.urls.get(curIndex);
    }

    public void curNext() {
        curIndex++;
        if(curIndex>=urls.size())curIndex=0;
    }

}