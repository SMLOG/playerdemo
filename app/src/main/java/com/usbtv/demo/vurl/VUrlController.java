package com.usbtv.demo.vurl;


import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.App;
import com.usbtv.demo.DocumentsUtils;
import com.usbtv.demo.FileDownload;
import com.usbtv.demo.PlayerController;
import com.usbtv.demo.data.Cache;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import download.M3u8DownloadFactory;
import download.M3u8Main;

@RestController
public class VUrlController {


    public static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36";

    private static String getM3u8(String token) throws IOException {
        Document doc;
        doc = Jsoup.connect("https://www.kanju5.com/player/player.php").userAgent(AGENT)
                .data("height", "500")
                .data("fc", token)
                .ignoreContentType(true)
                //.header("Content-Type", "application/json;charset=UTF-8")
                .post();
        String url = doc.select("iframe").eq(0).attr("src");
        url = url.replaceAll("\\\\", "").replaceAll("\\\"", "");
        System.out.println(url);

        if (!url.startsWith("http")) {
            url = "https://www.kanju5.com/" + url;
        }

        doc = Jsoup.connect(url).userAgent(AGENT).get();
        String[] a = (doc.toString().split("\"source\": \"")[1].split("\""));
        System.out.println(a[0]);
        return a[0];
    }

    public List<String> getList( String keyword) {

        List<String> list =new ArrayList<>();

        if (keyword == null && keyword.length() == 0) {
                return list;
        }
        try {
            Document doc = Jsoup.connect("https://www.kanju5.com/").userAgent(AGENT).data("s", keyword).get();

            Elements links = doc.select("#play_list_o li a");

            if(links.size()==0)return  list;

            Dao<Cache, Integer> cacheDao = App.getHelper().getDao(Cache.class);
            Cache cache = cacheDao.queryBuilder().where().eq("key", keyword).queryForFirst();
            if (cache != null) {
                list = JSON.parseArray(cache.getContent(), String.class);

            }

            for (int k = list.size(); k < links.size(); k++) {

                String pageUrl = links.get(k).absUrl("href");

                if (!pageUrl.startsWith("http")) continue;

                String[] a = Jsoup.connect(pageUrl).userAgent(AGENT).get().toString().split("fc: \"");
                a = a[1].split("\"");

                String index = getM3u8(a[0]);
                list.add(index);


            }

            String cacheContent = JSON.toJSONString(list);

            if (cache == null) {
                cache = new Cache();
                cache.setKey(keyword);
            }

            cache.setContent(cacheContent);
            cacheDao.createOrUpdate(cache);

            Collections.reverse(list);

        } catch (Exception ee) {
            ee.printStackTrace();
        }

        return list;
    }

    @GetMapping(path = "/api/v")
    String v(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword, HttpResponse response
    ) throws IOException {

        List<String> list = getList(keyword);

        StringBuilder sb = new StringBuilder();


        sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"></head>");
        sb.append("<iframe name='ifr' style='display:none'; ></iframe><form method='post' action='vurl' target='ifr'><input name='keyword' value='")
                .append(keyword)
                .append("'  /><a onclick='document.location.href=document.location.pathname+\"?keyword=\"+encodeURIComponent(document.forms[0].keyword.value);this.onclick=null;'>Search<a/><br />")
                .append("'  <input type='hidden' name='list' value='");

        for (int k = 0; k < list.size(); k++) {
            sb.append(list.get(k)).append(';');
        }

        if (list.size() > 0) sb.delete(sb.length() - 1, sb.length());

        sb.append("' /> <br />");

        for (int k = 0; k < list.size(); k++) {
            sb.append("<li>").append(k + 1).append("<input type='radio' name='curIndex' value='").append(k).append("' onchange='this.form.submit()' />")
                    .append(list.get(k)).append("</li> ");
        }

        sb.append("</form>");

        try {
            List<Cache> cacheList = App.getHelper().getDao(Cache.class).queryForAll();

            sb.append("<ul>");
            for(int i=0;i<cacheList.size();i++){
                String key = cacheList.get(i).getKey();
                sb.append("<li><a href=?keyword='").append(URLEncoder.encode(key)).append("' >").append(key).append("</a></li>");
            }
            sb.append("</ul>");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        sb.append("</html>");


        response.setHeader("Content-Type", "text/html");
        return sb.toString();
    }

    // @ResponseBody
    @GetMapping(path = "/api/vurl")
    String vurl(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword, HttpResponse response
    ) throws IOException {

        List<String> list = new ArrayList<String>();

        if (keyword != null && keyword.length() > 0) {
            Document doc = Jsoup.connect("https://www.kanju5.com/").userAgent(AGENT).data("s", keyword).get();

            Elements links = doc.select("#play_list_o li a");


            int i = 1;
            for (Element e : links) {
                String pageUrl = e.absUrl("href");

                if (!pageUrl.startsWith("http")) continue;

                String[] a = Jsoup.connect(pageUrl).userAgent(AGENT).get().toString().split("fc: \"");
                a = a[1].split("\"");

                String index = getM3u8(a[0]);
                list.add(index);
                System.out.println(i);
                i++;
            }
            Collections.reverse(list);
        }


        StringBuilder sb = new StringBuilder();


        sb.append("<iframe name='ifr' ></iframe><form method='post' target='ifr'><input name='keyword' value='")
                .append(keyword)
                .append("'  /><a onclick='document.location.href=document.location.pathname+\"?keyword=\"+encodeURIComponent(document.forms[0].keyword.value);'>Search<a/><br />")
                .append("'  <input type='hidden' name='list' value='");

        for (int k = 0; k < list.size(); k++) {
            sb.append(list.get(k)).append(';');
        }
        ;
        if (list.size() > 0) sb.delete(sb.length() - 1, sb.length());

        sb.append("' /> <br />");

        for (int k = 0; k < list.size(); k++) {
            sb.append("<li><input type='radio' name='curIndex' value='").append(k).append("' onchange='this.form.submit()' />")
                    .append(list.get(k)).append("</li> ");
        }

        sb.append("</form>");


        response.setHeader("Content-Type", "text/html");
        return sb.toString();
    }

    @PostMapping(path = "/api/vurl")
    String pl(@RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
              @RequestParam(name = "list", required = false, defaultValue = "") String list,
              @RequestParam(name = "curIndex", required = false, defaultValue = "0") int curIndex
    ) throws IOException {


        VUrlList vUrlList = new VUrlList(keyword, curIndex, list.split(";"));
        PlayerController.getInstance().play(vUrlList);

        return "";
    }


    @GetMapping(path = "/api/r")
    ResponseBody range(
            HttpRequest request,HttpResponse response,
            @RequestParam(name = "url", required = false, defaultValue = "") String url,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "curIndex", required = false, defaultValue = "0") int curIndex
    ) throws IOException {

        ResponseBody responseBody = null;
        while (true) {
            try {

                String name = "" + (curIndex + 1);
                String dir = "/storage/36AC6142AC60FDAD/videos/"+keyword;

                File file = new File(dir + "/" + name + ".mp4");
                if (!file.exists()) {
                    final M3u8DownloadFactory.M3u8Download[] dower = {null};

                    new Thread(() -> {
                        try {
                            dower[0] = M3u8Main.startDownload(url, dir, name);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }).start();

                    while (true){
                        Thread.sleep(1000*10);
                        if(dower[0]!=null&&dower[0].getPercent()>1){

                            responseBody =  new FileDownload(request,response,new File(dower[0].getFilePath()),"mp4");
                            break;
                        }
                    }

                }else{
                    responseBody =  new FileBody(file);
                }
                break;
            } catch (Exception ee) {
                ee.printStackTrace();

            }
        }

        response.setBody(responseBody);
        return responseBody;
    }

}