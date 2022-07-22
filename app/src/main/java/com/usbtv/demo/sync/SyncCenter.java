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

        RunCron.run(new RunCron.Period("cnn", "cnn", 12l * 3600 * 1000,false) {
            @Override
            public void doRun() throws Throwable {
                CnnSync.cnnVideos(400, housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);
                updateScreenTabs(typesMap);

            }
        }, id);


        String[][] arr = new String[][]{
                new String[]{"oumeiju", "欧美剧",""},
                new String[]{"neidiju", "内地剧",""},
                new String[]{"gangju", "港剧",""},
                new String[]{"taiju", "台剧",""},
                new String[]{"riju", "日剧",""},
                new String[]{"hanju", "韩剧",""},
                new String[]{"taiguoju", "泰剧",""},
                new String[]{"meiman", "美漫",""},
        };

        for (int k = 0; k < arr.length; k++) {
            final int fk = k;
            arr[k][2]= "dsj" + fk;

            RunCron.run(new RunCron.Period(arr[k][2], arr[fk][1], 24l*30 * 3600 * 1000,false) {
                @Override
                public void doRun() throws Throwable {
                    MJ.syncList(500 + fk, arr[fk][0], arr[fk][1], typesMap, folderDao, vFileDao);
                    updateScreenTabs(typesMap);
                }
            }, id);


        }

        RunCron.run(new RunCron.Period("dsj", "dsj", 24l * 3600 * 1000,false) {
            @Override
            public void doRun() throws Throwable {
                MJ.syncFromRecnetlyUpate(500 , arr, typesMap, folderDao, vFileDao);
            }
        }, id);


        RunCron.run(new RunCron.Period("bili", "bili", 3l * 24 * 3600 * 1000,true) {
            @Override
            public void doRun() throws Throwable {
                BiLi.bilibiliVideos(100, housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap, validAidsMap);
                updateScreenTabs(typesMap);
            }
        }, id);



        RunCron.run(new RunCron.Period("bili2", "bili2", 5l * 24 * 3600 * 1000,true) {
            @Override
            public void doRun() throws Throwable {
                BiLi.bilibiliVideosSearchByKeyWord(200, housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap, validAidsMap
                );
                updateScreenTabs(typesMap);
            }
        }, id);

        RunCron.run(new RunCron.Period("tv", "tv", 15l * 24 * 3600 * 1000,true) {
            @Override
            public void doRun() throws Throwable {
                TV.liveStream(300, housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);
                updateScreenTabs(typesMap);
            }
        }, id);

        RunCron.run(new RunCron.Period("local", "local", 0,true) {
            @Override
            public void doRun() throws Throwable {
                Aid.scanAllDrive(housekeepTypeIdList, typesMap, keepFoldersMap, validAidsMap);
                updateScreenTabs(typesMap);
            }
        }, id);

        RunCron.startRunTasks();


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

        while (it.hasNext()) {
            String cat = it.next();
            if (!isCatHasItem(map.get(cat))) {
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
            return (typeId == 0 || App.getHelper().getDao(Folder.class).queryBuilder().where().eq("typeId", typeId).queryForFirst() != null);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }


}
