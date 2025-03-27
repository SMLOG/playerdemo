package com.usbtv.demo.sync;


import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.usbtv.demo.ConfigStore;
import com.usbtv.demo.PlayerController;
import com.usbtv.demo.comm.Aid;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.CatType;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import org.jsoup.internal.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SyncCenter {

    public static void syncData(String id) throws SQLException {


        Map<Integer, Boolean> keepFoldersMap = new HashMap<Integer, Boolean>();
        Map<String, Boolean> validAidsMap = new HashMap<String, Boolean>();

        ArrayList<Integer> housekeepTypeIdList = new ArrayList<>();

        Dao<Folder, Integer> folderDao = App.getFolderDao();
        Dao<VFile, Integer> vFileDao = App.getVFileDao();

        if (RunCron.peroidMap.size() == 0) {

            RunCron.addPeriod(new RunCron.Period("Update Feed", "Update Feed", 2592000l * 1000, true) {
                @Override
                public void doRun() throws Throwable {

                    String content = Utils.get( PlayerController.getInstance().configStore.configUrl);
                    ConfigStore configStore = JSON.parseObject(content, ConfigStore.class);
                    PlayerController.getInstance().configStore=configStore;
                    PlayerController.getInstance().configStore.save();

                }

            });

            RunCron.addPeriod(new RunCron.Period("tv", "tv", 15l * 24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    TV.liveStream(this, 300, housekeepTypeIdList, folderDao, vFileDao, keepFoldersMap);
                    updateScreenTabs();
                }
            });
            RunCron.addPeriod(new RunCron.Period("Center", "Center",  15l * 24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    if( PlayerController.getInstance().configStore.playList!=null)
                        for(String str: PlayerController.getInstance().configStore.playList){
                            try{
                                if(!StringUtil.isBlank(str))VideoList.insertVideos(str,null,true);
                            }catch (Throwable ee){
                                ee.printStackTrace();
                            }
                        }


                    updateScreenTabs();
                }
            });


            List<CatType> types = App.getCatTypeDao().queryForAll();
            for(CatType type:types){
                try{
                    if(!StringUtil.isBlank(type.getUrl())){
                        RunCron.addPeriod(new RunCron.Period(type.getTypeId()+"", type.getName(),  24 * 3600 * 1000, true) {
                            @Override
                            public void doRun() throws Throwable {
                                VideoList.insertVideos(type.getUrl(),null,true);

                                updateScreenTabs();
                            }
                        });
                    }
                }catch (Throwable ee){
                    ee.printStackTrace();
                }
            }

            if(false)RunCron.addPeriod(new RunCron.Period("Sync", "Sync",  24 * 3600 * 1000, true) {
                @Override
                public void doRun() throws Throwable {
                    List<CatType> types = App.getCatTypeDao().queryForAll();
                    for(CatType type:types){
                        try{
                            if(!StringUtil.isBlank(type.getUrl()))VideoList.insertVideos(type.getUrl(),null,true);
                        }catch (Throwable ee){
                            ee.printStackTrace();
                        }
                    }

                    updateScreenTabs();
                }
            });


           if(false) RunCron.addPeriod(new RunCron.Period("local", "local", 0, true) {
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
