package com.usbtv.demo;


import android.media.MediaPlayer;

import com.alibaba.fastjson.JSON;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.ResItem;
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
import com.yanzhenjie.andserver.util.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;

@RestController
public class IndexController {
    private static String TAG = "IndexController";

    @ResponseBody
    @GetMapping(path = "/api/status")
    String status(RequestBody body, HttpResponse response) throws IOException {
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        PlayerController.getInstance().setaIndex(App.playList.getaIndex());
        PlayerController.getInstance().setbIndex(App.playList.getbIndex());PlayerController.getInstance().getDuration();
        return JSON.toJSONString(PlayerController.getInstance());
    }


    @ResponseBody
    @GetMapping(path = "/api/reLoadPlayList")
    String reLoadPlayList(RequestBody body) throws IOException {

        DowloadPlayList.loadPlayList(false);

        return "ok";
    }

    @ResponseBody
    @GetMapping(path = "/api/cmd")
    String cmd(

            @RequestParam(name = "cmd") String cmd,
            @RequestParam(name = "val", required = false, defaultValue = "") String val,
            @RequestParam(name = "id", required = false, defaultValue = "-1") int id,
            @RequestParam(name = "typeId",required = false, defaultValue = "0") int typeId,
            @RequestParam(name = "aIndex", required = false, defaultValue = "-1") int aIndex,
            @RequestParam(name = "bIndex", required = false, defaultValue = "-1") int bIndex
    ) {

        if ("play".equals(cmd)) {

            PlayerController.getInstance().getCurItem().setId(id);
            PlayerController.getInstance().getCurItem().setTypeId(typeId);

            if (typeId == ResItem.VIDEO) {
                App.schedule(aIndex, bIndex);
            } else {
                App.schedule(-1, -1);

            }
        }     else   if ("next".equals(cmd)) {


            if (typeId != ResItem.VIDEO) {
                PlayerController.getInstance().getCurItem().setId(PlayerController.getInstance().getCurItem().getId()+1);
            }

            App.schedule(-1, -1);

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

        }

        return "ok";

    }


    @GetMapping(path = "/api/schedule")
    @ResponseBody
    synchronized String schedule() throws IOException {


        if (PlayerController.getInstance().getCurItem().getTypeId() != ResItem.VIDEO) {
            try {
                QueryBuilder builder = App.getHelper().getDao().queryBuilder();


                do {
                    Where where = builder.where().eq("typeId", PlayerController.getInstance().getCurItem().getTypeId()).and().ge("id", PlayerController.getInstance().getCurItem().getId());
                    ResItem curResItem = (ResItem) where.queryForFirst();
                    if (curResItem == null) {
                        PlayerController.getInstance().getCurItem().setId(0);
                        continue;
                    }

                    PlayerController.getInstance().getCurItem().setId(curResItem.getId() + 1);
                    return JSON.toJSONString(curResItem);

                } while (true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


        return JSON.toJSONString(PlayerController.getInstance().getCurItem());
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

    @GetMapping(path = "/api/url")
    String url(@RequestParam(name = "aIndex") int aIndex, @RequestParam(name = "bIndex") int bIndex, HttpResponse response) throws IOException {
        String link = App.playList.getUrl(aIndex, bIndex);
        if (link.matches("http[s]://.*"))
            response.sendRedirect(link);
        else if (link.startsWith("file://")) {
            response.sendRedirect("/api/down?path=" + link.substring(7));
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
            long pageSize = 20;
            long total = App.getHelper().getDao().queryBuilder().where().eq("typeId", typeId).countOf();
            List<ResItem> list = App.getHelper().getDao().queryBuilder()
                    .where().eq("typeId", typeId).queryBuilder()
                    .offset((page - 1) * pageSize)
                    .limit(20l).query();
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("datas", list);
            result.put("page", page);
            result.put("total", total);
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
        App.playList.delete(aIndex, bIndex);

        return null;
    }

    @ResponseBody
    @PostMapping(path = "/api/event")
    String event(RequestBody body) throws IOException {
        String content = body.string();
        try {
            JSONObject params = new JSONObject(content);
            String str = params.getString("event");
            if (App.exit.equals(str)) {
                App.sendExit();
            }
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

    @ResponseBody
    @GetMapping("/api/list.json")
    public String getJsonList() {
        return JSON.toJSONString(App.playList.getAidList());
    }

    /*@GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }*/

}
