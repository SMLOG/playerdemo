package com.usbtv.demo;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.CueGroup;
import com.google.android.exoplayer2.ui.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.exo.MyExo2ListPlayerView;
import com.usbtv.demo.exo.MyExo2MediaPlayer;
import com.usbtv.demo.exo.MyExo2PlayerManager;
import com.usbtv.demo.exo.MyExo2VideoManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.codeest.enviews.ENDownloadView;
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class GsyTvVideoView extends MyExo2ListPlayerView implements Player.Listener{
    private boolean releaseCompleted;

    public GsyTvVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PlayerFactory.setPlayManager(Exo2PlayerManager.class);
        PlayerFactory.setPlayManager(IjkPlayerManager.class);
        //PlayerFactory.setPlayManager(GSYExoPlayerManager.class);

        //  CacheFactory.setCacheManager(ProxyCacheManager.class);
        // CacheFactory.setCacheManager(ExoPlayerCacheManager.class);
        IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
        this.mDismissControlTime = 200;

    }
    private SubtitleView mSubtitleView;

    @Override
    protected void init(Context context) {
        super.init(context);
        mSubtitleView = findViewById(R.id.sub_title_view);
        mSubtitleView.setUserDefaultStyle();
        mSubtitleView.setUserDefaultTextSize();

        //mSubtitleView.setStyle(new CaptionStyleCompat(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_NONE, CaptionStyleCompat.EDGE_TYPE_NONE, null));
       // mSubtitleView.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
    }

    @Override
    protected void startDismissControlViewTimer() {
        super.startDismissControlViewTimer();
        mPostDismiss = false;
    }

    @Override
    protected void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        setViewShowState(mBottomProgressBar, GONE);
    }
    @Override
    protected void hideAllWidget() {
        super.hideAllWidget();
        setViewShowState(mBottomProgressBar, GONE);
    }
    private static boolean isAcronym(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }
    int cueCount=0;
    @Override
    public void onCues(CueGroup cueGroup) {
        if (mSubtitleView != null) {
            mSubtitleView.setCues(cueGroup.cues);
            for(Cue cue:cueGroup.cues){
               if(cue.text!=null &&
                       !"".equals(cue.text.toString().trim()) &&
                       isAcronym(cue.text.toString())){
                   cueCount++;
               }else cueCount=0;
            }
            if(cueCount>3)
            mSubtitleView.setVisibility(GONE);
            else mSubtitleView.setVisibility(VISIBLE);

        }
    }
    @Override
    public int getLayoutId() {
        return R.layout.video_layout_subtitle;
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
       if(  getGSYVideoManager().getPlayer().getMediaPlayer() instanceof MyExo2MediaPlayer){
           ((MyExo2MediaPlayer) (getGSYVideoManager().getPlayer().getMediaPlayer())).addCutesListener(this);
           mSubtitleView.setVisibility(GONE);
       }else{
           mSubtitleView.setVisibility(GONE);

       }
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
        //super.onError(what, extra);
        Toast.makeText(App.getInstance().getApplicationContext(), "播放出错", Toast.LENGTH_SHORT).show();

        if(mPlayPosition<mUriList.size()-1){
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

                    new GSYVideoModel(
                            res.getFolder().getTypeId()>=500&& res.getFolder().getTypeId()<600?
                                    SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl.m3u8?id=" + files[i].getId()  :
                            files[i].getdLink() != null ? App.getUrl(files[i].getdLink()) :

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

  public void release(){
       //super.release();
        super.releaseVideos();
  }

    public boolean onKeyDown(int keyCode) {
        return false;
    }
}
