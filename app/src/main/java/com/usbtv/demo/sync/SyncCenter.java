package com.usbtv.demo.sync;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.usbtv.demo.comm.Aid;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.PlayerController;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.data.Video;
import com.usbtv.demo.news.NewsStarter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncCenter {


    public static synchronized void syncData(String id) throws SQLException {


        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
        Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);

        Map<Integer, Boolean> keepFoldersMap = new HashMap<Integer, Boolean>();
        Map<String, Boolean> validAidsMap = new HashMap<String, Boolean>();
        Map<String, Integer> typesMap = new LinkedHashMap<>();

        ArrayList<Integer> housekeepTypeIdList = new ArrayList<>();

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
                BiLi.bilibiliVideos(housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap, validAidsMap);
                updateScreenTabs(typesMap);
            }
        }, id);

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
                cnnVideos(housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);
                updateScreenTabs(typesMap);
            }
        }, id);

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
                TV.liveStream(housekeepTypeIdList, typesMap, folderDao, vFileDao, keepFoldersMap);
                updateScreenTabs(typesMap);

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


    public static void cnnVideos(ArrayList<Integer> housekeepTypeIdList, Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> keepFoldersMap) throws IOException, SQLException {
        int channelId = 3;

        String[] urls = new String[]{
                "https://edition.cnn.com/playlist/top-news-videos/index.json",
                "https://edition.cnn.com/video/data/3.0/video/business/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/health/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/politics/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/tech/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/world/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/economy/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/us/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/uk/relateds.json",

        };

        Dao<Video, Integer> videoDao = App.getHelper().getDao(Video.class);

        for (String feedUrl : urls) {

            String resp = Utils.get(feedUrl);

            JSONArray jsonArr = null;
            boolean isTop = feedUrl.indexOf("index.json") > -1;

            if (isTop) jsonArr = JSONObject.parseArray(resp);
            else jsonArr = JSON.parseObject(resp).getJSONArray("videos");

            Pattern pattern = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");
            for (int i = 0; i < jsonArr.size(); i++) {
                JSONObject item = (JSONObject) jsonArr.get(i);

                String videoId = item.getString(isTop ? "videoId" : "id");
                String title = item.getString(isTop ? "title" : "headline");
                String folderName = null;

                Matcher matcher = pattern.matcher(videoId);
                int seq = 0;
                if (matcher.find()) {
                    String dateStr = matcher.group();
                    folderName = dateStr;
                    seq = Integer.parseInt(dateStr.replaceAll("/", ""));

                } else continue;


                String imageUrl = "http:" + item.getString(isTop ? "imageUrl" : "endslate_url_small");

                Folder folder = folderDao.queryBuilder().where().eq("typeId", channelId).and().eq("name", folderName).queryForFirst();


                //videoDao.queryForAll();
                Video video = videoDao.queryBuilder().where().eq("videoId", videoId).queryForFirst();
                if (video != null) continue;

                resp = Utils.get("https://fave.api.cnn.io/v1/video?id=" + videoId + "&customer=cnn&edition=international&env=prod");
                JSONObject obj = JSONObject.parseObject(resp);

                String mediumId = obj.getString("mediumId");

                resp = Utils.get("https://medium.ngtv.io/media/" + mediumId + "?appId=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6ImNubi1jbm4td2ViLTk1am96MCIsIm5ldHdvcmsiOiJjbm4iLCJwbGF0Zm9ybSI6IndlYiIsInByb2R1Y3QiOiJjbm4iLCJpYXQiOjE1MjQ2ODQwMzB9.Uw8riFJwARLjeE35ffMwSa-37RNxCcQUEp2pqwG9TvM");

                obj = JSONObject.parseObject(resp);
                if (obj == null) {
                    continue;
                }
                String url = Utils.getObject(obj, "media.tv.unprotected.url");
                if (url == null) continue;
                System.out.println(url);


                if (folder == null) {

                    folder = new Folder();
                    folder.setTypeId(channelId);
                    folder.setName(folderName);
                    folder.setCoverUrl(imageUrl);
                    folder.setOrderSeq(seq);
                    folderDao.createOrUpdate(folder);

                    Folder folder2 = new Folder();
                    folder2.setTypeId(4);
                    folder2.setName(folderName);
                    folder2.setCoverUrl(imageUrl);
                    folder2.setOrderSeq(seq);
                    folderDao.createOrUpdate(folder2);


                    VFile vf2 = new VFile();
                    vf2.setName(title);
                    vf2.setFolder(folder2);

                    vf2.setdLink(SSLSocketClient.ServerManager.getServerHttpAddress() + "/hls/vod.m3u8?folderId=" + folder.getId());
                    vf2.setOrderSeq(0);

                    vFileDao.createOrUpdate(vf2);


                } else folder.setCoverUrl(imageUrl);

                folderDao.createOrUpdate(folder);

                VFile vf = vFileDao.queryBuilder().where().eq("folder_id", folder.getId()).and().eq("dLink", url).queryForFirst();
                if (vf == null) {
                    vf = new VFile();
                    vf.setName(title);
                    vf.setFolder(folder);
                    vf.setdLink(url);
                    vf.setOrderSeq(seq);
                    vFileDao.createOrUpdate(vf);
                }

                video = new Video();
                video.setVideoId(videoId);
                video.setTitle(title);
                video.setCoverUrl(imageUrl);
                video.setUrl(url);
                video.setDt(Integer.parseInt(folderName.replaceAll("/", "")));
                videoDao.createOrUpdate(video);
            }

        }

        typesMap.put("CNN2", 4);
        typesMap.put("CNN", channelId);
        // housekeepTypeIdList.add(3);
        // housekeepTypeIdList.add(4);

    }


}
