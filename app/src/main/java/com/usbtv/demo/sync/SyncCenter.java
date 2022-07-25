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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SyncCenter {

    private static Dao<Folder, Integer> folderDao = null;
    private static Dao<VFile, Integer> vFileDao = null;
    public static synchronized void syncData(String id) throws SQLException {

        if (folderDao == null) folderDao = App.getHelper().getDao(Folder.class);

        if (vFileDao == null) vFileDao = App.getHelper().getDao(VFile.class);

        Map<Integer, Boolean> keepFoldersMap = new HashMap<Integer, Boolean>();
        Map<String, Boolean> validAidsMap = new HashMap<String, Boolean>();
        Map<String, Integer> typesMap = new LinkedHashMap<>();

        ArrayList<Integer> housekeepTypeIdList = new ArrayList<>();

        if (RunCron.peroidMap == null) {

            RunCron.addPeriod(new RunCron.Period("cnn", "cnn", 12l * 3600 * 1000, false) {
                @Override
                public void doRun() throws Throwable {
                    CnnSync.cnnVideos(this, 400, housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);
                    updateScreenTabs(typesMap);

                }

            });


            String[][] arr = new String[][]{
                    new String[]{"oumeiju", "欧美剧", ""},
                    new String[]{"neidiju", "内地剧", ""},
                    new String[]{"gangju", "港剧", ""},
                    new String[]{"taiju", "台剧", ""},
                    new String[]{"riju", "日剧", ""},
                    new String[]{"hanju", "韩剧", ""},
                    new String[]{"taiguoju", "泰剧", ""},
                    new String[]{"meiman", "美漫", ""},
            };

            for (int k = 0; k < arr.length; k++) {
                final int fk = k;
                arr[k][2] = "dsj" + fk;

                RunCron.addPeriod(new RunCron.Period(arr[k][2], arr[fk][1], 24l * 30 * 3600 * 1000, false) {
                    @Override
                    public void doRun() throws Throwable {
                        MJ.syncList(this, 500 + fk, arr[fk][0], arr[fk][1], typesMap, folderDao, vFileDao);
                        updateScreenTabs(typesMap);
                    }
                });


            }

            RunCron.addPeriod(new RunCron.Period("dsj", "dsj", 24l * 3600 * 1000, false) {
                @Override
                public void doRun() throws Throwable {
                    MJ.syncFromRecnetlyUpate(this, 500, arr, typesMap, folderDao, vFileDao);
                }
            });

            RunCron.addPeriod(new RunCron.Period("bili", "bili", 3l * 24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    BiLi.bilibiliVideos(this, 100, housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap, validAidsMap);
                    updateScreenTabs(typesMap);
                }
            });

            RunCron.addPeriod(new RunCron.Period("bili2", "bili2", 5l * 24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    BiLi.bilibiliVideosSearchByKeyWord(this, 200, housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap, validAidsMap
                    );
                    updateScreenTabs(typesMap);
                }
            });

            RunCron.addPeriod(new RunCron.Period("tv", "tv", 15l * 24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    TV.liveStream(this, 300, housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);
                    updateScreenTabs(typesMap);
                }
            });

            RunCron.addPeriod(new RunCron.Period("local", "local", 0, true) {
                @Override
                public void doRun() throws Throwable {
                    Aid.scanAllDrive(this, housekeepTypeIdList, typesMap, keepFoldersMap, validAidsMap);
                    updateScreenTabs(typesMap);
                }
            });
        }


        RunCron.addToQueue(id);

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
