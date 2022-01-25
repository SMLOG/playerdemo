package com.usbtv.demo.comm;

import android.content.Context;
import android.content.SharedPreferences;

public class RunCron {

    public interface Period{
        String id();
        long getPeriodDuration();
        void doRun() throws Throwable;
    }

    public static void run(Period period, String forceRunId){

        SharedPreferences sp = App.getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);
        String id = "peroid_"+period.id();
        long lastStamp = sp.getLong(id, 0l);
        if(period.id().equals(forceRunId) || System.currentTimeMillis()-lastStamp > period.getPeriodDuration()){
            try {
                period.doRun();
                SharedPreferences.Editor editor = sp.edit();
                editor.putLong(id, System.currentTimeMillis());
                editor.apply();
                sp.edit().commit();

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

    }
}
