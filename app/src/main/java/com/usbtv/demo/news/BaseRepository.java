package com.usbtv.demo.news;

import android.content.Context;
import android.content.SharedPreferences;

import com.usbtv.demo.comm.App;
import com.usbtv.demo.news.video.CcVideo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class BaseRepository  {

    public abstract String getSinceId();

    public String getToken() {
        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        return  token;
    }

    public String getSince() {

        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        String token = sp.getString(getSinceId(), null);
        return  token;
    }

    public String saveSince(String string) {
        SimpleDateFormat df=new  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {

            String since= df.format(new Date(df.parse(string).getTime()+1000));
            SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
            sp.edit().putString(getSinceId(),string);
            return  since;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return string;
    }

}
