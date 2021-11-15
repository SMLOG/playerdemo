package com.usbtv.demo.vurl;


import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.usbtv.demo.App;
import com.usbtv.demo.DocumentsUtils;
import com.usbtv.demo.FileDownload;
import com.usbtv.demo.PlayerController;
import com.usbtv.demo.ServerManager;
import com.usbtv.demo.data.Cache;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.annotation.PathVariable;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.HandlerInterceptor;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.framework.body.StreamBody;
import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.framework.handler.RequestHandler;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import download.Log;
import download.M3u8DownloadFactory;
import download.M3u8DownloadProxy;
import download.M3u8Exception;
import download.M3u8Main;

@RestController
@Interceptor
public class VUrlController implements HandlerInterceptor {


    public static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36";
    private static final Map<String, M3u8DownloadProxy> DOWNLOADING = new HashMap<String, M3u8DownloadProxy>();

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

    public List<String> getList(String keyword, boolean b) {

        List<String> list = new ArrayList<>();

        if (keyword == null && keyword.length() == 0) {
            return list;
        }
        try {
            Document doc = Jsoup.connect("https://www.kanju5.com/").userAgent(AGENT).data("s", keyword).get();

            Elements links = doc.select("#play_list_o li a");

            if (links.size() == 0) return list;

            Dao<Cache, Integer> cacheDao = App.getHelper().getDao(Cache.class);
            Cache cache = cacheDao.queryBuilder().where().eq("key", keyword).queryForFirst();
            if (cache != null) {
                list = JSON.parseArray(cache.getContent(), String.class);

            }

            if (b) list.clear();
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
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "all", required = false, defaultValue = "0") int all,
            HttpResponse response
    ) throws IOException {

        List<String> list = getList(keyword, all > 0);

        StringBuilder sb = new StringBuilder();


        sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"></head>");
        sb.append("<iframe name='ifr' style='display:none'; ></iframe><form method='post' action='vurl' target='ifr'><input name='keyword' value='")
                .append(keyword)
                .append("'  /><a onclick='document.location.href=document.location.pathname+\"?keyword=\"+encodeURIComponent(document.forms[0].keyword.value);this.onclick=null;'>Search<a/><br />")
                .append("'  <input type='hidden' name='list' value='");

        try {
            Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
            Dao<VFile, Integer> vfileDao = App.getHelper().getDao(VFile.class);
            Folder folder = folderDao.queryBuilder()
                    .where().eq("typeId", 1)
                    .and().eq("name", keyword)
                    .queryForFirst();
            if (folder == null) {
                folder = new Folder();
                folder.setName(keyword);
                folder.setTypeId(1);
                folder.setP(keyword);
                folderDao.createOrUpdate(folder);
            } else {
                if (all > 0) {
                    vfileDao.delete(folder.getFiles());
                    folder.getFiles().clear();
                }
            }

            int n = folder.getFiles() == null ? 0 : folder.getFiles().size();

            for (int k = 0; k < list.size(); k++) {
                sb.append(list.get(k)).append(';');
                if (k >= n) {
                    VFile vf = new VFile();
                    vf.setFolder(folder);
                    vf.setP("" + k + ".mp4");
                    vf.setdLink(list.get(k));
                    vf.setOrderSeq(k);
                    vfileDao.createOrUpdate(vf);
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
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
            for (int i = 0; i < cacheList.size(); i++) {
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


    @PostMapping(path = "/api/vurl")
    String pl(@RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
              @RequestParam(name = "list", required = false, defaultValue = "") String list,
              @RequestParam(name = "curIndex", required = false, defaultValue = "0") int curIndex
    ) throws IOException {


        VUrlList vUrlList = new VUrlList(keyword, curIndex, list.split(";"));
        PlayerController.getInstance().play(vUrlList);

        return "";
    }


    @GetMapping(path = "/api/r/{fid}/{index}/index.m3u8")
    ResponseBody range2(
            HttpRequest request, HttpResponse response,
            @RequestParam(name = "url", required = false, defaultValue = "") String url,
            @PathVariable("fid") int fid,
            @PathVariable("index") int index
    ) throws Exception {

        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
        Folder folder = folderDao.queryForId(fid);
        String name = folder.getName();
        ResponseBody responseBody = null;

        if (url.trim().equalsIgnoreCase("")) {
            url = folder.getFiles().toArray(new VFile[]{})[index].getdLink();
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Connection", "keep-alive");

        String path = "" + index + "/" + index + ".mp4";
        String dir = "/storage/36AC6142AC60FDAD/videos/" + name;

        File file = new File(dir + "/" + path + ".mp4");
        String downloadId = folder.getId() + "-" + index;
        synchronized (DOWNLOADING) {

            for (String id : DOWNLOADING.keySet()) {
                if (downloadId.equals(id)) continue;
                M3u8DownloadProxy down = DOWNLOADING.get(id);
                if (down != null) {
                    down.pause();
                    //DOWNLOADING.remove(id);
                }
            }
            if (!file.exists()) {
                if (DOWNLOADING.get(downloadId) == null) {
                    M3u8DownloadProxy proxy = new M3u8DownloadProxy(url, downloadId, dir, index, "" + (index + 1)).start();
                    proxy.mergeAllTsToMp4();

                    if (true) {
                        DOWNLOADING.put(downloadId, proxy);
                        response.setHeader("Content-Type", "application/vnd.apple.mpegURL");
                        String str = proxy.getM3U8Content(true);
                        System.out.println(str);
                        responseBody = new StringBody(str, MediaType.parseMediaType("application/vnd.apple.mpegURL"));
                        return responseBody;
                    }
                }
            } else {
                DOWNLOADING.put(downloadId, null);
            }
        }

        if (!file.exists()) {
            M3u8DownloadProxy downloader = (M3u8DownloadProxy) DOWNLOADING.get(downloadId);
            response.setHeader("Content-Type", "application/vnd.apple.mpegURL");
            response.setHeader("Content-Disposition", "inline; filename=index.m3u8");

            String str = downloader.getM3U8Content(true);
            System.out.println(str);
            responseBody = new StringBody(str, MediaType.parseMediaType("application/vnd.apple.mpegURL"));
        } else {
            responseBody = new FileBody(file);
        }

        response.setBody(responseBody);
        return responseBody;
    }


    @GetMapping(path = "/api/s/{downloadId}/hls.m3u8")
    ResponseBody s(
            HttpRequest request, HttpResponse response,
            @PathVariable("downloadId") String downloadId
    ) throws Exception {

        //  if(true)return new FileBody(new File("/storage/36AC6142AC60FDAD/videos/970456553/1/970456553.mp4"));;
        ResponseBody responseBody = null;

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Content-Type", "application/vnd.apple.mpegURL");
        M3u8DownloadProxy downloader = (M3u8DownloadProxy) DOWNLOADING.get(downloadId);
        responseBody = new StringBody(downloader.getM3U8Content(false), MediaType.parseMediaType("application/vnd.apple.mpegURL"));


        response.setBody(responseBody);
        return responseBody;
    }


    @GetMapping(path = "/api/rts/{downloadId}/{index}/a.ts")
    ResponseBody ts(
            HttpRequest request, HttpResponse response,
            @PathVariable(name = "downloadId") String downloadId,
            @PathVariable(name = "index") int index
    ) throws Exception {

        //  if(true)return new FileBody(new File("/storage/36AC6142AC60FDAD/videos/970456553/1/970456553.mp4"));;
        ResponseBody responseBody = null;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Connection", "keep-alive");

        response.setHeader("content-type", "video/mp2t");

        M3u8DownloadProxy downloader = (M3u8DownloadProxy) DOWNLOADING.get(downloadId);
        Object bytesOrFile = downloader.getTsStream(index);

        if (bytesOrFile instanceof byte[]) {
            byte[] bytes = (byte[]) bytesOrFile;
            responseBody = new StreamBody(new ByteArrayInputStream(bytes), bytes.length, MediaType.parseMediaType("video/mp2t"));
            response.setBody(responseBody);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            response.setHeader("Connection", "keep-alive");

            response.setHeader("content-type", "video/mp2t");

            return responseBody;
        }


        //Object fileOrUrl = downloader.downloadIndexTs(index);
        if (bytesOrFile instanceof File) {
            File file = (File) bytesOrFile;
            responseBody = new StreamBody(new FileInputStream(file), file.length(), MediaType.parseMediaType("video/mp2t"));
            response.setBody(responseBody);


            response.setHeader("Content-Disposition", "attachment; filename=" + index + ".ts");
            //response.sendRedirect("file://"+file.getAbsolutePath());
            //return null;
            return responseBody;
        }
        //else
        //  response.sendRedirect(fileOrUrl.toString());

        return null;
    }

    @GetMapping(path = "/api/m3u8proxy")
    ResponseBody m3u8Proxy(
            HttpRequest request, HttpResponse response,
            @RequestParam(name = "url") String url
    ) throws Exception {

        int count = 1;
        int retryCount = 5;
        HttpURLConnection httpURLConnection = null;
        long timeoutMillisecond = 100000L;

        HashMap<String, String> requestHeaderMap = new HashMap<String, String>();
        requestHeaderMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36");
        String contentType = null;


        InputStream in = null;

        //重试次数判断
        while (count <= retryCount) {
            try {
                httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                httpURLConnection.setConnectTimeout((int) timeoutMillisecond);
                for (Map.Entry<String, String> entry : requestHeaderMap.entrySet())
                    httpURLConnection.addRequestProperty(entry.getKey(), entry.getValue().toString());
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setReadTimeout((int) timeoutMillisecond);
                httpURLConnection.setDoInput(true);

                contentType = httpURLConnection.getHeaderField("content-type");
                in = httpURLConnection.getInputStream();

                ByteArrayOutputStream byos = new ByteArrayOutputStream();
                int len = 0;
                byte[] bytes = new byte[4096];
                while ((len = in.read(bytes)) != -1) {
                    byos.write(bytes, 0, len);
                }
                in.close();

                bytes = new byte[byos.size()];
                System.arraycopy(byos.toByteArray(), 0, bytes, 0, bytes.length);
                byos.close();

                if (contentType.contains("x-mpegURL")) {
                    String[] lines = new String(bytes).split("\n");
                    StringBuilder sb = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith("#")) {
                            sb.append(line);
                        } else {
                            String absUrl = "";

                            if (line.startsWith("/")) {
                                absUrl = url.split("/")[0] + line;
                            } else if (line.matches("^(http|https)://.+")) {
                                absUrl = line;
                            } else {
                                absUrl = url.substring(0, url.lastIndexOf("/") + 1) + line;
                            }
                            sb.append(ServerManager.getServerHttpAddress()).append("/api/m3u8proxy?url=" + URLEncoder.encode(absUrl));
                        }

                        sb.append("\n");

                    }
                    bytes = sb.toString().getBytes();
                }

                Map<String, List<String>> hfs = httpURLConnection.getHeaderFields();
                for (String name : hfs.keySet()) {
                   // if(name!=null)
                     //   response.setHeader(name, httpURLConnection.getHeaderField(name));
                }

                StreamBody responseBody = new StreamBody(new ByteArrayInputStream(bytes), bytes.length, MediaType.parseMediaType(contentType));

                //response.setBody(responseBody);
                return responseBody;

            } catch (Exception e) {
                e.printStackTrace();

                Log.d("第" + count + "获取链接重试！\t" + url);
                count++;

            } finally {

                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

            }
        }

        if (count > retryCount)
            throw new M3u8Exception("连接超时！");
        return null;
    }

    @Override
    public boolean onIntercept(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull RequestHandler handler) throws Exception {

        if(request.getURI().startsWith("")){

        }

        return false;
    }
}
