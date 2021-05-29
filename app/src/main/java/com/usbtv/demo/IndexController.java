package com.usbtv.demo;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.danikula.videocache.file.Md5FileNameGenerator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.usbtv.demo.data.ResItem;
import com.yanzhenjie.andserver.annotation.Controller;
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
import java.io.OutputStream;
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
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("aIndex", App.playList.getaIndex());
            jsonObject.put("bIndex", App.playList.getbIndex());
            jsonObject.put("progress", (App.getVideoView().getCurrentPosition()));
            jsonObject.put("curPosition", (App.getVideoView().getCurrentPosition()));
            jsonObject.put("duration", App.getVideoView().getDuration());
            jsonObject.put("isPlaying", App.getVideoView().isPlaying());


        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    @ResponseBody
    @GetMapping(path = "/api/reLoadPlayList")
    String reLoadPlayList(RequestBody body) throws IOException {

        DowloadPlayList.loadPlayList(false);

        return "ok";
    }

    @ResponseBody
    @PostMapping(path = "/api/play")
    String play(RequestBody body) throws IOException {
        String content = body.string();
        try {
            if (App.curResItem == null) App.curResItem = new ResItem();
            App.curResItem.setTypeId(ResItem.VIDEO);
            JSONObject params = new JSONObject(content);

            int aIndex = params.getInt("aIndex");
            int bIndex = params.getInt("bIndex");
            boolean isPlaying = params.getBoolean("isPlaying");
            double progress = params.getDouble("progress");
            if (aIndex == App.playList.getaIndex() && bIndex == App.playList.getbIndex()) {
                App.getVideoView().seekTo((int) (progress * App.getVideoView().getDuration()));
            } else App.sendPlayBroadCast(aIndex, bIndex);
            if (isPlaying != App.getVideoView().isPlaying()) {
                if (isPlaying)
                    App.getVideoView().start();
                else App.getVideoView().pause();
            }


            return "ok";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    @GetMapping(path = "/api/schedule")
    @ResponseBody
    synchronized String schedule() throws IOException {

        if (App.curResItem == null) App.curResItem = new ResItem();

        if (App.curResItem.getTypeId() == 1) {
            try {
                QueryBuilder builder = App.getHelper().getDao().queryBuilder();


                do {
                    Where where = builder.where().eq("typeId", ResItem.IMAGE).and().ge("id", App.curResItem.getId());
                    ResItem curResItem = (ResItem) where.queryForFirst();
                    if (curResItem == null) {
                        App.curResItem.setId(0);
                        continue;
                    }

                    App.curResItem.setId(curResItem.getId() + 1);
                    return JSON.toJSONString(curResItem);

                } while (true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


        return JSON.toJSONString(App.curResItem);
    }

    @GetMapping(path = "/api/playres")
    @ResponseBody
    synchronized String playres(@RequestParam(name = "id") int id, @RequestParam(name = "typeId") int typeId) throws IOException {

        if (App.curResItem == null)
            App.curResItem = new ResItem();
        ;
        App.curResItem.setId(id);
        App.curResItem.setTypeId(typeId);
        App.sendPlayBroadCast(-1, -1);
        return "OK";
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

    @GetMapping(path = "/api/manRes")
    @ResponseBody
    String getResList(@RequestParam(name = "page") int page) {

        try {
            long pageSize = 20;
            long total = App.getHelper().getDao().countOf();
            List<ResItem> list = App.getHelper().getDao().queryBuilder()
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

    //https://fanyi.baidu.com/gettts?lan=zh&text=%E4%BD%A0%E5%A5%BD&spd=5&source=web
   /* @ResponseBody
    @GetMapping("/api/get")
    public String ib(HttpRequest req, HttpResponse resp)  {
        try {
            String uri = req.getURI();
            Log.d(TAG, uri);
            String rurl = uri.split("url=")[1];

            JSONObject json =Bili.getParams(rurl);
            if(json!=null){
                String videoUrl = json.getString("url");
                if(uri.indexOf("saveurlonly")>-1){
                   VideoItem item = DbHelper.UpdateOrInsert(rurl,json.getString("title"));
                    return "updateOrInsert ok id:"+item.id;
                }
                if(videoUrl!=null){
                    resp.sendRedirect(videoUrl);
                }

            }

        }catch (Throwable e){
            e.printStackTrace();
        }

        return "ok";
    }*/


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
