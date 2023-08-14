package com.usbtv.demo.sync;

import static com.usbtv.demo.sync.SyncCenter.updateScreenTabs;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.PlayerController;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.CatType;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Yt {

    private static V8ScriptEngine v8scriptEngine;


    public static void getList(RunCron.Period srcPeriod, final int startTypeId, ArrayList<Integer> housekeepTypeIdList,
                               Dao<Folder, Integer> folderDao,
                               Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> validFoldersMap,
                               Map<String, Boolean> validAidsMap) throws IOException, SQLException {


        String[] playListUrls = PlayerController.getInstance().configStore.ytPlayList;
        for (int d = 0; d < playListUrls.length; d++) {


            Integer btypeId = startTypeId + d;

            Document resp = Jsoup.connect(playListUrls[d])//.cookies(cookies)
                    .userAgent(Utils.AGENT).get();
            String string = resp.outerHtml();
            //System.out.println(string);
            int beg = string.indexOf("var ytInitialData =");
            String json = string.substring(
                    beg + "var ytInitialData =".length(),
                    string.indexOf("</script>", beg) - 1
            );
            //System.out.println(json);
            JSONObject jobj = JSON.parseObject(json.replace(";<", ""));
            String catName = jobj.getJSONObject("metadata").getJSONObject("playlistMetadataRenderer").getString("title");
            JSONArray list = jobj.getJSONObject("contents")
                    .getJSONObject("twoColumnBrowseResultsRenderer")
                    .getJSONArray("tabs").getJSONObject(0)
                    .getJSONObject("tabRenderer")
                    .getJSONObject("content")
                    .getJSONObject("sectionListRenderer")
                    .getJSONArray("contents").getJSONObject(0)
                    .getJSONObject("itemSectionRenderer")
                    .getJSONArray("contents").getJSONObject(0)
                    .getJSONObject("playlistVideoListRenderer")
                    .getJSONArray("contents");

            //contents.twoColumnBrowseResultsRenderer.tabs[0].tabRenderer.content.sectionListRenderer.contents[0].itemSectionRenderer
            //.contents[0].playlistVideoListRenderer.contents[0].playlistVideoRenderer.title.runs[0].text
            for (int i = 0, t = list.size(); i < list.size(); i++) {
                JSONObject it = list.getJSONObject(i);
                String title = it.getJSONObject("playlistVideoRenderer").getJSONObject("title").getJSONArray("runs").getJSONObject(0).getString("text");
                String bvid = it.getJSONObject("playlistVideoRenderer").getString("videoId");
                String cover = it.getJSONObject("playlistVideoRenderer").getJSONObject("thumbnail").getJSONArray("thumbnails").getJSONObject(0).getString("url");


                String folderTitle = title;

                Folder folder;
                String aid = bvid;


                folder = folderDao.queryBuilder().where().eq("aid", aid).queryForFirst();

                if (folder == null) {

                    folder = new Folder();

                }

                folder.setJob(srcPeriod.getId());
                folder.setBvid(bvid);
                folder.setAid(aid);
                folder.setOrderSeq(t - i);
                folder.setTypeId(btypeId);
                folder.setName(folderTitle);
                folder.setCoverUrl(cover);

                folderDao.createOrUpdate(folder);

                validFoldersMap.put(folder.getId(), true);
                validAidsMap.put(title, true);

                for (int k = 1; k <= 1; k++) {

                    VFile vfile = vFileDao.queryBuilder().where()/*.eq("folder_id", folder.getId())
                                .and()*/.eq("bvid", bvid).and().eq("page", k).queryForFirst();
                    if (vfile == null) {
                        vfile = new VFile();
                    }
                    vfile.setFolder(folder);
                    vfile.setBvid(bvid);
                    vfile.setAid(aid);
                    vfile.setPage(k);
                    vfile.setOrderSeq(k);
                    vfile.setName(title);
                    vfile.setExt(".mp4");
                    vFileDao.createOrUpdate(vfile);

                }


            }


            CatType catType = new CatType();
            catType.setStatus("A");
            catType.setJob(srcPeriod.getId());
            catType.setName(catName);
            catType.setTypeId(btypeId);
            App.getCatTypeDao().createOrUpdate(catType);
            updateScreenTabs();

        }

    }

}
