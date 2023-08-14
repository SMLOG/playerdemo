package com.usbtv.demo.news;


import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.usbtv.demo.comm.App;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

@RestController
public class NewsController {
    private static String TAG = "NewsController";

    @ResponseBody
    @GetMapping("/api/news/token")
    public String token(@RequestParam(name = "token",required = false,defaultValue = "") String token) {
        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();

        editor.putString("token", token);
        editor.apply();
        sp.edit().commit();
        return "OK";
    }

    @ResponseBody
    @GetMapping("/api/news")
    public String news() {

        try {
            List<UploadItem> items = App.getHelper().getDao(UploadItem.class).queryBuilder().orderBy("date",false).limit(100l).query();

            return JSON.toJSONString(items);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return "OK";
    }

}
