package com.llw.androidtvdemo;


import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.yanzhenjie.andserver.annotation.Controller;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Controller
public class IndexController {
    private static String TAG = "IndexController";

    @ResponseBody
    @PostMapping(path = "/api/addOrUpdate")
    String push(RequestBody body) throws IOException {
        String content = body.string();
        VideoItem item = new VideoItem();
        try {
            JSONObject jitem = new JSONObject(content);

            item.url = jitem.getString("url");
            item.id = jitem.getInt("id");
            if (item.id > 0) {
                DbHelper.update(item);
            } else
                DbHelper.insert(item);

            App.playList.add(item);

            return "ok";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    @ResponseBody
    @PostMapping(path = "/api/play")
    String play(RequestBody body) throws IOException {
        String content = body.string();
        VideoItem item = new VideoItem();
        try {
            JSONObject jitem = new JSONObject(content);

            int i=-1;
            for(VideoItem it:App.playList)
            {
                i++;
                if(it.url.equals(jitem.getString("url"))){
                    break;
                }
            }

            Intent intent = new Intent();
            intent.setAction(App.URLACTION);
            intent.putExtra("index", i);

            App.getInstance().getApplicationContext().sendBroadcast(intent);

            return "ok";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    @ResponseBody
    @GetMapping("/api/delete")
    public String delete(@RequestParam(name = "ids") String ids
    ) {
        for (String id : ids.split(","))
            DbHelper.delete(Integer.parseInt(id));
        return "ok";
    }
    @ResponseBody
    @GetMapping("/api/reloadplaylist")
    public String reloadPlayList() {
        DUtils.reloadPlayList();

        return list();
    }


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

    @GetMapping("/down")
    @ResponseBody
    public FileDownload download(HttpRequest request,HttpResponse respone){
        FileDownload body = new FileDownload(request, respone);
        respone.setBody(body);
        return body;

    }
    @ResponseBody
    @GetMapping("/api/list")
    public String list(
    ) {

        ArrayList<VideoItem> items = App.playList;
        JSONArray jsonArray = new JSONArray();

        try {
            for (int i = 0; i < items.size(); i++) {
                VideoItem item = items.get(i);
                JSONObject jitem = new JSONObject();
                jitem.put("id", item.id);
                jitem.put("url", item.url);
                jitem.put("title", item.title);
                jitem.put("status", item.status);

                /*
                String videoUrl = App.getProxy(App.getInstance().getApplicationContext())
                        .getProxyUrl(item.url);

                Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(
                        videoUrl.replace("file://", ""), MediaStore.Video.Thumbnails.MICRO_KIND);
                Bitmap bitmap = Utils.createVideoThumbnail(
                        videoUrl.replace("file://", ""),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                //item.thumb = Utils.bitmapToBase64(videoThumbnail);
                */

                jitem.put("thumb", item.thumb);
                jsonArray.put(jitem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonArray.toString();
    }

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

}
