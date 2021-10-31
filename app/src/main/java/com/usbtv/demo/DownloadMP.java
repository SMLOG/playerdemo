package com.usbtv.demo;


import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.j256.ormlite.stmt.DeleteBuilder;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadMP {

    private static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

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

    public static String join(String s, List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            sb.append(v).append(s);
        }
        return sb.toString();
    }

    public static JSONObject getVideoInfo(ScriptEngine scriptEngine, String link) throws ScriptException, IOException {

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
                .addHeader("User-Agent", AGENT)
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
                .addHeader("User-Agent", AGENT)
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
                .addHeader("Referer", "https://bilibili.iiilab.com/").addHeader("User-Agent", AGENT)
                .addHeader("Cookie", join(";", cookies))

                .post(requestBody).build();
        call = okHttpClient.newCall(request);
        response = call.execute();


        for (String cookie :
                response.headers().values("Set-Cookie"))
            cookies.add(cookie.split(";")[0]);

        String rsp = response.body().string();


        mediaType = MediaType.parse("application/x-www-form-urlencoded");

        requestBody = new FormBody.Builder().add("link", link).add("r", e)
                .add("s", n).build();

        request = new Request.Builder().url(a + "/video/web/" + site)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("Origin", "https://bilibili.iiilab.com/")
                .addHeader("Referer", "https://bilibili.iiilab.com/")
                .addHeader("User-Agent", AGENT)
                .addHeader("Cookie", join(";", cookies))
                .addHeader("X-Client-Data", xclientdata)

                .post(requestBody).build();
        call = okHttpClient.newCall(request);
        response = call.execute();
        rsp = response.body().string();
        System.out.println(rsp);
        JSONObject jsonObj = JSONObject.parseObject(rsp);

        if(jsonObj.getString("retDesc")!=null&&jsonObj.getString("retDesc").equals("outstanding")) {

            String str=new String(Base64.decode(jsonObj.getString("data").substring(6),Base64.DEFAULT));
            jsonObj.put("data", JSON.parse(str));
        }

        System.out.println(e);
        return jsonObj;

    }

    public static void main(String[] args) throws ScriptException, IOException, SQLException {

        process();
    }

    public static void process() throws  IOException, SQLException {
        String resp = get("https://api.bilibili.com/x/v3/fav/folder/created/list-all?up_mid=358543891&jsonp=jsonp");
        JSONObject jsonObj = JSONObject.parseObject(resp);

        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
        Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);
        //Drive rootDriv = App.getDefaultRootDrive();



        Map<Integer,Boolean> validFoldersMap = new HashMap<Integer,Boolean>();
        Map<String, Boolean> validAidsMap = new HashMap<String,Boolean>();
        JSONArray list = (JSONArray) ((JSONObject) (jsonObj.get("data"))).get("list");


        for (int i = 0; i < list.size(); i++) {
            JSONObject item = (JSONObject) list.get(i);
            Integer typeId = (Integer) item.get("id");
            Integer typeId2 = i+1;
            Integer media_count = (Integer) item.get("media_count");

            App.getTypesMap().put(item.getString("title"),typeId2.toString());

            System.out.println("**目录 ："+item.getString("title")+" count:"+media_count);

            int pn=1;

            do {
                resp = get("https://api.bilibili.com/x/v3/fav/resource/list?media_id=" + typeId + "&pn=" + pn + "&ps=20&keyword=&order=mtime&type=0&tid=0&platform=web&jsonp=jsonp");
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
                        folderDao.update(folder);

                    }

                    validFoldersMap.put(folder.getId(),true);
                    validAidsMap.put(aid.toString(),true);

                    for (int k = 1; k <= pages; k++) {

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
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                ArrayList<String> newList = new ArrayList<>();
                newList.addAll(App.getInstance().getTypesMap().keySet());
                MainActivity.mNavigationLinearLayout.setDataList(newList);

            }});

       // DeleteBuilder<VFile, Integer> deleteBuilder = vFileDao.deleteBuilder();
       // deleteBuilder.where().isNull("p");
       // deleteBuilder.delete();
        try {
            Aid.scanAllDrive(validFoldersMap,validAidsMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Folder> folders = folderDao.queryForAll();
        for(Folder folder:folders){
            //if(!folder.exists()){
                if(validFoldersMap.get(folder.getId())==null){
                    vFileDao.delete(folder.getFiles());
                    folderDao.delete(folder);
                }
            //}else
          //  if(folder.getFiles().size()==0){
            //    folderDao.delete(folder);
          //  }
        }

        //getVideoInfo(scriptEngine,"https://www.bilibili.com/video/BV1oA411s77k?p=13");
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


    private static String get(String url) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url)
                .newBuilder();
        reqBuild.url(urlBuilder.build());
        Request request = reqBuild.addHeader("User-Agent", AGENT).build();
        final Call call = okHttpClient.newCall(request);
        Response response = call.execute();

        String resp = response.body().string();
        System.out.println(resp);
        return resp;
    }

}
