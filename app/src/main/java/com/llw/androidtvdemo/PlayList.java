package com.llw.androidtvdemo;

import java.util.ArrayList;
import java.util.LinkedList;

public class PlayList extends ArrayList<VideoItem> {

    private int curIndex=-1;
    public synchronized VideoItem next(){
       if(this.size()<=0)return null;
       if(curIndex<0)curIndex=-1;
       curIndex++;

       if(curIndex>this.size()-1) curIndex = 0;


        VideoItem item=  get(curIndex);
        item.times++;
        return item;
    }
    public  void markCurItemErrorStatus(){
        if(curIndex>-1&&curIndex<this.size()){
           VideoItem curItem = get(curIndex);
           if(curItem!=null)curItem.status++;
        }
    }

    public String nextURL(int index) {

        if (index >= 0 && index < App.playList.size()) {
            return App.playList.get(index).url;
        }
       VideoItem item = this.next();
       if(item!=null)return item.url;
       else return null;
    }
}
