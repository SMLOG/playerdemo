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

import com.alibaba.fastjson.JSON;
import com.danikula.videocache.CacheListener;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.His;
import com.usbtv.demo.data.ResItem;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.view.MyImageView;
import com.usbtv.demo.view.MyMediaPlayer;
import com.usbtv.demo.view.MyVideoView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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

    private boolean detach;
    private MyImageView imageView;
    private MyVideoView videoView;
    private MyMediaPlayer mediaPlayer;
    private TextView textView;

    private Map<String, VFile> mapFiles;
    private Uri videoUrl;

    public boolean isDetach() {
        return detach;
    }

    public void setDetach(boolean detach) {
        this.detach = detach;
    }

    private View maskView;

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

    public void showMaskView() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PlayerController.this.maskView.bringToFront();
                PlayerController.this.maskView.setVisibility(View.VISIBLE);
            }
        });

    }

    public void hideMaskView() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PlayerController.this.maskView.setVisibility(View.GONE);
            }
        });
    }



    public boolean isShowMask() {
        return this.maskView.getVisibility() == View.VISIBLE;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }


    public void play(Object res) {
        this.curItem = res;

        new Thread(new Runnable() {
            @Override
            public void run() {

                PlayerController.this.videoUrl = null;
                if (res instanceof VFile) {
                    videoUrl = getUri((VFile) res);
                }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (res instanceof VFile) {
                            VFile vf = (VFile) res;
                            synchronized (videoView) {

                                textView.setVisibility(View.GONE);
                                imageView.setVisibility(View.GONE);
                                if (mediaPlayer.isPlaying()) mediaPlayer.stop();

                                videoView.setVisibility(View.VISIBLE);

                                videoView.setVideoURI(PlayerController.this.videoUrl);
                                videoView.requestFocus();
                                videoView.start();
                                PlayerController.getInstance().setMediaObj(videoView);
                            }

                        } else if (res instanceof ResItem) {
                            ResItem item = (ResItem) res;
                            try {
                                synchronized (mediaPlayer) {
                                    PlayerController.getInstance().setMediaObj(mediaPlayer);
                                    textView.setVisibility(View.VISIBLE);
                                    videoView.pause();
                                    videoView.setVisibility(View.GONE);
                                    imageView.setVisibility(View.VISIBLE);
                                    textView.setText(item.getEnText() + " " + item.getCnText());
                                    imageView.setUrl(App.getProxyUrl(item.getImgUrl()));
                                    mediaPlayer.reset();

                                    if (item.getTypeId() == ResItem.IMAGE) {
                                        String url = "https://fanyi.baidu.com/gettts?lan=en&text=" + URLEncoder.encode(item.getEnText()) + "&spd=3&source=web";
                                        mediaPlayer.addPlaySource(App.getProxyUrl(url), 0);

                                        url = "https://fanyi.baidu.com/gettts?lan=zh&text=" + URLEncoder.encode(item.getCnText()) + "&spd=3&source=web";
                                        mediaPlayer.addPlaySource(App.getProxyUrl(url), 0);
                                        if (item.getSound() != null) {
                                            mediaPlayer.addPlaySource(App.getProxyUrl(item.getSound()), 10000);
                                        }
                                    } else {

                                        PlayerController.getInstance().showMaskView();
                                        if (item.getSound() != null) {

                                            mediaPlayer.addPlaySource(App.getProxyUrl(item.getSound()), 0);
                                        }
                                    }


                                    mediaPlayer.prepare();

                                    mediaPlayer.start();
                                }


                            } catch (Throwable tt) {
                                PlayerController.getInstance().playNext();
                            }
                        } else if (res instanceof His) {
                            His item = (His) res;
                            try {
                                synchronized (mediaPlayer) {
                                    PlayerController.getInstance().setMediaObj(mediaPlayer);
                                    textView.setVisibility(View.VISIBLE);
                                    videoView.pause();
                                    videoView.setVisibility(View.GONE);
                                    imageView.setVisibility(View.VISIBLE);
                                    textView.setText(item.getLangText() + " " + item.getCn().getCnText());
                                    imageView.setUrl(App.getProxyUrl(item.getCn().getImgUrl()));
                                    mediaPlayer.reset();

                                    String url = "https://fanyi.baidu.com/gettts?lan=" + item.getLang() + "&text=" + URLEncoder.encode(item.getLangText()) + "&spd=5&source=web";
                                    System.out.println(url);
                                    mediaPlayer.addPlaySource(App.getProxyUrl(url), 0);

                                    url = "https://fanyi.baidu.com/gettts?lan=zh&text=" + URLEncoder.encode(item.getCn().getCnText()) + "&spd=5&source=web";
                                    mediaPlayer.addPlaySource(App.getProxyUrl(url), 0);
                                    mediaPlayer.prepare();
                                    mediaPlayer.start();
                                }

                            } catch (Throwable tt) {
                                PlayerController.getInstance().playNext();
                            }
                        }
                    }
                });
            }
        }).start();


    }

    private Uri getUri(VFile vf) {

        String vremote = "http://127.0.0.1:8080/api/vfile?id=" + vf.getId();

        if (vf.getFolder().getRoot() == null || !new File(vf.getFolder().getRoot().getP()).exists()) {

            com.alibaba.fastjson.JSONObject vidoInfo = DownloadMP.getVidoInfo(vf.getFolder().getBvid(), vf.getPage());
            if (vidoInfo != null && null != vidoInfo.getString("video")) {
                vremote = vidoInfo.getString("video");
            }

        } else {
            String path = vf.getFolder().getRoot().getP() + "/" + vf.getFolder().getP() + "/" + vf.getP();
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

        } else if (curItem instanceof His) {
            try {

                do {
                    His vf = (His) curItem;
                    His vfile = App.getHelper().getDao(His.class).
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

        } else {

            try {
                QueryBuilder builder = App.getHelper().getDao().queryBuilder();
                ResItem vi = (ResItem) this.curItem;

                do {
                    ResItem curResItem = (ResItem) builder.where().eq("typeId", vi.getTypeId())
                            .and().ge("id", vi.getId()).queryForFirst();
                    if (curResItem == null) {
                        curResItem = (ResItem) builder.where().eq("typeId", vi.getTypeId())
                                .and().lt("id", 0).queryForFirst();
                    }
                    if (curResItem != null) {
                        play(curResItem);
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
                    VFile vfile = App.getHelper().getDao(VFile.class).
                            queryBuilder().where()
                            .gt("folder_id", vf.getFolder().getId()).queryForFirst();
                    if (vfile == null) {
                        vfile = (VFile) App.getHelper().getDao(VFile.class).
                                queryBuilder().where()
                                .gt("id", 0).queryForFirst();
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

        } else if (curItem instanceof His) {
            try {

                do {
                    His vf = (His) curItem;
                    His vfile = App.getHelper().getDao(His.class).
                            queryBuilder().where()
                            .gt("id", vf.getId()).queryForFirst();
                    if (vfile == null) {
                        vfile = (His) App.getHelper().getDao(His.class).
                                queryBuilder().where()
                                .gt("id", 0).queryForFirst();
                    }
                    if (vfile != null) {
                        vf.setId(vf.getId() + 1);
                        play(vfile);
                        return;
                    }


                } while (true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        } else {

            try {
                QueryBuilder builder = App.getHelper().getDao().queryBuilder();
                ResItem vi = (ResItem) this.curItem;

                do {
                    ResItem curResItem = (ResItem) builder.where().eq("typeId", vi.getTypeId())
                            .and().ge("id", vi.getId()).queryForFirst();
                    if (curResItem == null) {
                        curResItem = (ResItem) builder.where().eq("typeId", vi.getTypeId())
                                .and().ge("id", 0).queryForFirst();
                    }
                    if (curResItem != null) {
                        curResItem.setId(curResItem.getId() + 1);
                        play(curResItem);
                        return;
                    }
                } while (true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void next() {

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
                            .gt("id", vf.getId()).queryForFirst();
                    if (vfile == null) {
                        vfile = (VFile) App.getHelper().getDao(VFile.class).
                                queryBuilder().where()
                                .gt("id", 0).queryForFirst();
                    }
                    if (vfile != null) {
                        //vf.setId(vf.getId()+1);
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

        } else if (curItem instanceof His) {
            try {

                do {
                    His vf = (His) curItem;
                    His vfile = App.getHelper().getDao(His.class).
                            queryBuilder().where()
                            .gt("id", vf.getId()).queryForFirst();
                    if (vfile == null) {
                        vfile = (His) App.getHelper().getDao(His.class).
                                queryBuilder().where()
                                .gt("id", 0).queryForFirst();
                    }
                    if (vfile != null) {
                        vf.setId(vf.getId() + 1);
                        play(vfile);
                        return;
                    }


                } while (true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        } else {

            try {
                QueryBuilder builder = App.getHelper().getDao().queryBuilder();
                ResItem vi = (ResItem) this.curItem;

                do {
                    ResItem curResItem = (ResItem) builder.where().eq("typeId", vi.getTypeId())
                            .and().ge("id", vi.getId()).queryForFirst();
                    if (curResItem == null) {
                        curResItem = (ResItem) builder.where().eq("typeId", vi.getTypeId())
                                .and().ge("id", 0).queryForFirst();
                    }
                    if (curResItem != null) {
                        curResItem.setId(curResItem.getId() + 1);
                        play(curResItem);
                        return;
                    }
                } while (true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void playNext() {

        if (mode == MODE_LOOP && curItem != null) {
            play(curItem);
            return;
        }
        next();

    }

    public void setUIs(TextView bgTextView, MyImageView imageView, TextView textView, MyVideoView videoView,
                       MyMediaPlayer mediaPlayer) {
        this.maskView = bgTextView;
        this.imageView = imageView;
        this.videoView = videoView;
        this.mediaPlayer = mediaPlayer;
        this.textView = textView;
    }

    public String getCoverUrl() {
        if (this.curItem == null) return "";
        if (this.curItem instanceof ResItem) {
            return ((ResItem) this.curItem).getImgUrl();
        } else if (this.curItem instanceof VFile)
            return ((VFile) (this.curItem)).getFolder().getCoverUrl();
        return "";
    }

    public String getName() {
        if (this.curItem == null) return "";
        if (this.curItem instanceof ResItem) {
            return ((ResItem) this.curItem).getEnText();
        } else if (this.curItem instanceof VFile)
            return ((VFile) (this.curItem)).getFolder().getName();
        return "";
    }


}
