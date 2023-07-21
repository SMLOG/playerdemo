package com.usbtv.demo.cnn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.CatType;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.data.Video;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MSN {
    public static final SimpleDateFormat yyymmmddd = new SimpleDateFormat("yyyy-MM-dd");
    static String url="https://assets.msn.com/service/MSN/Feed/me?$top=20&DisableTypeSerialization=true&activityId=7E2F0C44-D701-4998-A932-A31D48A50A60&apikey=0QfOX3Vn51YCzitbLaRkTTBadtWpgTN8NZLW0C1SEM&contentType=video&location=21.3744|110.248&market=en-us&query=news%20video&queryType=myfeed&responseSchema=cardview&timeOut=1000&wrapodata=false?t=234686";

    public static void sync(RunCron.Period srcPeriod, int cnnStartTypeId, ArrayList<Integer> housekeepTypeIdList,
                            Dao<Folder, Integer> folderDao,
                            Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> keepFoldersMap) throws IOException, SQLException {
        int channelId = cnnStartTypeId;


        int keepdate = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date())) - 7;

        String resp = Utils.get(url);
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
        type.setName("News");
        App.getCatTypeDao().createOrUpdate(type);

        List<Folder> dels = folderDao.queryBuilder().where().eq("typeId", channelId).and().lt("orderSeq", keepdate).query();
        if (dels.size() > 0) folderDao.delete(dels);

    }

}
