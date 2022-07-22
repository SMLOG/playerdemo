package com.usbtv.demo.comm;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RunCron {

     public static class Period{
         public long lastRunAt = 0;
         public int enable = -1;
         public String id;
         public String name;
         public  long duration;
         public boolean defaultRun=false;
         public boolean runing=false;
         private boolean canRun;

         public Period() {
         }

         public Period(String id, String name, long duration, boolean defaultRun) {
             this.id = id;
             this.name = name;
             this.duration = duration;
             this.defaultRun=defaultRun;

         }

         public void doRun() throws Throwable{

         }

         public long getDuration() {
             return duration;
         }

         public void setDuration(long duration) {
             this.duration = duration;
         }

         public long getLastRunAt() {
             return lastRunAt;
         }

         public void setLastRunAt(long lastRunAt) {
             this.lastRunAt = lastRunAt;
         }

         public String getId() {
             return id;
         }

         public void setId(String id) {
             this.id = id;
         }

         public String getName() {
             return name;
         }

         public void setName(String name) {
             this.name = name;
         }


         public void setEnable(boolean v){
             if(v) enable=1;
             else enable=0;
         }

         public boolean getEnable() {
             return enable==-1?defaultRun:enable==0?false:true;
         }

         public boolean isRuning() {
             return runing;
         }

         public void setRuning(boolean runing) {
             this.runing = runing;
         }
     }




    public static final Map<String,Period> peroidMap= new LinkedHashMap<String,Period>();


    public static void run(Period period, String forceRunId){

        SharedPreferences sp = App.getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);
        String id = "_task_"+period.getId();
        String json = sp.getString(id,"");sp.getAll();
        Period task;
        if(!json.equals("")){
            task =JSON.parseObject(json,Period.class);

        }else {
            task = period;
        }
        period.enable = task.enable;
        period.lastRunAt = task.lastRunAt;

        peroidMap.put(period.getId(),period);
        period.runing=false;
        period.canRun =  task.getEnable() && (period.getId().equals(forceRunId) || System.currentTimeMillis()-task.lastRunAt > period.getDuration());
    }
    public static void  startRunTasks(){
        SharedPreferences sp = App.getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);

        for(String id: peroidMap.keySet()){
           Period task = peroidMap.get(id);

           if(!task.canRun)continue;

           try {
               task.runing=true;
               task.doRun();
               SharedPreferences.Editor editor = sp.edit();
               task.lastRunAt = System.currentTimeMillis();
               editor.putString(id,JSON.toJSONString(task));

               editor.apply();
               editor.commit();

           } catch (Throwable throwable) {
               throwable.printStackTrace();
           }
       }

    }
}
