package com.usbtv.demo.sync;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.usbtv.demo.cnn.CnnSync;
import com.usbtv.demo.comm.Aid;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.PlayerController;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.news.NewsStarter;
import com.usbtv.demo.news.video.CcVideoStarter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SyncCenter {

    public static synchronized void syncData(String id) throws SQLException {

        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
        Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);

        Map<Integer, Boolean> keepFoldersMap = new HashMap<Integer, Boolean>();
        Map<String, Boolean> validAidsMap = new HashMap<String, Boolean>();
        Map<String, Integer> typesMap = new LinkedHashMap<>();

        ArrayList<Integer> housekeepTypeIdList = new ArrayList<>();

        int startTypeId=100;
        final int biliStartTypeId = startTypeId;
        RunCron.run(new RunCron.Period() {
            @Override
            public String id() {
                return "bili";
            }

            @Override
            public long getPeriodDuration() {
                return 3 * 24 * 3600 * 1000;
            }

            @Override
            public void doRun() throws Throwable {
                BiLi.bilibiliVideos(biliStartTypeId,housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap, validAidsMap);
                updateScreenTabs(typesMap);
            }
        }, id);

        /*startTypeId=200;
        final int cnnStartTypeId = startTypeId;
        RunCron.run(new RunCron.Period() {
            @Override
            public String id() {
                return "cnn";
            }

            @Override
            public long getPeriodDuration() {
                return 2 * 3600 * 1000;
            }

            @Override
            public void doRun() throws Throwable {
                CnnSync.cnnVideos(cnnStartTypeId,housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);
                updateScreenTabs(typesMap);
            }
        }, id);*/

        startTypeId=300;
        final int tvStartTypeId = startTypeId;

        RunCron.run(new RunCron.Period() {
            @Override
            public String id() {
                return "tv";
            }

            @Override
            public long getPeriodDuration() {
                return  24 * 3600 * 1000;
            }

            @Override
            public void doRun() throws Throwable {
                TV.liveStream(tvStartTypeId,housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);

            }
        }, id);




      if(false)  RunCron.run(new RunCron.Period() {
            @Override
            public String id() {
                return "news";
            }

            @Override
            public long getPeriodDuration() {
                return 1000 * 3600 * 6;
            }

            @Override
            public void doRun() throws Throwable {
                NewsStarter.run();
            }
        }, id);

        RunCron.run(new RunCron.Period() {
            @Override
            public String id() {
                return "cc";
            }

            @Override
            public long getPeriodDuration() {
                return 12 * 3600 * 1000;
            }

            @Override
            public void doRun() throws Throwable {
                CcVideoStarter.run();
            }
        }, id);
        RunCron.run(new RunCron.Period() {
            @Override
            public String id() {
                return "local";
            }

            @Override
            public long getPeriodDuration() {
                return 0;
            }

            @Override
            public void doRun() throws Throwable {
                Aid.scanAllDrive(housekeepTypeIdList, typesMap, keepFoldersMap, validAidsMap);
                updateScreenTabs(typesMap);
            }
        }, id);
        QueryBuilder<Folder, Integer> folderBuilder = folderDao.queryBuilder();
        folderBuilder.where().in("typeId", housekeepTypeIdList);
        List<Folder> folders = folderBuilder.query();
        for (Folder folder : folders) {
            if (keepFoldersMap.get(folder.getId()) == null
            ) {
                vFileDao.delete(folder.getFiles());
                folderDao.delete(folder);
            }
        }


        updateScreenTabs(typesMap);

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                TV.checkTvUrls(id);
            }
        }).start();*/
    }

    public static void updateScreenTabs(Map<String, Integer> typesMap) {
        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        Map<String, Integer> map = App.getStoreTypeMap();
        map.putAll(typesMap);

        Iterator<String> it = map.keySet().iterator();

       while (it.hasNext()){
           String cat = it.next();
           if(!isCatHasItem(map.get(cat))){
               it.remove();
           }
       }

        String jsonStr = JSON.toJSONString(map);
        editor.putString("typesMap", jsonStr);
        editor.apply();
        editor.commit();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                PlayerController.getInstance().refreshCats();
            }
        });
    }

    private static boolean isCatHasItem(Integer typeId) {

        try {
            return  (typeId==0 ||  App.getHelper().getDao(Folder.class).queryBuilder().where().eq("typeId",typeId).queryForFirst()!=null);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }


}
