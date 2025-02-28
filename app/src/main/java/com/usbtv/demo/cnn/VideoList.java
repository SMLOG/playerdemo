package com.usbtv.demo.cnn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.CatType;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.sync.SyncCenter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// $.post("/api/insert", { url:'http://192.168.3.227:9080/videos.json',typeid:'200',typename:'Video', content: 'a' })
public class VideoList {

    public static void insertVideos(int channelId, String feedUrl, String jsonContent,
                                     String typename) throws IOException, SQLException {

        Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);
        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);


        JSONArray jsonArr = null;
        if(jsonContent!=null&&!jsonContent.trim().equals("")&&jsonContent.length()>10){
            jsonArr = JSONArray.parseArray(jsonContent);
        }else{
            String resp = Utils.get(feedUrl);
            jsonArr = JSON.parseArray(resp);
        }

        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject item = (JSONObject) jsonArr.get(i);

            String folderName = item.getString("title");

            String imageUrl = item.getString("img");

            Folder folder = folderDao.queryBuilder().where().eq("typeId", channelId).and().eq("name", folderName).queryForFirst();


            if (folder == null) {

                folder = new Folder();
                folder.setTypeId(channelId);
                folder.setName(folderName);
                folder.setCoverUrl(imageUrl);
                folder.setOrderSeq(i);
                folderDao.createOrUpdate(folder);


            } else folder.setCoverUrl(imageUrl);

            folderDao.createOrUpdate(folder);


            JSONArray urls = item.getJSONArray("urls");
            List<VFile> exists = vFileDao.queryBuilder().where().eq("folder_id", folder.getId()).query();

            vFileDao.delete(exists);

            for (int j = 0; j < urls.size(); j++) {
                JSONObject itemObj = urls.getJSONObject(j);
                String url = itemObj.getString("url");
                VFile vf = null;
                vf = new VFile();
                //vf.setName(title);
                vf.setFolder(folder);
                vf.setdLink(url);
                vf.setOrderSeq(j);
                vFileDao.createOrUpdate(vf);
            }

            exists = vFileDao.queryBuilder().where().eq("folder_id", folder.getId()).query();
            if(exists.size()==0){
                folderDao.delete(folder);
            }
        }






        CatType type = new CatType();
        type.setStatus("A");
        type.setTypeId(channelId);
        type.setName(typename);
        App.getCatTypeDao().createOrUpdate(type);

        List<Folder> exitsFolder = folderDao.queryBuilder().where().eq("typeId", channelId).and().query();
        if(exitsFolder.size()==0)App.getCatTypeDao().delete(type);

        SyncCenter.updateScreenTabs();

    }

}
