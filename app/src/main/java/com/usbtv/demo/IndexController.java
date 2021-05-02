package com.usbtv.demo;


import com.alibaba.fastjson.JSON;
import com.yanzhenjie.andserver.annotation.Controller;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

@RestController
public class IndexController {
    private static String TAG = "IndexController";


    @ResponseBody
    @GetMapping(path = "/api/status")
    String status(RequestBody body,HttpResponse response) throws IOException {
        response.setHeader("Content-Type","application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("aIndex", App.playList.getaIndex());
            jsonObject.put("bIndex", App.playList.getbIndex());
            jsonObject.put("progress", (App.getVideoView().getCurrentPosition()));
            jsonObject.put("curPosition", (App.getVideoView().getCurrentPosition()));
            jsonObject.put("duration", App.getVideoView().getDuration());
            jsonObject.put("isPlaying", App.getVideoView().isPlaying());


        }catch (Exception e){
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

            JSONObject params = new JSONObject(content);

            int aIndex=params.getInt("aIndex");
            int bIndex = params.getInt("bIndex");
            boolean isPlaying = params.getBoolean("isPlaying");
            double progress = params.getDouble("progress");
            if(aIndex == App.playList.getaIndex()&&bIndex == App.playList.getbIndex()){
                App.getVideoView().seekTo((int) (progress*App.getVideoView().getDuration()));
            }else  App.sendPlayBroadCast(aIndex,bIndex);
            if(isPlaying!=App.getVideoView().isPlaying()){
                if(isPlaying)
                App.getVideoView().start();
                else App.getVideoView().pause();
            }


            return "ok";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
    @GetMapping(path = "/api/url")
    String url(@RequestParam(name = "aIndex") int aIndex,@RequestParam(name = "bIndex") int bIndex,HttpResponse response) throws IOException {
        String link = App.playList.getUrl(aIndex,bIndex);
        if(link.matches("http[s]://.*"))
            response.sendRedirect(link);
        else if(link.startsWith("file://")){
            response.sendRedirect("/api/down?path="+link.substring(7));
        }
        return null;
    }
    @GetMapping(path = "/api/delete")
    String delete(@RequestParam(name = "aIndex") int aIndex,@RequestParam(name = "bIndex") int bIndex,HttpResponse response) throws IOException {
         App.playList.delete(aIndex,bIndex);

        return null;
    }

    @ResponseBody
    @PostMapping(path = "/api/event")
    String event(RequestBody body) throws IOException {
        String content = body.string();
        try {
            JSONObject params = new JSONObject(content);
            String str=params.getString("event");
            if(App.exit.equals(str)){
                App.sendExit();
            }
            Utils.exec(str);
            return "ok";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
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



    @GetMapping("/api/down")
    public void download(HttpRequest request,HttpResponse respone){
        try{
            com.yanzhenjie.andserver.http.ResponseBody  body = new FileDownload(request, respone);
            respone.setBody(body);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    @ResponseBody
    @GetMapping("/api/list.json")
    public String getJsonList(){
        return JSON.toJSONString(App.playList.getAidList());
    }

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

}
