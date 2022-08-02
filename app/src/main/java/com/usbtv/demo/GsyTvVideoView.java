package com.usbtv.demo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.Toast;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.exo.GSYExo2ListPlayerView;
import com.usbtv.demo.exo.MyExo2PlayerManager;
import com.usbtv.demo.exo.MyExo2VideoManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class GsyTvVideoView extends GSYExo2ListPlayerView {
    private boolean releaseCompleted;

    public GsyTvVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PlayerFactory.setPlayManager(Exo2PlayerManager.class);
        PlayerFactory.setPlayManager(IjkPlayerManager.class);
        //PlayerFactory.setPlayManager(GSYExoPlayerManager.class);

        //  CacheFactory.setCacheManager(ProxyCacheManager.class);
        // CacheFactory.setCacheManager(ExoPlayerCacheManager.class);
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);

    }

    @Override
    public void onAutoCompletion() {


        PlayerController.getInstance().incPlayCount();
        PlayerController.getInstance().setCurIndex(PlayerController.getInstance().getCurIndex()+1);

        if(getGSYVideoManager() instanceof MyExo2VideoManager){
            mPlayPosition++;
        }else{
            playNext();
        }
    }
    @Override
    public void onCompletion() {
        super.onCompletion();
        if(!releaseCompleted) PlayerController.getInstance().next();
        else releaseCompleted=false;
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);
        Toast.makeText(App.getInstance().getApplicationContext(), "播放出错", Toast.LENGTH_SHORT).show();

        if(mPlayPosition<=mUriList.size()-1){
            playNext();
        }else PlayerController.getInstance().playNextFolder();
    }

    @Override
    public GSYVideoViewBridge getGSYVideoManager() {
        if( PlayerFactory.getPlayManager() instanceof  MyExo2PlayerManager){
            MyExo2VideoManager.instance().initContext(getContext().getApplicationContext());
            return MyExo2VideoManager.instance();
        }else{
            GSYVideoManager.instance().initContext(getContext().getApplicationContext());
            return GSYVideoManager.instance();
        }

    }



    public void setVideoURI(Uri videoUrl, VFile res, int fi) {

        //releaseVideos();
        mPauseBeforePrepared=false;
        getGSYVideoManager().pause();
        if(res.getFolder().getTypeId()>=500 && res.getFolder().getTypeId()<600){
            PlayerFactory.setPlayManager(IjkPlayerManager.class);
            // GSYVideoManager.onPause();
            //  GSYExoVideoManager.onPause();
        }else{
            PlayerFactory.setPlayManager(MyExo2PlayerManager.class);
            //  GSYVideoManager.onPause();

        }

        setUpUrls(res, fi);
        releaseCompleted = true;
        startPlayLogic();
        releaseCompleted = false;

    }

    private void setUpUrls(VFile res, int fi) {
        Map header = new HashMap<>();

        header.put("allowCrossProtocolRedirects", "true");
        setMapHeadData(header);

        VFile[] files = res.getFolder().getFiles().toArray(new VFile[]{});
        int curIndex = fi;


        List<GSYVideoModel> urls = new ArrayList<>();

        for (int i = curIndex; i < files.length; i++) {
            urls.add(
                    new GSYVideoModel(files[i].getdLink() != null ? App.getUrl(files[i].getdLink()) :
                            (SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl.mp4?id=" + files[i].getId())

                            , files[i].getName()));
        }

        setMapHeadData(header);

        setUp(urls, 0, null, header);
    }

    @Override
    protected void startButtonLogic() {
        if (mVideoAllCallBack != null && (mCurrentState == CURRENT_STATE_NORMAL
                || mCurrentState == CURRENT_STATE_AUTO_COMPLETE)) {
            Debuger.printfLog("onClickStartIcon");
            mVideoAllCallBack.onClickStartIcon(mOriginUrl, mTitle, this);
        } else if (mVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartError");
            mVideoAllCallBack.onClickStartError(mOriginUrl, mTitle, this);
        }
        startPlayLogic();
    }



    @Override
    public void startPlayLogic() {
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartThumb");
            mVideoAllCallBack.onClickStartThumb(mOriginUrl, mTitle, this);
        }
       // boolean hasPrepare = getGSYVideoManager().listener()
       // if(!hasPrepare)
            super.prepareVideo();
       // else
      //  prepareDatasources();

       // mHadPrepared=true;
        getGSYVideoManager().start();;


        startDismissControlViewTimer();
    }

    private boolean playing;

    public boolean isPlaying() {

        updateHandler.post(() -> {
            playing = this.getCurrentState()==CURRENT_STATE_PLAYING;
        });
        return playing;

    }
    private long seekTime;
    @Override
    public void seekTo(long time) {
        seekTime = time;
        updateHandler.postDelayed(() -> {
            super.seekTo(seekTime);
        },300);

    }
    private Handler updateHandler = new Handler(Looper.getMainLooper());
    private long currentPosition;

    private long duration;
    public long getDuration(){
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

    public void onPause() {
        updateHandler.post(() -> {
            super.onVideoPause();

            // super.pauseLogic(null, true);
        });
    }

    public void start() {
       onResume();
    }

    public void onResume() {
        updateHandler.post(() -> {
            onVideoResume();
           getGSYVideoManager().start();
        });
    }

    public boolean onKeyDown(int keyCode) {
        return false;
    }
}
