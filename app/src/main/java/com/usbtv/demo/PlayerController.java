package com.usbtv.demo;

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
    private Integer aIndex;
    private Integer bIndex;
    private int mode;

    private boolean detach;
    private MyImageView imageView;
    private MyVideoView videoView;
    private MyMediaPlayer mediaPlayer;
    private TextView textView;

    private Map<String,VFile> mapFiles;
    public boolean isDetach() {
        return detach;
    }

    public void setDetach(boolean detach) {
        this.detach = detach;
    }

    private View maskView;

    private PlayerController() {
    }

    public Integer getaIndex() {
        return aIndex;
    }

    public void setaIndex(Integer aIndex) {
        this.aIndex = aIndex;
    }

    public Integer getbIndex() {
        return bIndex;
    }

    public void setbIndex(Integer bIndex) {
        this.bIndex = bIndex;
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

    public void setMastView(View view) {
        this.maskView = view;
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

                        videoView.setVideoURI(getUri(vf));
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
                }
            }
        });


    }

    private Uri getUri(VFile vf) {

        String path = vf.getFolder().getRoot().getP() + "/" + vf.getFolder().getP() + "/" + vf.getP();
        String url="";
        if(new File(path).exists()){
            url = "file://" + path;
        }else{
            if(vf.getFolder().getBvid()!=null){
                String vremote = "http://127.0.0.1:8080/api/vfile?id="+vf.getId();
              return Uri.parse(App.getProxyUrl(vremote));
            }
        }
       return Uri.parse(App.getProxyUrl(url));
    }


    public void playNext() {

        if (curItem == null) {
            curItem = new VFile();
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
                        play(curResItem);
                        return;
                    }
                } while (true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    public void setUIs(TextView bgTextView, MyImageView imageView, TextView textView, MyVideoView videoView,
                       MyMediaPlayer mediaPlayer) {
        this.maskView = bgTextView;
        this.imageView = imageView;
        this.videoView = videoView;
        this.mediaPlayer = mediaPlayer;
        this.textView = textView;
    }

    public String getCoverUrl(){
        if(this.curItem == null) return "";
        if(this.curItem instanceof ResItem){
            return ((ResItem) this.curItem).getImgUrl();
        }else if(this.curItem instanceof VFile)
            return ((VFile)(this.curItem)).getFolder().getCoverUrl();
        return "";
    }
    public String getName(){
        if(this.curItem == null) return "";
        if(this.curItem instanceof ResItem){
            return ((ResItem) this.curItem).getEnText();
        }else if(this.curItem instanceof VFile)
            return ((VFile)(this.curItem)).getFolder().getName();
        return "";
    }


}
