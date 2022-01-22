package com.usbtv.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.nurmemet.nur.nurvideoplayer.TvVideoView;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import java.sql.SQLException;
import java.util.List;

public final class PlayerController {

    final static int MODE_RANDOM = 1;
    final static int MODE_SEQ = 0;
    final static int MODE_LOOP = 2;

    private static PlayerController instance;
    private VFile curItem;

    private int mode;

    private TvVideoView videoView;
    private Uri videoUrl;
    private View girdView;
    private int curIndex;


    private PlayerController() {
    }


    public static PlayerController getInstance() {
        if (instance == null) instance = new PlayerController();
        return instance;
    }

    public long getDuration() {

        return videoView == null ? 0 : videoView.getDuration();
    }

    public long getCurrentPosition() {
        return videoView == null ? 0 : videoView.getCurrentPosition();

    }

    public boolean isPlaying() {
        return videoView == null ? false : videoView.isPlaying();

    }

    public void seekTo(int pos) {
        if (videoView != null) videoView.seekTo(pos);

    }

    public void pause() {
        if (videoView != null) videoView.pause();
    }

    public void start() {
        if (videoView != null) videoView.start();

    }

    public void prepare() {
    }


    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }


    public PlayerController play(VFile res) {
        this.curItem = res;
        VFile vf = (VFile) res;
        PlayerController.getInstance().setCurIndex(vf.getOrderSeq());
        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("id", vf.getId());
        editor.apply();
        sp.edit().commit();


        new Thread(new Runnable() {
            @Override
            public void run() {

                String title = "";
                PlayerController.this.videoUrl = null;
                videoUrl = App.getUri( res);
                title = res.getName();

                Handler handler = new Handler(Looper.getMainLooper());
                String finalTitle = title;
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        synchronized (videoView) {


                            videoView.pause();
                            videoView.setVideoURI(PlayerController.this.videoUrl, finalTitle);
                            //videoView.requestFocus();
                            videoView.resume();
                            MainActivity.numTabAdapter.refresh(res);

                        }
                    }
                });
            }
        }).start();

        return this;
    }


    public void prev() {
    }

    public void nextFolder() {

        if (curItem == null) {
                return;
        }
            try {


                    VFile vf =  curItem;

                    QueryBuilder<Folder, ?> foldersQueryBuilder = App.getHelper().getDao(Folder.class).
                            queryBuilder();

                    foldersQueryBuilder.where().eq("typeId", curItem.getFolder().getTypeId());

                    VFile vfile =
                            App.getHelper().getDao(VFile.class).
                                    queryBuilder().join(foldersQueryBuilder).where()
                                    .gt("folder_id", vf.getFolder().getId()).queryForFirst();

                    if (vfile == null) {
                        vfile = (VFile) App.getHelper().getDao(VFile.class).
                                queryBuilder().join(foldersQueryBuilder).where()
                                .gt("folder_id", 0).queryForFirst();
                    }
                    if (vfile != null) {
                        play(vfile);
                    }


            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }


    }

    public void next() {
        try {
            Dao<VFile, Integer> vfDao = App.getHelper().getDao(VFile.class);

            int curId = 0;
            if (curItem == null) {
                SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);

                curId = sp.getInt("id", curId);
                curItem = vfDao.queryForId(curId);

                if (curItem != null) {
                    play(curItem);
                    return;
                }else{
                    curItem = vfDao.queryBuilder().where().gt("id",curId).queryForFirst();
                    if(curItem==null)
                     curItem = vfDao.queryBuilder().where().lt("id",curId).queryForFirst();

                }

            }

            if(curItem==null) return;



                VFile vf =  curItem;


                int vfId = vf.getId();

                if (vf.getFolder() == null) vf = vfDao.queryForId(vfId);

                QueryBuilder<Folder, ?> foldersQueryBuilder = App.getHelper().getDao(Folder.class).
                        queryBuilder();

                foldersQueryBuilder.where().eq("typeId", vf.getFolder().getTypeId());


                VFile nextVf = vfDao.queryBuilder().where().eq("folder_id",  vf.getFolder().getId()).and()
                        .gt("id", vfId).queryForFirst();


                if (nextVf == null) {
                    nextVf = App.getHelper().getDao(VFile.class).
                            queryBuilder().join(foldersQueryBuilder).where()
                            .gt("id", vfId).queryForFirst();

                    if (nextVf == null) {
                        nextVf = (VFile) App.getHelper().getDao(VFile.class).
                                queryBuilder().join(foldersQueryBuilder).where()
                                .gt("id", 0).queryForFirst();
                    }

                }


                if (nextVf == null) {
                    nextVf = (VFile) App.getHelper().getDao(VFile.class).
                            queryBuilder().join(foldersQueryBuilder).where()
                            .gt("id", 0).queryForFirst();
                }

                if (nextVf != null) {
                    play(nextVf);
                }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void playNext() {

        if (mode == MODE_LOOP && curItem != null) {
            play(curItem);
            return;
        }
        next();

    }

    public void setUIs(TvVideoView videoView,
                       View gridView) {
        this.videoView = videoView;
        this.girdView = gridView;
    }

    public String getCoverUrl() {
        if (this.curItem instanceof VFile)
            return ((VFile) (this.curItem)).getFolder().getCoverUrl();
        return "";
    }

    public String getName() {
        if (this.curItem instanceof VFile)
            return ((VFile) (this.curItem)).getFolder().getName();
        return "";
    }


    public PlayerController hideMenu() {
        this.girdView.setVisibility(View.GONE);
        return this;
    }



    public int getCurIndex() {
        return this.curIndex;
    }

    public PlayerController setCurIndex(int i) {
        this.curIndex = i;
        return this;
    }

    public void play(List<Folder> mList, int position) {

        this.play(mList.get(position).getFiles().iterator().next());
    }
}
