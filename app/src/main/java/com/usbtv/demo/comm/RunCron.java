package com.usbtv.demo.comm;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public class RunCron {

     public static class Period{
         public long lastRunAt = 0;
         public int enable = -1;
         public String id(){return "";}
         public long getDuration(){return 0;}
         public void doRun() throws Throwable {}
         public String getId(){
             return id();
         }
         public String getName(){
             return id();
         };
         public boolean canRunIfDefault(){
             return false;
         };
         public void setEnable(boolean v){
             if(v) enable=1;
             else enable=0;
         }

         public boolean getEnable() {
             return enable==-1?canRunIfDefault():enable==0?false:true;
         }
     }




    public static final Map<String,Period> peroidMap= new HashMap<String,Period>();


    public static void run(Period period, String forceRunId){

        SharedPreferences sp = App.getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);
        String id = "_task_"+period.id();
        String json = sp.getString(id,"");sp.getAll();
        Period task;
        if(!json.equals("")){
            task =JSON.parseObject(json,Period.class);

        }else {
            task = period;
        }

        peroidMap.put(period.id(),task);

        boolean canRun = task.getEnable();
        if(canRun) if(period.id().equals(forceRunId) || System.currentTimeMillis()-task.lastRunAt > period.getDuration()){
            try {
                period.doRun();
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
