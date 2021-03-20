package com.llw.androidtvdemo;


import android.content.Intent;

import com.yanzhenjie.andserver.annotation.Controller;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.http.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

@Controller
public class IndexController {
    @ResponseBody
    @PostMapping(path = "/api/addOrUpdate")
    String push(RequestBody body) throws IOException {
        String content = body.string();
        VideoItem item = new VideoItem();
        try {
            JSONObject jitem = new JSONObject(content);

            item.url=jitem.getString("url");
            item.id=jitem.getInt("id");
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

            item.url=jitem.getString("url");
            item.id=jitem.getInt("id");
            if (item.id > 0) {
                DbHelper.update(item);
            } else
                DbHelper.insert(item);
            App.playList.add(item);

            Intent intent = new Intent();
            intent.setAction(App.URLACTION);
            intent.putExtra("index",App.playList.size()-1);

            App.getInstance().getApplicationContext().sendBroadcast(intent);

            return "ok";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    @ResponseBody
    @GetMapping("/api/delete")
    public String delete(@RequestParam(name="ids") String ids
    ) {
        for(String id:ids.split(","))
            DbHelper.delete(Integer.parseInt(id));
        return "ok";
    }
    @ResponseBody
    @GetMapping("/api/list")
    public String list(
    ) {
        ArrayList<VideoItem> items = DbHelper.getList();
        JSONArray jsonArray = new JSONArray();

        try{
            for(int i = 0;i < items.size();i++){
                VideoItem item = items.get(i);
                JSONObject jitem = new JSONObject();
                jitem.put("id",item.id);
                jitem.put("url",item.url);
                jsonArray.put(jitem);
            }
        }catch (Exception e){e.printStackTrace();}

        return jsonArray.toString();
    }

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

}
