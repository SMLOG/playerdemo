package com.usbtv.demo.sync;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.usbtv.demo.comm.Aid;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.PlayerController;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.news.NewsStarter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

        startTypeId+=100;
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
                Cnn.cnnVideos(cnnStartTypeId,housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);
                updateScreenTabs(typesMap);
            }
        }, id);

        startTypeId+=100;
        final int tvStartTypeId = startTypeId;

        RunCron.run(new RunCron.Period() {
            @Override
            public String id() {
                return "tv";
            }

            @Override
            public long getPeriodDuration() {
                return 15 * 24 * 3600 * 1000;
            }

            @Override
            public void doRun() throws Throwable {
                TV.liveStream(tvStartTypeId,housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);

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


        RunCron.run(new RunCron.Period() {
            @Override
            public String id() {
                return "news";
            }

            @Override
            public long getPeriodDuration() {
                return 1000 * 3600 * 2;
            }

            @Override
            public void doRun() throws Throwable {
                NewsStarter.run();
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
    }

    public static void updateScreenTabs(Map<String, Integer> typesMap) {
        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        Map<String, Integer> map = App.getStoreTypeMap();
        map.putAll(typesMap);
        String jsonStr = JSON.toJSONString(map);
        editor.putString("typesMap", jsonStr);
        editor.apply();
        editor.commit();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                PlayerController.getInstance().reloadMoviesList();
            }
        });
    }





}
