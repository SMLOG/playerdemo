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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// $.post("/api/insert", { url:'http://192.168.3.227:9080/videos.json',typeid:'200',typename:'Video', content: 'a' })
public class VideoList {

    public static void insertVideos( String feedUrl, String jsonContent) throws IOException, SQLException {

        Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);
        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);


        JSONArray jsonArr = null;
        JSONObject rootObject;
        String content = jsonContent;
        if(jsonContent==null|| jsonContent.trim().length()<10){
            content = Utils.get(feedUrl);

        }
        rootObject = JSONObject.parseObject(content);
        jsonArr = rootObject.getJSONArray("data");
        Integer channelId = rootObject.getInteger("id");
        String channel = rootObject.getString("channel");
        if(channelId==null){
            channelId = 200;
        }
        if(channel==null&&channel.trim().isEmpty()){
            channel="Video";
        }

        List<Integer> folderIds = new ArrayList<>();
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

            if(urls.size()>0)
                folderIds.add(folder.getId());

            for (int j = 0; j < urls.size(); j++) {
                String url = null;
                try{
                   url = urls.getString(j);
                }catch (Throwable ee){
                    JSONObject itemObj = urls.getJSONObject(j);
                    url=itemObj.getString("url");
                }

                VFile vf = null;
                vf = new VFile();
                //vf.setName(title);
                vf.setPage(j+1);
                vf.setFolder(folder);
                vf.setdLink(url);
                vf.setOrderSeq(j);
                vFileDao.createOrUpdate(vf);
            }


        }

        if(rootObject.get("clean")!=null){
            List<Folder> shouldDelFolds = folderDao.queryBuilder().where().eq("typeId", channelId).and().notIn("id", folderIds).query();

            List<Integer> shouldDelFolderIds = new ArrayList<>();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                shouldDelFolderIds = shouldDelFolds.stream().map(f -> f.getId()).collect(Collectors.toList());
            }
            List<VFile> shouldDelVfiles = vFileDao.queryBuilder().where().in("folder_id", shouldDelFolderIds).query();
            if(shouldDelVfiles.size()>0)
                vFileDao.delete(shouldDelVfiles);
            if(shouldDelFolds.size()>0)
                folderDao.delete(shouldDelFolds);
        }



        CatType type = new CatType();
        type.setStatus("A");
        type.setTypeId(channelId);
        type.setName(channel);
        App.getCatTypeDao().createOrUpdate(type);

        List<Folder> exitsFolder = folderDao.queryBuilder().where().eq("typeId", channelId).query();
        if(exitsFolder.size()==0)App.getCatTypeDao().delete(type);

        SyncCenter.updateScreenTabs();

    }

}
