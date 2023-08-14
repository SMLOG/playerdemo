package com.usbtv.demo.cnn;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.CatType;
import com.usbtv.demo.data.Feed;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Start {
    public static final SimpleDateFormat yyymmmddd = new SimpleDateFormat("yyyy-MM-dd");

    public static void sync(RunCron.Period srcPeriod, int cnnStartTypeId, ArrayList<Integer> housekeepTypeIdList,
                            Dao<Folder, Integer> folderDao,
                            Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> keepFoldersMap, Feed feed) throws IOException, SQLException {
        int channelId = cnnStartTypeId;


        int keepdate = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date())) - feed.keepDate;

        String resp = Utils.get(feed.url);
        JSONObject jsonObj = JSONObject.parseObject(resp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        JSONArray medias = (JSONArray) (jsonObj.get("subCards"));
        for (int i = 0; i < medias.size(); i++) {

            try {
                jsonObj = (JSONObject) medias.get(i);
                JSONArray vfs = jsonObj.getJSONArray("externalVideoFiles");
                String m3u8 = null;
                String cc = null;
                String folderName = yyymmmddd.format(dateFormat.parse(jsonObj.getString("publishedDateTime")));
                String imageUrl = jsonObj.getJSONArray("images").getJSONObject(0).getString("url");
                String title = jsonObj.getString("title");
                for (int j = 0; j < vfs.size(); j++) {
                    String link = vfs.getJSONObject(j).getString("url");
                    if (link.indexOf("m3u8-aapl") > -1) {
                        m3u8 = link;
                        break;
                    }
                }
                if (m3u8 != null) {
                    JSONArray closedCaptions = jsonObj.getJSONObject("videoMetadata").getJSONArray("closedCaptions");
                    if (closedCaptions != null && closedCaptions.size() > 0)
                        cc = closedCaptions.getJSONObject(0).getString("href");
                    System.out.println(m3u8);
                    System.out.println(cc);

                    Folder folder = folderDao.queryBuilder().where().eq("typeId", channelId)
                            .and().eq("name", folderName).queryForFirst();

                    if (folder == null) {

                        folder = new Folder();
                        folder.setTypeId(channelId);
                        folder.setName(folderName);
                        folder.setCoverUrl(imageUrl);
                        folder.setOrderSeq(Integer.parseInt(folderName.replaceAll("\\-", "")));
                        folderDao.createOrUpdate(folder);
                        folder.setJob(srcPeriod.getId());

                    } else folder.setCoverUrl(imageUrl);

                    folderDao.createOrUpdate(folder);

                    VFile vf = vFileDao.queryBuilder().where().eq("folder_id", folder.getId()).and().eq("dLink", m3u8).queryForFirst();
                    if (vf == null) {
                        vf = new VFile();
                        vf.setName(title);
                        vf.setFolder(folder);
                        vf.setdLink(m3u8);
                        vf.setOrderSeq((int) (1e15 - System.currentTimeMillis()));
                        vf.setCc(cc);
                        vFileDao.createOrUpdate(vf);
                    }

                }
            }catch (Throwable e){
                e.printStackTrace();
            }


        }


        CatType type = new CatType();
        type.setStatus("A");
        type.setJob(srcPeriod.getId());
        type.setTypeId(channelId);
        type.setName(feed.name);
        App.getCatTypeDao().createOrUpdate(type);

        List<Folder> dels = folderDao.queryBuilder().where().eq("typeId", channelId).and().lt("orderSeq", keepdate).query();
        if (dels.size() > 0) folderDao.delete(dels);

    }

}
