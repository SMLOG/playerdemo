package com.usbtv.demo;


import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.data.Feed;

import java.util.List;

public class ConfigStore {
   public Integer fileId=0;
   public boolean usingBiliPlayer =false;
   public boolean isSeamless=false;
   public boolean startAtBoot=true;
   public boolean subTitleActive=true;
   public boolean subTitleActiveTran=false;
    public List<Feed> feeds;
    public String configUrl ="https://smlog.github.io/data/configStore.json";
    public String fallback;
    public String[] ytPlayList;
    public String proxy;
    public String[] ipTvList;
    public String[] cnnList;

    public void save() {
      SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("ConfigStore", Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = sp.edit();
      String jsonStr = JSON.toJSONString(this);
      editor.putString("ConfigStore", jsonStr);
      editor.apply();
      sp.edit().commit();
   }
   public static ConfigStore restore(){
      SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("ConfigStore", Context.MODE_PRIVATE);
      String jsonStr = sp.getString("ConfigStore", null);
      if(jsonStr!=null) return JSON.parseObject(jsonStr,ConfigStore.class);
      else  return new ConfigStore();
   }
}
