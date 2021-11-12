package com.usbtv.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.nurmemet.nur.nurvideoplayer.TvVideoView;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.view.MyVideoView;
import com.usbtv.demo.vurl.VUrlList;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Map;

public final class PlayerController {

    final static int MODE_RANDOM = 1;
    final static int MODE_SEQ = 0;
    final static int MODE_LOOP = 2;

    private static PlayerController instance;
    private Object mediaObj;
    private Object curItem;

    private int mode;

    private TvVideoView videoView;

    private Map<String, VFile> mapFiles;
    private Uri videoUrl;
    private View girdView;
    private int curTypeId = 1;
    private int curIndex;


    private PlayerController() {
    }


    public void setMediaObj(Object mediaObj) {
        this.mediaObj = mediaObj;
    }


    public static PlayerController getInstance() {
        if (instance == null) instance = new PlayerController();
        return instance;
    }

    public long getDuration() {
        if (mediaObj instanceof MediaPlayer) {
            MediaPlayer m = (MediaPlayer) mediaObj;
            return m.getDuration();
        } else if (mediaObj instanceof VideoView) {
            VideoView v = (VideoView) mediaObj;
            return v.getDuration();
        }
        return 0;
    }

    public long getCurrentPosition() {
        if (mediaObj instanceof MediaPlayer) {
            MediaPlayer m = (MediaPlayer) mediaObj;
            return m.getCurrentPosition();
        } else if (mediaObj instanceof VideoView) {
            VideoView v = (VideoView) mediaObj;
            return v.getCurrentPosition();
        }
        return 0;
    }

    public boolean isPlaying() {
        if (mediaObj instanceof MediaPlayer) {
            MediaPlayer m = (MediaPlayer) mediaObj;
            return m.isPlaying();
        } else if (mediaObj instanceof VideoView) {
            VideoView v = (VideoView) mediaObj;
            return v.isPlaying();
        }
        return false;
    }

    public void seekTo(int pos) {
        if (mediaObj instanceof MediaPlayer) {
            MediaPlayer m = (MediaPlayer) mediaObj;
            m.seekTo(pos);
        } else if (mediaObj instanceof VideoView) {
            VideoView v = (VideoView) mediaObj;
            v.seekTo(pos);
        }

    }

    public void pause() {
        if (mediaObj instanceof MediaPlayer) {
            MediaPlayer m = (MediaPlayer) mediaObj;
            m.pause();
        } else if (mediaObj instanceof VideoView) {
            VideoView v = (VideoView) mediaObj;
            v.pause();
        }
    }

    public void start() {
        if (mediaObj instanceof MediaPlayer) {
            MediaPlayer m = (MediaPlayer) mediaObj;
            m.start();
        } else if (mediaObj instanceof VideoView) {
            VideoView v = (VideoView) mediaObj;
            v.start();
        }
    }

    public void prepare() {
        if (mediaObj instanceof MediaPlayer) {
            MediaPlayer m = (MediaPlayer) mediaObj;
            try {
                m.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }


    public PlayerController play(Object res) {
        this.curItem = res;
        if(res instanceof VFile){
            VFile vf = (VFile) res;
            PlayerController.getInstance().setCurIndex(vf.getOrderSeq());
            SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("id", vf.getId()); //sp.getInt("id",0);
            editor.apply();
            //editor.apply();
            sp.edit().commit();

        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                PlayerController.this.videoUrl = null;
                if (res instanceof VFile) {
                    videoUrl = getUri((VFile) res);
                } else if (res instanceof VUrlList) {
                    videoUrl = ((VUrlList) res).getCurVideoUrl();
                }
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {

                        synchronized (videoView) {


                            videoView.setVisibility(View.VISIBLE);

                            videoView.setVideoURI(PlayerController.this.videoUrl);
                            videoView.requestFocus();
                            videoView.start();
                            PlayerController.getInstance().setMediaObj(videoView);
                            if (res instanceof VFile)
                                MainActivityTest.numTabAdapter.refresh(((VFile) res).getFolder());

                        }
                    }
                });
            }
        }).start();

    return this;
    }

    private Uri getUri(VFile vf) {

        String vremote = "http://127.0.0.1:8080/api/vfile?id=" + vf.getId();

        String path = vf.getAbsPath();

        if (path == null || !new File(path).exists())
            for (Drive d : App.diskList) {
                vf.getFolder().setRoot(d);
                if (vf.exists() && new File(vf.getAbsPath()).canRead()
                ) {
                    try {
                        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);

                        folderDao.update(vf.getFolder());

                        //  path = vf.getAbsPath();
                        break;

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                }
            }

        if (!vf.exists()) {

            if(vf.getdLink()!=null&&vf.getdLink().endsWith(".m3u8")){

               // vremote = "http://127.0.0.1:8080/api/r/"+ URLEncoder.encode(vf.getFolder().getName())+"/"+vf.getOrderSeq() +"/index.m3u8?url="+URLEncoder.encode(vf.getdLink());
               //if(true)return Uri.parse("http://192.168.0.101/32.m3u8?t="+System.currentTimeMillis());

                return Uri.parse(
                        "http://127.0.0.1:8080/api/r/"+ vf.getFolder().getId()
                        +"/"+vf.getOrderSeq()+"/index.m3u8"
                        +"?t="+System.currentTimeMillis()
                );


            }else {
                com.alibaba.fastjson.JSONObject vidoInfo = DownloadMP.getVidoInfo(vf.getFolder().getBvid(), vf.getPage());
                if (vidoInfo != null && null != vidoInfo.getString("video")) {
                    vremote = vidoInfo.getString("video");
                    vremote = App.cache2Disk(vf, vremote);
                }
            }


        } else {
            path = vf.getAbsPath();
            if (new File(path).exists()) {
                vremote = "file://" + path;
            }
        }
        System.out.println(vremote);
        return Uri.parse(App.getProxyUrl(vremote));
    }

    public void prev() {

        if (curItem == null) {
            curItem = new VFile();
            SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
            int id = sp.getInt("id", 0);
            VFile vf = (VFile) curItem;
            vf.setId(id);
        }
        if (curItem instanceof VFile) {
            try {

                do {
                    VFile vf = (VFile) curItem;
                    VFile vfile = App.getHelper().getDao(VFile.class).
                            queryBuilder().where()
                            .lt("id", vf.getId()).queryForFirst();

                    if (vfile != null) {
                        play(vfile);
                        return;
                    }


                } while (true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
    }

    public void next2() {

        if (curItem == null) {
            curItem = new VFile();
            SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
            int id = sp.getInt("id", 0);
            VFile vf = (VFile) curItem;
            vf.setId(id);
        }
        if (curItem instanceof VFile) {
            try {

                do {
                    VFile vf = (VFile) curItem;

                    QueryBuilder<Folder, ?> foldersQueryBuilder = App.getHelper().getDao(Folder.class).
                            queryBuilder();

                    foldersQueryBuilder.where().eq("typeId", curTypeId);

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
                        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("id", vf.getId()); //sp.getInt("id",0);
                        editor.apply();
                        //editor.apply();

                        sp.edit().commit();

                        return;
                    }


                } while (true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
    }

    public void next() {
        try {
        Dao<VFile, Integer> vfDao = App.getHelper().getDao(VFile.class);

        if (curItem == null) {
            SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
            int id = sp.getInt("id", 0);

            curItem = vfDao.queryForId(id);

            if(curItem==null){
                curItem = new VFile();
                VFile vf = (VFile) curItem;
                vf.setId(id);
            }else{
                play(curItem);
                return;
            }

        }
        if (curItem instanceof VFile) {


                do {
                    VFile vf = (VFile) curItem;


                    int vfId = vf.getId();

                    if(vf.getFolder()==null)vf = vfDao.queryForId(vfId);

                    QueryBuilder<Folder, ?> foldersQueryBuilder = App.getHelper().getDao(Folder.class).
                            queryBuilder();

                    foldersQueryBuilder.where().eq("typeId", curTypeId);


                    VFile nextVf = vfDao.queryBuilder().where().eq("folder_id", vf==null?0:vf.getFolder().getId()).and()
                            .gt("id", vfId).queryForFirst();

                    if (nextVf == null){
                        nextVf = App.getHelper().getDao(VFile.class).
                                queryBuilder().join(foldersQueryBuilder).where()
                                .gt("id", vfId).queryForFirst();
                    }

                    if (nextVf == null) {
                        nextVf = (VFile) App.getHelper().getDao(VFile.class).
                                queryBuilder().join(foldersQueryBuilder).where()
                                .gt("id", 0).queryForFirst();
                    }
                    if (nextVf == null) {
                        nextVf = (VFile) App.getHelper().getDao(VFile.class).
                                queryBuilder().where()
                                .gt("id", 0).queryForFirst();
                    }
                    if (nextVf != null) {
                        //vf.setId(vf.getId()+1);
                        play(nextVf);
                        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("id", nextVf.getId()); //sp.getInt("id",0);
                        editor.apply();
                        //editor.apply();

                        sp.edit().commit();

                        return;
                    }


                } while (true);



        } else if (curItem instanceof VUrlList) {
            ((VUrlList) curItem).curNext();
            play(curItem);

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

    public void setTypeId(int typeId) {
        this.curTypeId = typeId;
    }

    public int getCurIndex() {
        return this.curIndex;
    }
    public PlayerController setCurIndex(int i) {
         this.curIndex=i;
        return this;
    }
}
