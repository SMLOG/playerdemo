package com.usbtv.demo.sync;


import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.usbtv.demo.PlayerController;
import com.usbtv.demo.cnn.CnnSync;
import com.usbtv.demo.cnn.MSN;
import com.usbtv.demo.comm.Aid;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SyncCenter {


    public static final String CNN = "cnn";

    public static  void syncData(String id) throws SQLException {



        Map<Integer, Boolean> keepFoldersMap = new HashMap<Integer, Boolean>();
        Map<String, Boolean> validAidsMap = new HashMap<String, Boolean>();

        ArrayList<Integer> housekeepTypeIdList = new ArrayList<>();

        Dao<Folder, Integer> folderDao = App.getFolderDao();
        Dao<VFile, Integer> vFileDao = App.getVFileDao();

        if (RunCron.peroidMap.size()==0) {

            RunCron.addPeriod(new RunCron.Period(CNN, CNN, 12l * 3600 * 1000, false) {
                @Override
                public void doRun() throws Throwable {
                    CnnSync.cnnVideos(this, 400, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap);
                    updateScreenTabs();
                }

            });
            RunCron.addPeriod(new RunCron.Period("MSN", "MSN", 12l * 3600 * 1000, false) {
                @Override
                public void doRun() throws Throwable {
                    MSN.sync(this, 400, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap);
                    updateScreenTabs();
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
                        MJ2.syncList(this, 500 + fk, arr[fk][0], arr[fk][1], folderDao, vFileDao);
                        updateScreenTabs();
                    }
                });


            }

            RunCron.addPeriod(new RunCron.Period("dsj", "dsj", 24l * 3600 * 1000, false) {
                @Override
                public void doRun() throws Throwable {
                    MJ2.syncFromRecnetlyUpate(this, 500, arr, folderDao, vFileDao);
                }
            });

            RunCron.addPeriod(new RunCron.Period("bili", "bili", 3l * 24 * 3600 * 1000, false) {
                @Override
                public void doRun() throws Throwable {
                    BiLi.bilibiliVideos(this, 100, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap, validAidsMap);
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
                    Aid.scanAllDrive(this, housekeepTypeIdList,  keepFoldersMap, validAidsMap);
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
