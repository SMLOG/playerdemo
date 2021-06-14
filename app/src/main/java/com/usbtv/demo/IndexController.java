package com.usbtv.demo;


import android.media.MediaPlayer;

import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.HttpGet;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.ResItem;
import com.usbtv.demo.data.VFile;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.framework.body.StreamBody;
import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.RequestBody;
import com.yanzhenjie.andserver.http.multipart.MultipartFile;
import com.yanzhenjie.andserver.util.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

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

import javax.script.ScriptException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

@RestController
public class IndexController {
    private static String TAG = "IndexController";
    HttpGet oInstance = new HttpGet();

    @ResponseBody
    @GetMapping(path = "/api/status")
    String status(RequestBody body, HttpResponse response) throws IOException {
        response.setHeader("Content-Type", "application/json; charset=utf-8");

        return JSON.toJSONString(PlayerController.getInstance());
    }


    @ResponseBody
    @GetMapping(path = "/api/reLoadPlayList")
    String reLoadPlayList(RequestBody body) throws IOException {

        DowloadPlayList.loadPlayList(false);

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
            @RequestParam(name = "typeId", required = false, defaultValue = "0") int typeId,
            @RequestParam(name = "aIndex", required = false, defaultValue = "-1") int aIndex,
            @RequestParam(name = "bIndex", required = false, defaultValue = "-1") int bIndex
    ) {

        if ("play".equals(cmd)) {
            Object item = null;
            if (typeId == 3) {
                try {
                    item = App.getHelper().getDao(VFile.class).queryBuilder().where().eq("id", id).queryForFirst();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            } else {
                try {
                    item = App.getHelper().getDao(ResItem.class).queryBuilder().where().eq("id", id).queryForFirst();

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            PlayerController.getInstance().play(item);

        } else if ("next".equals(cmd)) {

            PlayerController.getInstance().playNext();

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

        } else if ("showMask".equals(cmd)) {

            if (Boolean.parseBoolean(val)) {
                PlayerController.getInstance().showMaskView();
            } else
                PlayerController.getInstance().hideMaskView();
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


    @GetMapping(path = "/api/res")
    String res(@RequestParam(name = "typeId") int typeId, @RequestParam(name = "id") int id, HttpResponse response) {

        ResItem res = new ResItem();
        res.setTypeId(typeId);

        if (typeId == res.IMAGE) {
            try {
                res = App.getHelper().getDao().queryForId(id);

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return JSON.toJSONString(res);

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
                    App.bgMedia.setDataSource(App.getProxyUrl(val));
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
    String getResList(@RequestParam(name = "page") int page, @RequestParam(name = "typeId") int typeId) {

        try {
            int pageSize = 20;
            Map<String, Object> result = new HashMap<String, Object>();

            result.put("pageSize", pageSize);
            result.put("page", page);

            if (typeId == 3) {

                long total = App.getHelper().getDao(Folder.class).countOf();

                result.put("total", total);
                List<Folder> list = App.getHelper().getDao(Folder.class).queryBuilder()
                        .offset((long) ((page - 1) * pageSize))
                        .limit(20l).query();
                result.put("datas", list);

            } else {
                long total = App.getHelper().getDao().queryBuilder().where().eq("typeId", typeId).countOf();
                List<ResItem> list = App.getHelper().getDao().queryBuilder()
                        .where().eq("typeId", typeId).queryBuilder()
                        .offset((long) ((page - 1) * pageSize))
                        .limit(20l).query();
                result.put("datas", list);
                result.put("total", total);
            }


            return JSON.toJSONString(result);

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping(path = "/api/manRes")
    String manRes(RequestBody body, HttpResponse response) {
        try {
            String content = body.string();

            ResItem res = JSON.parseObject(content, ResItem.class);

            List<ResItem> list = App.getHelper().getDao().queryForEq("enText", res.getEnText());
            if (list.size() > 0) {
                res.setId(list.get(0).getId());
                App.getHelper().getDao().update(res);
            } else
                App.getHelper().getDao().create(res);

            //return JSON.toJSONString(res);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping(path = "/api/manRes2")
    StringBody manResContent(@RequestParam(name = "content") String content, HttpResponse response) {
        try {

            ResItem res = JSON.parseObject(content, ResItem.class);

            List<ResItem> list = App.getHelper().getDao().queryForEq("enText", res.getEnText());
            if (list.size() > 0) {
                res.setId(list.get(0).getId());
                App.getHelper().getDao().update(res);
            } else
                App.getHelper().getDao().create(res);

            //return JSON.toJSONString(res);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Type", "text/html;charset=utf-8");
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><script>alert('ok');window.close();</script></body></html>");
        return new StringBody(sb.toString());
    }

    @PostMapping(path = "/api/delRes")
    String delRes(@RequestParam(name = "id") int id) {
        try {
            App.getHelper().getDao().deleteById(id);

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


    @GetMapping("/api/down")
    public void download(HttpRequest request, HttpResponse respone) {
        try {
            com.yanzhenjie.andserver.http.ResponseBody body = new FileDownload(request, respone);
            respone.setBody(body);
        } catch (Throwable e) {
            e.printStackTrace();
        }
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

    @PostMapping(path = "/api/uploadInfo")
    String upload2(HttpRequest request,
                   @RequestParam(name = "path") String path

    ) {

        List<Drive> drives = Utils.getSysAllDriveList();

        Drive drive = drives.get(drives.size() - 1);

        try {

            Folder folder = new Folder();
            folder.setRoot(drive);
            Aid.scanFolder(drive, new File(drive.getP() + "/" + path));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

    @ResponseBody
    @GetMapping(path = "/api/mybi")
    String mybi() throws IOException, ScriptException, SQLException {

        DownloadMP.process();

        return "";
    }
    @GetMapping(path = "/api/vfile")
    void vfile(@RequestParam(name = "id") int id,HttpResponse response) throws SQLException {

        Dao<VFile, Integer> dao = App.getHelper().getDao(VFile.class);
        VFile vfile = dao.queryForId(id);
        String path = vfile.getAbsPath();
        if(new File(path).exists()){

            System.out.println(path+" file exists" );
        }else{

            String url = DownloadMP.getVidoUrl(vfile.getFolder().getBvid(),vfile.getPage());


            new Thread(new Runnable() {
                @Override
                public void run() {
                    String proxyUrl = App.getProxyUrl("http://127.0.0.1:8080/api/vfile?id="+id);
                    new File(path).getParentFile().mkdirs();
                    oInstance.addItem(proxyUrl,path);
                    oInstance.downLoadByList();
                }
            }).start();


            System.out.println(url);
            response.sendRedirect(url);
        }

    }

    /*@GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }*/

}
