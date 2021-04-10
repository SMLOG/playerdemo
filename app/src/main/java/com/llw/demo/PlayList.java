package com.llw.demo;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PlayList {


    private ArrayList<Aid>  aidList = new ArrayList<Aid>();;
    private int bIndex = 0;
    private int aIndex = 0;

    public int getbIndex() {
        return bIndex;
    }

    public int getaIndex() {
        return aIndex;
    }

    public ArrayList<Aid> getAidList() {
        return aidList;
    }


    private int autoAindex(){
        if( aIndex>= aidList.size()) aIndex = 0;
        if(aIndex<0) aIndex  = 0;
        return aIndex;
    }

    public synchronized String nextURL(int bIndex) {

        this.bIndex++;
        if(bIndex<0 && this.bIndex>=0)bIndex = this.bIndex;

        if(aidList.size()==0)return  null;





        autoAindex();

        if(bIndex >=  aidList.get(aIndex).getItems().size() || bIndex<0){
            bIndex = 0 ;
            aIndex++;
            while (aidList.get(aIndex).getItems().size()==0){
                aIndex++;
                autoAindex();
            }
        }
        String url = aidList.get(aIndex).getItems().get(bIndex).getPath();

        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("aIndex",aIndex);
        editor.putInt("bIndex",bIndex);
        editor.commit();

        return  getUrl(aIndex,bIndex);
    }

    public String getUrl(int aIndex,int bIndex){
        String url = aidList.get(aIndex).getItems().get(bIndex).getPath();
        return this.aidList.get(aIndex).getServerBase()+this.aidList.get(aIndex).getAid()+ File.separator+url;
    }
    public synchronized void  addAll(List<Aid> aidList, String host) {


        for(int k =0;k<aidList.size();k++){
            Aid o = aidList.get(k);
            if(null== o.getServerBase()){
                o.setServerBase( host.split("playlist")[0]);
            }
            int index = this.aidList.indexOf(o);
            if(index >-1)aidList.set(index, o);
            else this.aidList.add(o);
        }

        App.sendPlayBroadCast(-1,-1);
    }

    public void setAIndex(int aIndex) {
        synchronized (this){
            this.aIndex = aIndex;
        }
    }
}
