package com.usbtv.demo.news;

import android.content.Context;
import android.content.SharedPreferences;

import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.App;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UploadItemRepository {

    private Dao<UploadItem, Integer> dao;

    public UploadItemRepository() {

        Dao<UploadItem, Integer> uploadDao = null;
        try {
            uploadDao = App.getHelper().getDao(UploadItem.class);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        this.dao = uploadDao;
    }



    public List<UploadItem> findAllByStatusOrderByDate(int i) {

        try {
            return dao.queryBuilder().where().eq("status", i).query();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public void saveAll(List<UploadItem> list) {

        try {

            for (int i = 0; i < list.size(); i++)
                dao.createOrUpdate(list.get(i));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<UploadItem> findByUrl(String url) {
        try {

            return dao.queryBuilder().where().lt("url", url).query();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public void save(UploadItem item) {

        try {
            dao.createOrUpdate(item);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void houseKeeping() {
        try {

            dao.deleteBuilder().where().lt("dt", new Date().getTime() - 10 * 24 * 3600 * 1000).query();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String getToken() {
        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        return  token;
    }

    public String getSince() {

        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        String token = sp.getString("since", null);
        return  token;
    }

    public String saveSince(String string) {
        SimpleDateFormat df=new  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {

            String since= df.format(new Date(df.parse(string).getTime()+1000));
            SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
            sp.edit().putString("since",string);
            return  since;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return string;
    }

    public List<UploadItem> findByP(String p) {

        try {
            return dao.queryBuilder().where().eq("p", p).query();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;

    }
}
