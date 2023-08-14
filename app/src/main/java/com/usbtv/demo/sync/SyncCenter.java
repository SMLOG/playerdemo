package com.usbtv.demo.sync;


import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.usbtv.demo.ConfigStore;
import com.usbtv.demo.PlayerController;
import com.usbtv.demo.cnn.CnnSync;
import com.usbtv.demo.cnn.Start;
import com.usbtv.demo.comm.Aid;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Feed;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SyncCenter {


    public static final String CNN = "cnn";

    public static void syncData(String id) throws SQLException {


        Map<Integer, Boolean> keepFoldersMap = new HashMap<Integer, Boolean>();
        Map<String, Boolean> validAidsMap = new HashMap<String, Boolean>();

        ArrayList<Integer> housekeepTypeIdList = new ArrayList<>();

        Dao<Folder, Integer> folderDao = App.getFolderDao();
        Dao<VFile, Integer> vFileDao = App.getVFileDao();

        if (RunCron.peroidMap.size() == 0) {

            RunCron.addPeriod(new RunCron.Period(CNN, CNN, 12l * 3600 * 1000, false) {
                @Override
                public void doRun() throws Throwable {
                    CnnSync.cnnVideos(this, 400, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap);
                    updateScreenTabs();
                }

            });

            RunCron.addPeriod(new RunCron.Period("Update Feed", "Update Feed", 2592000l * 1000, true) {
                @Override
                public void doRun() throws Throwable {

                    String content = Utils.get( PlayerController.getInstance().configStore.configUrl);
                    ConfigStore configStore = JSON.parseObject(content, ConfigStore.class);
                    PlayerController.getInstance().configStore=configStore;
                    PlayerController.getInstance().configStore.save();
                    attachFeeds(keepFoldersMap, housekeepTypeIdList, folderDao, vFileDao);

                }

            });

            attachFeeds(keepFoldersMap, housekeepTypeIdList, folderDao, vFileDao);

            RunCron.addPeriod(new RunCron.Period("bili", "bili", 3l * 24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    BiLi.bilibiliVideos(this, 100, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap, validAidsMap);
                    updateScreenTabs();
                }
            });
            RunCron.addPeriod(new RunCron.Period("yt", "yt", 3l * 24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    Yt.getList(this, 800, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap, validAidsMap);
                    updateScreenTabs();
                }
            });
           /* RunCron.addPeriod(new RunCron.Period("bili2", "bili2", 5l * 24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    BiLi.bilibiliVideosSearchByKeyWord(this, 200, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap, validAidsMap
                    );
                    updateScreenTabs();
                }
            });*/

            RunCron.addPeriod(new RunCron.Period("tv", "tv", 15l * 24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    TV.liveStream(this, 300, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap);
                    updateScreenTabs();
                }
            });

            RunCron.addPeriod(new RunCron.Period("local", "local", 0, true) {
                @Override
                public void doRun() throws Throwable {
                    Aid.scanAllDrive(this, housekeepTypeIdList, keepFoldersMap, validAidsMap);
                    updateScreenTabs();
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


        updateScreenTabs();

    }

    private static void attachFeeds(Map<Integer, Boolean> keepFoldersMap, ArrayList<Integer> housekeepTypeIdList, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao) {
        List<Feed> feeds = PlayerController.getInstance().configStore.feeds;
        if (feeds != null)
            for (int i = 0; i < feeds.size(); i++) {
                Feed feed = feeds.get(i);
                int finalI = i;
                RunCron.addPeriod(new RunCron.Period(feed.name, feed.name, feed.refreshTime, feed.isDefRun) {
                    @Override
                    public void doRun() throws Throwable {
                        Start.sync(this, 401 + finalI, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap, feed);
                        updateScreenTabs();
                    }

                });
            }
    }

    public static void updateScreenTabs() {


        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                PlayerController.getInstance().refreshCats();
            }
        });
    }


}
