package com.usbtv.demo.cnn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.data.Video;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CnnSync {

    public static void cnnVideos(int cnnStartTypeId, ArrayList<Integer> housekeepTypeIdList, Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> keepFoldersMap) throws IOException, SQLException {
        int channelId = cnnStartTypeId;
        //int typeId = cnnStartTypeId + 1;

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
        Pattern pattern = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");
        int keepdate = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))-7;

        for (String feedUrl : urls) {

            String resp = Utils.get(feedUrl);

            JSONArray jsonArr = null;
            boolean isTop = feedUrl.indexOf("index.json") > -1;

            if (isTop) jsonArr = JSONObject.parseArray(resp);
            else jsonArr = JSON.parseObject(resp).getJSONArray("videos");

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

                if( seq  < keepdate){
                    continue;
                }

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

                   /* Folder folder2 = new Folder();
                    folder2.setTypeId(typeId);
                    folder2.setName(folderName);
                    folder2.setCoverUrl(imageUrl);
                    folder2.setOrderSeq(seq);
                    folderDao.createOrUpdate(folder2);


                    VFile vf2 = new VFile();
                    vf2.setName(title);
                    vf2.setFolder(folder2);

                    vf2.setdLink(":/cnn/vod.m3u8?ymd=" + seq);
                    vf2.setOrderSeq(0);

                    vFileDao.createOrUpdate(vf2);*/


                } else folder.setCoverUrl(imageUrl);

                folderDao.createOrUpdate(folder);

                video = new Video();
                video.setVideoId(videoId);
                video.setTitle(title);
                video.setCoverUrl(imageUrl);
                video.setUrl(url);
                video.setYmd(seq);
                video.setDt(Integer.parseInt(folderName.replaceAll("/", "")));
                videoDao.createOrUpdate(video);

                VFile vf = vFileDao.queryBuilder().where().eq("folder_id", folder.getId()).and().eq("dLink", url).queryForFirst();
                if (vf == null) {
                    vf = new VFile();
                    vf.setName(title);
                    vf.setFolder(folder);
                    vf.setdLink(url);
                   // vf.setdLink(":/cnn/video.m3u8?videoId="+videoId);
                    vf.setOrderSeq((int) (-System.currentTimeMillis()/10000));
                    vFileDao.createOrUpdate(vf);
                }


            }

        }
       // typesMap.put("CNN2", typeId);

        typesMap.put("CNN", channelId);
       // housekeepTypeIdList.add(channelId);
        List<Folder> dels = folderDao.queryBuilder().where().eq("typeId", channelId).and().lt("orderSeq", keepdate).query();
        if(dels.size()>0)folderDao.delete(dels);
        // housekeepTypeIdList.add(4);

    }

}
