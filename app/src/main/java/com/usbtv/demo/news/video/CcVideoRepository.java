package com.usbtv.demo.news.video;

import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.news.BaseRepository;
import com.usbtv.demo.news.UploadItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CcVideoRepository extends BaseRepository {

    private Dao<CcVideo, Integer> dao;
    public CcVideoRepository() {

        try {
            dao = App.getHelper().getDao(CcVideo.class);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public String getSinceId() {
        return "videosince";
    }

    public List<CcVideo> findByVid(String vid) {

        try {
            return dao.queryBuilder().where().eq("vid", vid).query();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return new ArrayList<>();
    }


    public List<CcVideo> findAllBySrcAndCcIsNull(String src) {
        try {
            return dao.queryBuilder().where().eq("src", src)
                    .and().isNull("cc")
                    .query();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new ArrayList<CcVideo>();
    }


    public List<CcVideo> findAllByStatusOrderByDt(int status) {
        try {
            return dao.queryBuilder().where().eq("status", status)
                    .query();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new ArrayList<CcVideo>();
    }


    public void save(CcVideo video) {
        try {
            dao.createOrUpdate(video);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void saveAll(List<CcVideo> list) {
        try {
            for (CcVideo video : list) {

                dao.createOrUpdate(video);

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
