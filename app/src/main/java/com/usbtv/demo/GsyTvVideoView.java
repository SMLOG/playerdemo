package com.usbtv.demo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.data.VFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager;
import tv.danmaku.ijk.media.exo2.ExoSourceManager;

public class GsyTvVideoView extends StandardGSYVideoPlayer {
    public GsyTvVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
       // PlayerFactory.setPlayManager(Exo2PlayerManager.class);
        PlayerFactory.setPlayManager(IjkPlayerManager.class);
      //  CacheFactory.setCacheManager(ProxyCacheManager.class);
       // CacheFactory.setCacheManager(ExoPlayerCacheManager.class);



    }

    @Override
    public void onCompletion() {
        super.onCompletion();
        PlayerController.getInstance().next();
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);
        PlayerController.getInstance().next();
    }

    public void setVideoURI(Uri videoUrl, VFile res, int fi) {

        Map header = new HashMap<>();

        header.put("allowCrossProtocolRedirects", "true");
        setMapHeadData(header);

        VFile[] files = res.getFolder().getFiles().toArray(new VFile[]{});
        int curIndex = 0;

        /*List<GSYVideoModel> urls = new ArrayList<>();

        for (int i = curIndex; i < files.length; i++) {
            urls.add(
                    new GSYVideoModel(files[i].getdLink() != null ? App.getUrl(files[i].getdLink()) :
                            App.getProxyUrl(SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl?id=" + files[i].getId())

                            , files[i].getName()));
        }*/

        setUp(res.getdLink() != null ? App.getUrl(res.getdLink()) :
                App.getProxyUrl(SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl?id=" + res.getId()),
false
                , res.getName());

        setMapHeadData(header);
        prepareVideo();
        startPlayLogic();


    }

    private boolean playing;

    public boolean isPlaying() {

        updateHandler.post(() -> {
            playing = this.getCurrentState()==CURRENT_STATE_PLAYING;
        });
        return playing;

    }

    private Handler updateHandler = new Handler(Looper.getMainLooper());
    private long currentPosition;

    private int duration;
    public int getDuration(){
        updateHandler.post(() -> {
            duration = super.getDuration();
        });
        return duration;
    }
    public long getCurrentPosition() {

        updateHandler.post(() -> {
            currentPosition = super.getCurrentPositionWhenPlaying();
        });
        return currentPosition;
    }

    public void pause() {
        updateHandler.post(() -> {
            super.onVideoPause();

           // super.pauseLogic(null, true);
        });
    }

    public void start() {
       /* updateHandler.post(() -> {
            super.onVideoResume();
            // super.startPlayLogic();
        });*/
    }

    public void resume() {
        updateHandler.post(() -> {
            super.onVideoResume();
        });
    }

    public boolean onKeyDown(int keyCode) {
        return false;
    }
}
