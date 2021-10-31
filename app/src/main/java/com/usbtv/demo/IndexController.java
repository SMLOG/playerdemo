package com.usbtv.demo;


import android.media.MediaPlayer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.usbtv.demo.comm.SpeechUtils;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.framework.body.StreamBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.RequestBody;
import com.yanzhenjie.andserver.http.multipart.MultipartFile;
import com.yanzhenjie.andserver.util.MediaType;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;

@RestController
public class IndexController {
    private static String TAG = "IndexController";
    private int searchPage;
    private int curP;
    private int playIndex;
    private String searchKeyword;
    private JSONArray searchResult;

    @ResponseBody
    @GetMapping(path = "/api/status")
    String status(RequestBody body, HttpResponse response) throws IOException {
        response.setHeader("Content-Type", "application/json; charset=utf-8");

        return JSON.toJSONString(PlayerController.getInstance());
    }


    @ResponseBody
    @GetMapping(path = "/api/reLoadPlayList")
    String reLoadPlayList(RequestBody body) throws IOException {


        return "ok";
    }

    @ResponseBody
    @GetMapping(path = "/play")
    String play(
            HttpRequest request
    ) {


        return "OK";
    }

    @ResponseBody
    @GetMapping(path = "/api/cmd")
    String cmd(

            @RequestParam(name = "cmd") String cmd,
            @RequestParam(name = "val", required = false, defaultValue = "") String val,
            @RequestParam(name = "id", required = false, defaultValue = "-1") int id,
            @RequestParam(name = "typeId", required = false, defaultValue = "0") int typeId
    ) {

        if ("play".equals(cmd)) {
            VFile item = null;
            try {
                item = App.getHelper().getDao(VFile.class).queryBuilder().where().eq("id", id).queryForFirst();
                PlayerController.getInstance().play(item);
                PlayerController.getInstance().setTypeId(item.getFolder().getTypeId());

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        } else if ("next".equals(cmd)) {

            PlayerController.getInstance().next();

        } else if ("pause".equals(cmd)) {

            if (PlayerController.getInstance().isPlaying())
                PlayerController.getInstance().pause();


        } else if ("resume".equals(cmd)) {

            if (!PlayerController.getInstance().isPlaying())
                PlayerController.getInstance().start();

        } else if ("toggle".equals(cmd)) {

            if (PlayerController.getInstance().isPlaying())
                PlayerController.getInstance().pause();
            else
                PlayerController.getInstance().start();


        } else if ("seekTo".equals(cmd)) {

            int progress = Integer.parseInt(val);
            if (progress < 0)
                progress = 0;
            else if (progress > PlayerController.getInstance().getDuration())
                progress = (int) PlayerController.getInstance().getDuration();

            PlayerController.getInstance().seekTo(progress);

        } else if ("mode".equals(cmd)) {

            PlayerController.getInstance().setMode(Integer.parseInt(val));
        } else if ("detach".equals(cmd)) {

            App.broadcastCMD(cmd, val);
        } else if (cmd.startsWith("broadcast")) {
            App.broadcastCMD(cmd, val);
        }

        return "ok";

    }


    @GetMapping(path = "/api/proxy")
    com.yanzhenjie.andserver.http.ResponseBody proxy(HttpRequest req, HttpResponse response) throws IOException {

        String url = App.getProxyUrl(req.getParameter("url"));
        if (url.startsWith("file://"))
            return new FileBody(new File(url.substring("file://".length())));
        OkHttpClient client = new OkHttpClient();

        //获取请求对象
        Request request = new Request.Builder().url(url).build();

        //获取响应体

        okhttp3.ResponseBody body = client.newCall(request).execute().body();
        String type = body.contentType().type();
        String[] cacheTypes = new String[]{"audio", "video", "image"};

        if (Arrays.asList(cacheTypes).indexOf(type) > -1) {

        }

        //获取流
        InputStream in = body.byteStream();

        try {
            StreamBody respBody = new StreamBody(in, body.contentLength(), MediaType.parseMediaType(body.contentType().toString()));
            // response.setBody(respBody);
            return respBody;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }




    @GetMapping(path = "/api/bgmedia")
    @ResponseBody
    int bgmedia(@RequestParam(name = "cmd") String cmd, @RequestParam(name = "val", required = false) String val) {
        if (App.bgMedia == null) {
            App.bgMedia = new MediaPlayer();
        }
        if ("start".equals(cmd)) {
            App.bgMedia.start();

        } else if ("pause".equals(cmd)) {
            if (App.bgMedia.isPlaying()) App.bgMedia.pause();

        } else if ("loop".equals(cmd)) {

            App.bgMedia.setLooping(Boolean.parseBoolean(val));

        } else if ("volume".equals(cmd)) {
            App.bgMedia.setVolume(Float.parseFloat(val), Float.parseFloat(val));

        } else if ("url".equals(cmd)) {
            try {
                if (val == null || val.trim().equals("")) {
                    if (App.bgMedia.isPlaying()) App.bgMedia.pause();
                    else App.bgMedia.start();

                } else {
                    App.bgMedia.reset();
                    App.bgMedia.setDataSource(val);
                    // App.bgMedia.setDataSource(Uri.parse(val));
                    App.bgMedia.prepare();
                    App.bgMedia.setLooping(true);
                    App.bgMedia.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return 0;
    }

    @GetMapping(path = "/api/manRes")
    @ResponseBody
    String getResList(@RequestParam(name = "page") int page, @RequestParam(name = "typeId") int typeId, @RequestParam(name = "searchValue", required = false, defaultValue = "") String searchValue) {

        try {
            int pageSize = 20;
            Map<String, Object> result = new HashMap<String, Object>();

            result.put("pageSize", pageSize);
            result.put("page", page);

            if (typeId == 3) {

                QueryBuilder<Folder, ?> where = App.getHelper().getDao(Folder.class).queryBuilder();
                if (searchValue != null && !searchValue.trim().equals("")) {
                    where = where.where().like("name", "%" + searchValue + "%").queryBuilder();
                }
                long total = where.countOf();
                where.query();
                result.put("total", total);
                List<Folder> list = where
                        .offset((long) ((page - 1) * pageSize))
                        .limit(20l).query();
                result.put("datas", list);

            }


            return JSON.toJSONString(result);

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }



    @GetMapping(path = "/api/delete")
    String delete(@RequestParam(name = "aIndex") int aIndex, @RequestParam(name = "bIndex") int bIndex, HttpResponse response) throws IOException {

        return null;
    }

    @ResponseBody
    @PostMapping(path = "/api/event")
    String event(RequestBody body) throws IOException {
        String content = body.string();
        try {
            JSONObject params = new JSONObject(content);
            String str = params.getString("event");
            Utils.exec(str);
            return "ok";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }



    @PostMapping(path = "/api/upload2")
    String upload(HttpRequest request, @RequestParam(name = "file") MultipartFile[] files) throws IOException {

        String rootDir = App.getDefaultRootDrive().getP();

        for (MultipartFile file : files) {
            String to = rootDir + file.getFilename();
            new File(to).getParentFile().mkdirs();
            file.transferTo(new File(to));
        }
        return "OK";

    }



    @GetMapping(path = "/api/upload")
    String upload2(HttpRequest request,
                   @RequestParam(name = "chunkNumber") int chunkNumber,
                   @RequestParam(name = "chunkSize") int chunkSize,
                   @RequestParam(name = "currentChunkSize") int currentChunkSize,
                   @RequestParam(name = "totalSize") int totalSize,
                   @RequestParam(name = "identifier") String identifier,
                   @RequestParam(name = "filename") String filename,
                   @RequestParam(name = "relativePath") String relativePath,
                   @RequestParam(name = "totalChunks") int totalChunks
    ) {
        Map<String, Object> res = new HashMap<>();
        List<Drive> drives = Utils.getSysAllDriveList();

        Drive drive = drives.get(drives.size() - 1);
        String rootDir = drive.getP();

        String to = rootDir + "/" + relativePath;
        if (new File(to).exists()) {


            res.put("skipUpload", true);
        }


        return JSON.toJSONString(res);
    }

    @PostMapping(path = "/api/upload")
    String upload2(HttpRequest request,
                   @RequestParam(name = "chunkNumber") int chunkNumber,
                   @RequestParam(name = "chunkSize") int chunkSize,
                   @RequestParam(name = "currentChunkSize") int currentChunkSize,
                   @RequestParam(name = "totalSize") int totalSize,
                   @RequestParam(name = "identifier") String identifier,
                   @RequestParam(name = "filename") String filename,
                   @RequestParam(name = "relativePath") String relativePath,
                   @RequestParam(name = "totalChunks") int totalChunks,
                   @RequestParam(name = "file") MultipartFile file
    ) {

        List<Drive> drives = Utils.getSysAllDriveList();

        String rootDir = drives.get(drives.size() - 1).getP();

        String to = rootDir + "/" + relativePath;
        String chunkTo = to + "-" + chunkNumber;

        if (new File(to).exists()) {
            return "";
            //file.delete();
        }

        new File(to).getParentFile().mkdirs();

        try {

            file.transferTo(new File(chunkTo));
            if (chunkNumber == totalChunks) {

                if (new File(to).exists()) {
                    return "";
                    //file.delete();
                }
                BufferedOutputStream destOutputStream = new BufferedOutputStream(new FileOutputStream(to));
                for (int i = 1; i <= totalChunks; i++) {
                    byte[] fileBuffer = new byte[1024];
                    int readBytesLength = 0;
                    File sourceFile = new File(to + "-" + i);
                    BufferedInputStream sourceInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                    while ((readBytesLength = sourceInputStream.read(fileBuffer)) != -1) {
                        destOutputStream.write(fileBuffer, 0, readBytesLength);
                    }
                    sourceInputStream.close();

                }
                destOutputStream.flush();
                destOutputStream.close();
                for (int i = 1; i <= totalChunks; i++) {
                    File sourceFile = new File(to + "-" + i);
                    boolean delete = sourceFile.delete();
                    if (delete) {
                    }
                }
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        return "";
    }

    @ResponseBody
    @GetMapping(path = "/api/listDisk")
    String listDisk(@RequestParam(name = "path", required = false, defaultValue = "") String path) throws IOException {

        List<Drive> drives = Utils.getSysAllDriveList();

        String rootDir = drives.get(drives.size() - 1).getP();

        File curFolder = new File(rootDir + path);

        ArrayList<Map<String, Object>> list = new ArrayList();


        for (File f : curFolder.listFiles()) {
            Map<String, Object> info = new HashMap<>();
            info.put("name", f.getName());
            info.put("size", f.getTotalSpace());
            info.put("isFile", f.isFile());
            list.add(info);
        }

        String df = Utils.exec("df -kh |grep /storage/");
        Map<String, Object> ret = new HashMap<>();
        ret.put("contents", list);
        ret.put("path", path);

        return JSON.toJSONString(ret);

    }

    private static final String AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36";

    @GetMapping(path = "/api/searchplay")
    com.yanzhenjie.andserver.http.ResponseBody searchplay(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "research", required = false, defaultValue = "true"
            ) boolean research,

            HttpResponse response) throws SQLException, IOException {

        while (true) {


            if (research || searchResult == null) {
                this.searchPage = 1;
                this.curP = 1;
                this.playIndex = -1;
                this.searchKeyword = keyword;

            }
            if (this.playIndex > 20) {
                this.searchPage++;
                this.playIndex = -1;
            }

            if (this.playIndex == -1) {
                Document doc = Jsoup.connect("https://api.bilibili.com/x/web-interface/search/type?context=&order=&"
                        + "duration=&tids_1=&tids_2=&from_source=video_tag&from_spmid=333.788.b_765f746167.6&platform=pc&__refresh__=true&_extra=&search_type=video&highlight=1&single_column=0")
                        .ignoreContentType(true)
                        .data("page", "" + this.searchPage)
                        .data("keyword", this.searchKeyword)
                        .userAgent(AGENT).get();

                System.out.println(doc.body().text());
                com.alibaba.fastjson.JSONObject json = JSON.parseObject(doc.body().text());

                json = (com.alibaba.fastjson.JSONObject) json.get("data");
                this.searchResult = json.getJSONArray("result");
                this.playIndex = 0;
            }

            com.alibaba.fastjson.JSONObject data = this.searchResult.getJSONObject(this.playIndex);
            com.alibaba.fastjson.JSONObject vidoInfo = DownloadMP.getVidoInfo(data.getString("bvid"), this.curP);
            if (vidoInfo == null || null == vidoInfo.getString("video")) {
                this.curP = 1;
                this.playIndex++;
                continue;
            } else {
                this.curP++;

                if (vidoInfo != null && null != vidoInfo.getString("video"))
                    response.sendRedirect(vidoInfo.getString("video"));

            }

            break;
        }
        return null;

    }

    @GetMapping(path = "/api/vfile")
    com.yanzhenjie.andserver.http.ResponseBody vfile(HttpRequest request, @RequestParam(name = "id") int id, HttpResponse response) throws SQLException, IOException {

        Dao<VFile, Integer> dao = App.getHelper().getDao(VFile.class);
        VFile vfile = dao.queryForId(id);
        String path = vfile.getAbsPath();

        if(path == null || ! new File(path).exists())
        for(Drive d:App.diskList){
            vfile.getFolder().setRoot(d);
            if(vfile.exists() && new File(vfile.getAbsPath()).canRead()
            ){
                try {
                    Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);

                    folderDao.update(vfile.getFolder());

                    path = vfile.getAbsPath();
                    break;

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        }

        if (path != null) {
            final File file = new File(path);
            if (file.exists() && file.length() > 0) {

                if (vfile.getP() == null) {
                    vfile.setP(vfile.getRelativePath());
                    dao.update(vfile);
                }

                System.out.println(path + " file exists");
                return new FileBody(file);

            }
        }


        String url = null;


        if (vfile.getdLink() != null) {
            url = DLVideo.getM3U8(vfile.getdLink());
        } else {
            com.alibaba.fastjson.JSONObject vidoInfo = DownloadMP.getVidoInfo(vfile.getFolder().getBvid(), vfile.getPage());
            if (vidoInfo != null && null != vidoInfo.getString("video")) {
                url = vidoInfo.getString("video");
            }
        }

        App.cache2Disk(vfile, url);


        System.out.println(url);
        response.sendRedirect(url);


        return null;

    }



    @ResponseBody
    @GetMapping(path = "/api/syncache")
    String mybi(@RequestParam(name = "download", required = false, defaultValue = "false") boolean download) throws SQLException, IOException {

        DownloadMP.process();

        //DLVideo.getList();

        if (download) {
            Dao<VFile, Integer> dao = App.getHelper().getDao(VFile.class);
            List<VFile> vfiles = dao.queryBuilder()
                    .where().isNull("p").query();

            for (VFile vfile : vfiles) {

                try {
                    if (vfile.getFolder().getRoot() == null) {
                        vfile.getFolder().setRoot(App.getDefaultRootDrive());
                        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
                        folderDao.update(vfile.getFolder());
                    }

                    if (vfile.getFolder().getRoot() == null) continue;

                    File file = new File(vfile.getAbsPath());
                    if (!file.exists() || file.length() == 0) {
                        com.alibaba.fastjson.JSONObject info = DownloadMP.getVidoInfo(vfile.getFolder().getBvid(), vfile.getPage());
                        //if (info != null && info.getString("video") != null)
                           // oInstance.saveToFile(info.getString("video"), vfile.getAbsPath());
                    }
                    vfile.setP(vfile.getRelativePath());
                    dao.update(vfile);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

        return "ok";
    }

    @ResponseBody
    @GetMapping("/api/ts")
    public String speakText(@RequestParam(name = "t") String t) {
        SpeechUtils.getInstance(App.getInstance().getApplicationContext()).speakText(t);
        return "OK";
    }

    /*@GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }*/

}
