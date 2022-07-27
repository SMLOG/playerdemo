package com.usbtv.demo.comm;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RunCron {

    public static class Period {
        public long lastRunAt = 0;
        public int enable = -1;
        public String id;
        public String name;
        public long duration;
        public boolean defaultRun = false;
        public boolean show = true;
        public int status;
        private boolean canRun;

        public boolean isCanRun() {
            return canRun;
        }

        public Period() {
        }

        public Period(String id, String name, long duration, boolean defaultRun) {
            this.id = id;
            this.name = name;
            this.duration = duration;
            this.defaultRun = defaultRun;

        }

        public void doRun() throws Throwable {

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


        public void setEnable(boolean v) {
            if (v) enable = 1;
            else enable = 0;
        }

        public boolean getEnable() {
            return enable == -1 ? defaultRun : enable == 0 ? false : true;
        }

    }


    public static final Map<String, Period> peroidMap = new LinkedHashMap<String, Period>();


    public static void addPeriod(RunCron.Period period) {
        if (period != null) peroidMap.put(period.getId(), period);

    }

    public static void addToQueue(String forceRunId) {
        SharedPreferences sp = App.getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);

       for(String key: peroidMap.keySet()){
           Period period = peroidMap.get(key);
           String id = "_task_" + period.getId();
           String json = sp.getString(id, "");
           Period task;
           if (!json.equals("")) {
               task = JSON.parseObject(json, Period.class);
           }
           else {
               task = period;
           }
           period.enable = task.enable;
           period.lastRunAt = task.lastRunAt;

           period.canRun = task.getEnable() && (period.getId().equals(forceRunId) || System.currentTimeMillis() - task.lastRunAt > period.getDuration());

           if (period.canRun && !queue.contains(period)) queue.add(period);
       }


    }

    private static final Lock lock = new ReentrantLock();
    private static BlockingQueue<Period> queue = new LinkedBlockingQueue<Period>();

    public static void startRunTasks() {

        SharedPreferences sp = App.getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);

        try {
            if(lock.tryLock())
            while (true) {

                if (queue.size() == 0) break;
                Period task = queue.take();
                if (task == null) break;

                if (!task.canRun) continue;


                try {
                    task.status = 1;
                    task.doRun();
                    SharedPreferences.Editor editor = sp.edit();
                    task.lastRunAt = System.currentTimeMillis();

                    editor.putString("_task_" + task.getId(), JSON.toJSONString(task));

                    editor.apply();
                    editor.commit();

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                } finally {
                    task.status = 0;
                }

            }
        } catch (Throwable ee) {
            ee.printStackTrace();
        } finally {
            lock.unlock();
        }

    }
}
