package com.usbtv.demo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.media3.common.Player;
import androidx.media3.common.text.Cue;
import androidx.media3.common.text.CueGroup;
import androidx.media3.ui.SubtitleView;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.exo.MyExo2ListPlayerView;
import com.usbtv.demo.exo.MyExo2MediaPlayer;
import com.usbtv.demo.exo.MyExo2PlayerManager;
import com.usbtv.demo.exo.MyExo2VideoManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class GsyTvVideoView extends MyExo2ListPlayerView implements Player.Listener {
    private boolean releaseCompleted;

    public GsyTvVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PlayerFactory.setPlayManager(Exo2PlayerManager.class);
       // PlayerFactory.setPlayManager(IjkPlayerManager.class);
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
        mSubtitleView.setVisibility(VISIBLE);

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

    int cueCount = 0;

    @Override
    public void onCues(CueGroup cueGroup) {
       // if(true) return;
        if (mSubtitleView != null) {
            mSubtitleView.setCues(cueGroup.cues);
            if(false)
            for (Cue cue : cueGroup.cues) {
                if (cue.text != null &&
                        !"".equals(cue.text.toString().trim()) &&
                        isAcronym(cue.text.toString())) {
                    cueCount++;
                } else cueCount = 0;
            }
            if (cueCount > 3)
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
        if (getGSYVideoManager().getPlayer().getMediaPlayer() instanceof MyExo2MediaPlayer) {
            ((MyExo2MediaPlayer) (getGSYVideoManager().getPlayer().getMediaPlayer())).addCutesListener(this);
        }
        mSubtitleView.setVisibility(GONE);

    }


    @Override
    public void onAutoCompletion() {


        PlayerController.getInstance().incPlayCount();
        PlayerController.getInstance().setCurIndex(PlayerController.getInstance().getCurIndex() + 1);

        if (getGSYVideoManager() instanceof MyExo2VideoManager) {
            mPlayPosition++;
        } else {
            playNext();
        }
    }

    @Override
    public void onCompletion() {
        super.onCompletion();
        if (!releaseCompleted) PlayerController.getInstance().next();
        else releaseCompleted = false;
    }

    @Override
    public void onError(int what, int extra) {
        //super.onError(what, extra);
        Toast.makeText(App.getInstance().getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

        if (mPlayPosition < mUriList.size() - 1) {
            playNext();
        } else PlayerController.getInstance().playNextFolder();
    }

    @Override
    public GSYVideoViewBridge getGSYVideoManager() {
        if (PlayerFactory.getPlayManager() instanceof MyExo2PlayerManager) {
            MyExo2VideoManager.instance().initContext(getContext().getApplicationContext());
            return MyExo2VideoManager.instance();
        } else {
            GSYVideoManager.instance().initContext(getContext().getApplicationContext());
            return GSYVideoManager.instance();
        }

    }


    public void setVideoURI(Uri videoUrl, VFile res, int fi) {

        mPauseBeforePrepared = false;
        getGSYVideoManager().pause();
        if (res.getFolder().getTypeId() >= 500 && res.getFolder().getTypeId() < 600) {
            PlayerFactory.setPlayManager(IjkPlayerManager.class);
        } else {
            PlayerFactory.setPlayManager(MyExo2PlayerManager.class);
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
            String url = null;

            if(files[i].getCc()!=null){
                url = SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl.m3u8?id=" + files[i].getId();
                try {
                    if(PlayerController.getInstance().isSubTitleActive())
                    url = SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl.m3u8?id=" + files[i].getId()+"&subtitle="+URLEncoder.encode(files[i].getCc(), "UTF-8");
                   if(PlayerController.getInstance().isSubTitleProxyActive())
                    url = SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl.m3u8?id=" + files[i].getId() + "&subtitle=" + URLEncoder.encode(
                            SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/subtitle?id=" + files[i].getId() + "&url=" +
                                    URLEncoder.encode(files[i].getCc(), "UTF-8"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }else if (res.getFolder().getTypeId() >= 500 && res.getFolder().getTypeId() < 600) {
                url = SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl.m3u8?id=" + files[i].getId();
            } else if (files[i].getdLink() != null) {
                url = App.getUrl(files[i].getdLink());
            } else {
                url = SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl.mp4?id=" + files[i].getId();
            }


            urls.add(new GSYVideoModel(url, files[i].getName() + "(" + files[i].getPage() + ")"));

            if(!PlayerController.getInstance().isSeamless()){
                break;
            }
            break;
        }

        setMapHeadData(header);

        setUp(urls, 0, null, header);
    }

    @Override
    protected void startButtonLogic() {
        if (mVideoAllCallBack != null && (mCurrentState == CURRENT_STATE_NORMAL
                || mCurrentState == CURRENT_STATE_AUTO_COMPLETE)) {
            mVideoAllCallBack.onClickStartIcon(mOriginUrl, mTitle, this);
        } else if (mVideoAllCallBack != null) {
            mVideoAllCallBack.onClickStartError(mOriginUrl, mTitle, this);
        }
        startPlayLogic();
    }


    @Override
    public void startPlayLogic() {
        if (mVideoAllCallBack != null) {
            mVideoAllCallBack.onClickStartThumb(mOriginUrl, mTitle, this);
        }
        super.prepareVideo();
        getGSYVideoManager().start();
        startDismissControlViewTimer();
    }

    private boolean playing;

    public boolean isPlaying() {

        updateHandler.post(() -> {
            playing = this.getCurrentState() == CURRENT_STATE_PLAYING;
        });
        return playing;

    }

    private long seekTime;

    @Override
    public void seekTo(long time) {
        seekTime = time;
        updateHandler.postDelayed(() -> {
            super.seekTo(seekTime);
        }, 300);

    }

    private Handler updateHandler = new Handler(Looper.getMainLooper());
    private long currentPosition;

    private long duration;

    public long getDuration() {
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

    public void release() {
        //super.release();
        super.releaseVideos();
    }

    public boolean onKeyDown(int keyCode) {
        return false;
    }
}
