package com.usbtv.demo.sync;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.MainActivity;
import com.usbtv.demo.comm.Aid;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.PlayerController;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.data.Video;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SyncCenter {


    public static JSONObject getVidoInfo(String bvid, Integer p) {

        try {
            JSONObject info = getVideoInfo(getJsEngine(), "https://www.bilibili.com/video/" + bvid + "?p=" + p + "&spm_id_from=pageDriver");
            info = info.getJSONObject("data");
            return info;
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    public static JSONObject getVideoInfo(ScriptEngine scriptEngine, String link) throws ScriptException, IOException {
        int i=5;
        JSONObject jsonObj = null;
        while(--i >0){



        String e = (String) scriptEngine.eval("Math.random().toString(10).substring(2)");

        String n = (String) scriptEngine.eval("generateStr('" + link + "@" + e + "').toString(10)");
        String o = "X-Client-Data";
        String a = "https://service0.iiilab.com";
        String site = "bilibili";

        String xclientdata = (String) scriptEngine.eval("u('" + n + "', '" + site + "')");

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(30, TimeUnit.SECONDS)//设置读取超时时间
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())//配置
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier()).build();

        Request request = new Request
                .Builder()
                .url("https://bilibili.iiilab.com/")
                .addHeader("User-Agent", Utils.AGENT)
                .build();

        Call call = okHttpClient.newCall(request);
        Response response = call.execute();

        List<String> cookies = new ArrayList<String>();
        for (String cookie :
                response.headers().values("Set-Cookie"))
            cookies.add(cookie.split(";")[0]);

        //cookies.add("ppp0609=1");
        //cookies.add("ppp0627=1");
       // cookies.add("zzz0821=1");

        request = new Request
                .Builder()
                .url("https://wx.iiilab.com/static/js/human.min.js?v21")
                .addHeader("User-Agent", Utils.AGENT)
                .build();

        call = okHttpClient.newCall(request);
        response = call.execute();
        String body = response.body().string();

        Pattern p = Pattern.compile("setCookie\\(\"(.*?)\",(.*?),.*?\\)");
        Matcher matcher = p.matcher(body);
        while(matcher.find()) {
            String name = matcher.group(1);
            String value = matcher.group(2);
            if(value.equals("new Date().getTime()"))value = ""+System.currentTimeMillis();
            cookies.add(name+"="+value);

        }

        String url = "https://service0.iiilab.com/sponsor/getByPage";

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        okhttp3.RequestBody requestBody = new FormBody.Builder().add("page", "bilibili").build();

        request = new Request.Builder().url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("Origin", "https://bilibili.iiilab.com/")
                .addHeader("Referer", "https://bilibili.iiilab.com/").addHeader("User-Agent", Utils.AGENT)
                .addHeader("Cookie", Utils.join(";", cookies))

                .post(requestBody).build();
        call = okHttpClient.newCall(request);
        response = call.execute();


        for (String cookie :
                response.headers().values("Set-Cookie"))
            cookies.add(cookie.split(";")[0]);

        String rsp = response.body().string();



        requestBody = new FormBody.Builder().add("link", link).add("r", e)
                .add("s", n).build();

        request = new Request.Builder().url(a + "/video/web/" + site)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("Origin", "https://bilibili.iiilab.com/")
                .addHeader("Referer", "https://bilibili.iiilab.com/")
                .addHeader("User-Agent", Utils.AGENT)
                .addHeader("Cookie", Utils.join(";", cookies))
                .addHeader("X-Client-Data", xclientdata)

                .post(requestBody).build();
        call = okHttpClient.newCall(request);
        response = call.execute();
        rsp = response.body().string();
        System.out.println(rsp);
         jsonObj = JSONObject.parseObject(rsp);

        if(jsonObj.getString("retDesc")!=null&&jsonObj.getString("retDesc").equals("outstanding")) {

            String str=new String(Base64.decode(jsonObj.getString("data").substring(6),Base64.DEFAULT));
            jsonObj.put("data", JSON.parse(str));
        }
        if(jsonObj.getJSONObject("data").getString("video").indexOf("upos-sz-mirrorcos")==-1)continue;

        System.out.println(e);
        return jsonObj;

        }
        return jsonObj;

    }

    public static void main(String[] args) throws ScriptException, IOException, SQLException {

        syncData(false);
    }

    public static void syncData(boolean force) throws  IOException, SQLException {

        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        if( ! force &&  System.currentTimeMillis()-sp.getLong("lastSynTime",0l)<2*60*60*1000)return;

        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
        Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);
        //Drive rootDriv = App.getDefaultRootDrive();


        Map<Integer,Boolean> validFoldersMap = new HashMap<Integer,Boolean>();
        Map<String, Boolean> validAidsMap = new HashMap<String,Boolean>();
        Map<String,Integer> typesMap = new LinkedHashMap<>();

        bilibiliVideos(typesMap,folderDao, vFileDao, validFoldersMap, validAidsMap);


        // cnn news video
        if(true){

            try {
                cnnVideos(typesMap,folderDao, vFileDao);

            }catch (Throwable e){e.printStackTrace();}

        }

        try {
            liveStream(force,typesMap,folderDao, vFileDao, validFoldersMap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // DeleteBuilder<VFile, Integer> deleteBuilder = vFileDao.deleteBuilder();
       // deleteBuilder.where().isNull("p");
       // deleteBuilder.delete();
        try {
            Aid.scanAllDrive(typesMap,validFoldersMap,validAidsMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Folder> folders = folderDao.queryForAll();
        for(Folder folder:folders){
            //if(!folder.exists()){
                if(validFoldersMap.get(folder.getId())==null
                        && folder.getTypeId()!=1
                        && folder.getTypeId()!=3
                        && folder.getTypeId()!=4
                ){
                    vFileDao.delete(folder.getFiles());
                    folderDao.delete(folder);
                }
            //}else
          //  if(folder.getFiles().size()==0){
            //    folderDao.delete(folder);
          //  }
        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("lastSynTime", System.currentTimeMillis());
        editor.apply();
        editor.commit();
        updateScreenTabs(typesMap);
        //getVideoInfo(scriptEngine,"https://www.bilibili.com/video/BV1oA411s77k?p=13");
    }

    public static void updateScreenTabs(Map<String,Integer> typesMap) {
        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        String jsonStr = JSON.toJSONString(typesMap);
        editor.putString("typesMap", jsonStr);
        editor.apply();
        editor.commit();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                PlayerController.getInstance().reloadMoviesList();
            }});
    }

    public static void bilibiliVideos(Map<String,Integer> typesMap , Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> validFoldersMap, Map<String, Boolean> validAidsMap) throws IOException, SQLException {
        String resp = Utils.get("https://api.bilibili.com/x/v3/fav/folder/created/list-all?up_mid=358543891&jsonp=jsonp");
        JSONObject jsonObj = JSONObject.parseObject(resp);


        JSONArray list = (JSONArray) ((JSONObject) (jsonObj.get("data"))).get("list");


        int orderSeq=list.size()*20;
        for (int i = 0; i < list.size(); i++) {
            JSONObject item = (JSONObject) list.get(i);
            Integer typeId = (Integer) item.get("id");
            Integer typeId2 = i+10;
            Integer media_count = (Integer) item.get("media_count");

            if(media_count>0) typesMap.put(item.getString("title"),typeId2);

            System.out.println("**目录 ："+item.getString("title")+" count:"+media_count);

            int pn=1;

            do {
                resp = Utils.get("https://api.bilibili.com/x/v3/fav/resource/list?media_id=" + typeId + "&pn=" + pn + "&ps=20&keyword=&order=mtime&type=0&tid=0&platform=web&jsonp=jsonp");
                jsonObj = JSONObject.parseObject(resp);
                JSONArray medias = (JSONArray) ((JSONObject) jsonObj.get("data")).get("medias");

                if(medias==null)break;
                for (int j = 0; j < medias.size(); j++) {
                    JSONObject media = ((JSONObject) medias.get(j));
                    String title = media.getString("title");
                    Integer aid = media.getInteger("id");
                    String bvid = media.getString("bvid");
                    String cover = media.getString("cover");
                    int pages = media.getInteger("page");
                    System.out.println(title);

                    if (title == null || title.indexOf("失效") > -1) continue;

                    Folder folder = folderDao.queryBuilder().where().eq("aid", aid).queryForFirst();
                    if (folder == null) {

                        folder = new Folder();
                        folder.setName(title);
                        //folder.setRoot(rootDriv);
                        folder.setAid("" + aid);
                        folder.setBvid(bvid);
                        folder.setCoverUrl(cover);
                        folder.setTypeId(typeId2);
                        folder.setOrderSeq(orderSeq);
                        folderDao.create(folder);

                        Map<String, Object> infoMap = new HashMap<String, Object>();
                        infoMap.put("Aid", "" + aid);
                        infoMap.put("Bid", "" + bvid);
                        infoMap.put("Title", "" + title);
                        infoMap.put("CoverURL", "" + cover);


                    } else {
                        folder.setTypeId(typeId2);
                        folder.setName(title);
                        folder.setCoverUrl(cover);
                        folder.setOrderSeq(orderSeq);

                        folderDao.update(folder);

                    }
                    orderSeq--;

                    validFoldersMap.put(folder.getId(),true);
                    validAidsMap.put(aid.toString(),true);

                    for (int k = 0; k <= pages; k++) {

                        VFile vfile = vFileDao.queryBuilder().where().eq("folder_id", folder.getId())
                                .and().eq("page", k).queryForFirst();
                        if (vfile == null) {
                            vfile = new VFile();
                            vfile.setFolder(folder);
                            vfile.setPage(k);
                            vfile.setOrderSeq(k);
                        }
                        vFileDao.createOrUpdate(vfile);

                    }


                }

                if(pn*20>media_count)break;
                pn++;

            }while (true);
        }
    }

    public static void liveStream(boolean force,Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> validFoldersMap) throws SQLException, InterruptedException {

       typesMap.put("TV",2);

        SharedPreferences sp = App.getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);
        String lastUpdateM3U="lastUpdateM3UTV";

        if (force||System.currentTimeMillis() - sp.getLong(lastUpdateM3U, 0l) > 3600 * 24*1000*10){

            List<Channel> chs = TV.getChannels();

            int i=chs.size();
            for(Channel ch:chs){
                Folder zhbFolder = folderDao.queryBuilder().where().eq("typeId", 2).and().eq("name", ch.title).queryForFirst();
                if(zhbFolder==null){
                    zhbFolder = new Folder();
                    zhbFolder.setTypeId(2);
                    zhbFolder.setName(ch.title);
                    zhbFolder.setCoverUrl(ch.logo);
                    folderDao.createOrUpdate(zhbFolder);

                    VFile vf = new VFile();
                    vf.setFolder(zhbFolder);
                    vf.setdLink(ch.m3uUrl);
                    vf.setOrderSeq(i);
                    vFileDao.createOrUpdate(vf);
                }else {
                    VFile vf=zhbFolder.getFiles().iterator().next();
                    vf.setdLink(ch.m3uUrl);
                    vf.setName(ch.title);
                    vf.setOrderSeq(i);
                    vFileDao.createOrUpdate(vf);
                }
                i--;
                validFoldersMap.put(zhbFolder.getId(),true);
            }

            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(lastUpdateM3U, System.currentTimeMillis());
            editor.apply();

        }else {
            List<Folder> folders = folderDao.queryBuilder().where().eq("typeId", 2).query();
            for(Folder f:folders){
                validFoldersMap.put(f.getId(),true);
            }

        }

    }

    public static void cnnVideos(Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao) throws IOException, SQLException {
        int channelId=3;

        String[] urls=new String[]{
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
        typesMap.put("CNN2",4);
        typesMap.put("CNN",channelId);
        for(String feedUrl:urls){

            String resp = Utils.get(feedUrl);

            JSONArray jsonArr = null;
            boolean isTop=feedUrl.indexOf("index.json")>-1;

            if(isTop)jsonArr= JSONObject.parseArray(resp);
            else jsonArr = JSON.parseObject(resp).getJSONArray("videos");

            Pattern pattern = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");
            for(int i=0;i<jsonArr.size();i++) {
                JSONObject item =(JSONObject) jsonArr.get(i);

                String videoId = item.getString(isTop?"videoId":"id");
                String title = item.getString(isTop?"title":"headline");
                String folderName = null;

                Matcher matcher = pattern.matcher(videoId);
                int seq=0;
                if(matcher.find()){
                    String dateStr = matcher.group();
                    folderName = dateStr;
                    seq = Integer.parseInt(dateStr.replaceAll("/",""));

                }else continue;


                String imageUrl = "http:"+item.getString(isTop?"imageUrl":"endslate_url_small");

                Folder folder = folderDao.queryBuilder().where().eq("typeId", channelId).and().eq("name", folderName).queryForFirst();



                videoDao.queryForAll();
                Video video = videoDao.queryBuilder().where().eq("videoId",videoId).queryForFirst();
                if(video!=null) continue;

                resp = Utils.get("https://fave.api.cnn.io/v1/video?id="+videoId+"&customer=cnn&edition=international&env=prod");
                JSONObject obj = JSONObject.parseObject(resp);

                String mediumId = obj.getString("mediumId");

                resp = Utils.get("https://medium.ngtv.io/media/"+mediumId+"?appId=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6ImNubi1jbm4td2ViLTk1am96MCIsIm5ldHdvcmsiOiJjbm4iLCJwbGF0Zm9ybSI6IndlYiIsInByb2R1Y3QiOiJjbm4iLCJpYXQiOjE1MjQ2ODQwMzB9.Uw8riFJwARLjeE35ffMwSa-37RNxCcQUEp2pqwG9TvM");

                obj = JSONObject.parseObject(resp);
                if(obj==null){
                    continue;
                }
                String url =  Utils.getObject(obj,"media.tv.unprotected.url");
                if(url==null) continue;
                System.out.println(url);


                if(folder==null){

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
                    folderDao.createOrUpdate(folder2);
                    folder2.setCoverUrl(imageUrl);
                    folder2.setOrderSeq(seq);



                    VFile vf2 = new VFile();
                    vf2.setName(title);
                    vf2.setFolder(folder2);

                    vf2.setdLink(SSLSocketClient.ServerManager.getServerHttpAddress()+"/hls/vod.m3u8?folderId="+folder.getId());
                    vf2.setOrderSeq(0);

                    vFileDao.createOrUpdate(vf2);


                }else folder.setCoverUrl(imageUrl);

                folderDao.createOrUpdate(folder);

                VFile  vf =  vFileDao.queryBuilder().where().eq("folder_id",folder.getId()).and().eq("dLink",url).queryForFirst();
                if(vf==null){
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
                video.setDt(Integer.parseInt(folderName.replaceAll("/","")));
                videoDao.createOrUpdate(video);
            }

        }
    }

    public static ScriptEngine getJsEngine() throws IOException, ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByName("js");

        // String path = Thread.currentThread().getContextClassLoader().getResource("").getPath(); // 获取targe路径
        // System.out.println(path);
        // FileReader的参数为所要执行的js文件的路径

        InputStream fd = App.getInstance().getApplicationContext().getAssets().open("md5.js");
        //scriptEngine.eval(new FileReader(path + "/md5.js"));
        scriptEngine.eval(new InputStreamReader(fd));
        return scriptEngine;
    }




}
