package com.usbtv.demo;


import android.content.res.AssetFileDescriptor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
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

    public static String getVidoUrl(String bvid, Integer p){

        try {
            JSONObject  info = getVideoInfo(getJsEngine(), "https://www.bilibili.com/video/" + bvid + "?p="+p+"&spm_id_from=pageDriver");
            info =  info.getJSONObject("data");
            return info.getString("video");
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static JSONObject  getVideoInfo(ScriptEngine scriptEngine, String link) throws ScriptException, IOException {

        String e = (String) scriptEngine.eval("Math.random().toString(10).substring(2)");

        String n = (String) scriptEngine.eval("generateStr('" + link + "@" + e + "').toString(10)");
        String o = "X-Client-Data";
        String a = "https://service0.iiilab.com";
        String site = "bilibili";

        String xclientdata = (String) scriptEngine.eval("u('" + n + "', '" + site + "')");

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())//配置
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier()).build();

        Request request2 = new Request
                .Builder()
                .url("https://bilibili.iiilab.com/")
                .addHeader("User-Agent",AGENT)
                .addHeader("Cookie", "jjpy=1; _ga=GA1.2.384352866.1623085341; iii_Session=1o38t6q6u3jsqdmt27iv1pjci6; ppp0609=1; _gsp=GA58ecc38347057979; _gid=GA1.2.173464692.1623550409; PHPSESSIID=686151623574; _gat=1")
                .build();
        Call call2 = okHttpClient.newCall(request2);
        Response response2= call2.execute();

        String session = response2.headers().get("Set-Cookie");


        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        okhttp3.RequestBody requestBody2 = new FormBody.Builder().add("link", link).add("r", e)
                .add("s", n).build();

        final Request request = new Request.Builder().url(a + "/video/web/" + site)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("Origin", "https://bilibili.iiilab.com/")
                .addHeader("Referer", "https://bilibili.iiilab.com/")
                .addHeader("User-Agent",AGENT)
                .addHeader("Cookie", "jjpy=1; _ga=GA1.2.384352866.1623085341; iii_Session=1o38t6q6u3jsqdmt27iv1pjci6; ppp0609=1; _gsp=GA58ecc38347057979; _gid=GA1.2.173464692.1623550409; "
                        //+ "PHPSESSIID=686151623574;"
                        +session.split(";")[0]
                        + "; _gat=1")
                .addHeader("X-Client-Data", xclientdata)
                .post(requestBody2).build();
        final Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        String rsp = response.body().string();
        System.out.println(rsp);
        JSONObject jsonObj = JSONObject.parseObject(rsp);

        System.out.println(e);
        return jsonObj;

    }

    public static void main(String[] args) throws ScriptException, IOException, SQLException {

        process();
    }

    public static void process() throws ScriptException, IOException, SQLException {
        String resp = get("https://api.bilibili.com/x/v3/fav/folder/created/list-all?up_mid=358543891&jsonp=jsonp");
        JSONObject jsonObj = JSONObject.parseObject(resp);

        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
        Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);
        Drive rootDriv = App.getDefaultRootDrive();
        JSONArray list = (JSONArray)((JSONObject)(jsonObj.get("data"))).get("list");
        for(int i=0;i<list.size();i++) {
            JSONObject item =(JSONObject) list.get(i);
            Integer id=(Integer)item.get("id");

            resp = get("https://api.bilibili.com/x/v3/fav/resource/list?media_id="+id+"&pn=1&ps=20&keyword=&order=mtime&type=0&tid=0&platform=web&jsonp=jsonp");
            jsonObj = JSONObject.parseObject(resp);
            JSONArray  medias = (JSONArray) ( (JSONObject)jsonObj.get("data")).get("medias");

            for(int j=0;j<medias.size();j++) {
                JSONObject media = ((JSONObject)medias.get(j));
                String title = media.getString("title");
                Integer aid = media.getInteger("id");
                String bvid = media.getString("bvid");
                String cover = media.getString("cover");
                int pages = media.getInteger("page");
                System.out.println(title);

                if(title == null || title.indexOf("失效")>-1)continue;

               Folder folder =  folderDao.queryBuilder().where().eq("aid",aid).queryForFirst();
               if(folder==null){

                   folder = new Folder();
                   folder.setName(title);
                   folder.setRoot(rootDriv);
                   folder.setAid(""+aid);
                   folder.setBvid(bvid);
                   folder.setCoverUrl(cover);

                   folderDao.create(folder);

               }

               for(int k=1;k<=pages;k++){

                   VFile vfile = vFileDao.queryBuilder().where().eq("folder_id",folder.getId())
                           .and().eq("page",k).queryForFirst();
                   if(vfile==null){
                       vfile = new VFile();
                       vfile.setFolder(folder);
                       vfile.setPage(k);
                       vFileDao.create(vfile);
                   }
               }


            }
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
        HttpUrl.Builder urlBuilder =HttpUrl.parse(url)
                .newBuilder();
        reqBuild.url(urlBuilder.build());
        Request request = reqBuild.addHeader("User-Agent",AGENT).build();
        final Call call = okHttpClient.newCall(request);
        Response response = call.execute();

        String resp = response.body().string();
        System.out.println(resp);
        return resp;
    }

}
