package com.usbtv.demo.r;

import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.App;
import com.usbtv.demo.data.Folder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VideoProvider {
    public static List<Folder> getMovieList(String val) {

        try {
            Dao<Folder, Integer> dao = App.getHelper().getDao(Folder.class);
          return   dao.queryBuilder().where().eq("typeId",val).queryBuilder()
                  .orderBy("id",false).query();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return  new ArrayList<>();
    }
}
