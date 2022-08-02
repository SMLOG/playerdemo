package com.usbtv.demo.exo;

import static com.usbtv.demo.exo.GSYExo2MediaPlayer.POSITION_DISCONTINUITY;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by guoshuyu on 2018/5/16.
 * 自定义View支持exo的list数据，实现无缝切换效果
 * 这是一种思路，通过自定义后GSYExo2MediaPlayer内部，通过ConcatenatingMediaSource实现列表播放
 * 诸如此类，还可以实现AdsMediaSource等
 */

public class GSYExo2ListPlayerView extends ListGSYVideoPlayer {


    protected boolean mExoCache = false;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public GSYExo2ListPlayerView(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public GSYExo2ListPlayerView(Context context) {
        super(context);
    }

    public GSYExo2ListPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * 设置播放URL
     *
     * @param url      播放url
     * @param position 需要播放的位置
     * @return
     */
    public boolean setUp(List<GSYVideoModel> url, int position) {
        return setUp(url, position, null, new HashMap<String, String>());
    }

    /**
     * 设置播放URL
     *
     * @param url       播放url
     * @param position  需要播放的位置
     * @param cachePath 缓存路径，如果是M3U8或者HLS，请设置为false
     * @return
     */
    public boolean setUp(List<GSYVideoModel> url, int position, File cachePath) {
        return setUp(url, position, cachePath, new HashMap<String, String>());
    }

    /**
     * 设置播放URL
     *
     * @param url         播放url
     * @param position    需要播放的位置
     * @param cachePath   缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData http header
     * @return
     */
    public boolean setUp(List<GSYVideoModel> url, int position, File cachePath, Map<String, String> mapHeadData) {
        return setUp(url, position, cachePath, mapHeadData, true);
    }

    /**
     * 设置播放URL
     *
     * @param url         播放url
     * @param position    需要播放的位置
     * @param cachePath   缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData http header
     * @param changeState 切换的时候释放surface
     * @return
     */
    protected boolean setUp(List<GSYVideoModel> url, int position, File cachePath, Map<String, String> mapHeadData, boolean changeState) {
        mUriList = url;
        mPlayPosition = position;
        mMapHeadData = mapHeadData;
        GSYVideoModel gsyVideoModel = url.get(position);

        //不支持边播边缓存
        boolean set = setUp(gsyVideoModel.getUrl(), false, cachePath, gsyVideoModel.getTitle(), changeState);
        if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
            mTitleTextView.setText(gsyVideoModel.getTitle());
        }
        return set;
    }

    @Override
    protected void cloneParams(GSYBaseVideoPlayer from, GSYBaseVideoPlayer to) {
        super.cloneParams(from, to);
        GSYExo2ListPlayerView sf = (GSYExo2ListPlayerView) from;
        GSYExo2ListPlayerView st = (GSYExo2ListPlayerView) to;
        st.mPlayPosition = sf.mPlayPosition;
        st.mUriList = sf.mUriList;
        st.mExoCache = sf.mExoCache;
        st.mTitle = sf.mTitle;
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        if (gsyBaseVideoPlayer != null) {
            GSYExo2ListPlayerView GSYExo2PlayerView = (GSYExo2ListPlayerView) gsyBaseVideoPlayer;
            GSYVideoModel gsyVideoModel = mUriList.get(mPlayPosition);
            if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
                GSYExo2PlayerView.mTitleTextView.setText(gsyVideoModel.getTitle());
            }
        }
        return gsyBaseVideoPlayer;
    }

    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        if (gsyVideoPlayer != null) {
            GSYVideoModel gsyVideoModel = mUriList.get(mPlayPosition);
            if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
                mTitleTextView.setText(gsyVideoModel.getTitle());
            }
        }
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
    }


    @Override
    protected void startPrepare() {
        if (getGSYVideoManager().listener() != null) {
            getGSYVideoManager().listener().onCompletion();
        }
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onStartPrepared");
            mVideoAllCallBack.onStartPrepared(mOriginUrl, mTitle, this);
        }
        getGSYVideoManager().setListener(this);
        getGSYVideoManager().setPlayTag(mPlayTag);
        getGSYVideoManager().setPlayPosition(mPlayPosition);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        try {
            ((Activity) getActivityContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {
            e.printStackTrace();
        }
        prepareDatasources();
    }

    public void prepareDatasources() {
        mBackUpPlayingBufferState = -1;

        //prepare通过list初始化
        List<String> urls = new ArrayList<>();
        for (GSYVideoModel gsyVideoModel : mUriList) {
            urls.add(gsyVideoModel.getUrl());
        }

        if (urls.size() == 0) {
            Debuger.printfError("********************** urls isEmpty . Do you know why ? **********************");
        }

        GSYVideoViewBridge videomManager = getGSYVideoManager();
        if(videomManager instanceof MyExo2VideoManager)
            ((MyExo2VideoManager) getGSYVideoManager()).prepare(urls, (mMapHeadData == null) ? new HashMap<String, String>() : mMapHeadData, mPlayPosition, mLooping, mSpeed, mExoCache, mCachePath, mOverrideExtension);
        else if(urls.size()>0) getGSYVideoManager().prepare(urls.get(mPlayPosition),
                (mMapHeadData == null) ? new HashMap<String, String>() : mMapHeadData,
                mLooping,
                mSpeed,
                mExoCache,
                mCachePath,
                mOverrideExtension);
//    void prepare(final String url, final Map<String, String> mapHeadData, boolean loop, float speed, boolean cache, File cachePath, String overrideExtension);
        setStateAndUi(CURRENT_STATE_PREPAREING);
    }

    /**
     * 设置播放显示状态
     *
     * @param state
     */
    @Override
    protected void setStateAndUi(int state) {
        mCurrentState = state;
        if ((state == CURRENT_STATE_NORMAL && isCurrentMediaListener())
                || state == CURRENT_STATE_AUTO_COMPLETE || state == CURRENT_STATE_ERROR) {
            mHadPrepared = false;
        }

        switch (mCurrentState) {
            case CURRENT_STATE_NORMAL:
                if (isCurrentMediaListener()) {
                    Debuger.printfLog(GSYExo2ListPlayerView.this.hashCode() + "------------------------------ dismiss CURRENT_STATE_NORMAL");
                    cancelProgressTimer();
                    getGSYVideoManager().releaseMediaPlayer();
                    releasePauseCover();
                    mBufferPoint = 0;
                    mSaveChangeViewTIme = 0;
                    if (mAudioManager != null) {
                        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                    }
                }
                releaseNetWorkState();
                break;
            case CURRENT_STATE_PREPAREING:
                resetProgressAndTime();
                break;
            case CURRENT_STATE_PLAYING:
                if (isCurrentMediaListener()) {
                    Debuger.printfLog(GSYExo2ListPlayerView.this.hashCode() + "------------------------------ CURRENT_STATE_PLAYING");
                    startProgressTimer();
                }
                break;
            case CURRENT_STATE_PAUSE:
                Debuger.printfLog(GSYExo2ListPlayerView.this.hashCode() + "------------------------------ CURRENT_STATE_PAUSE");
                startProgressTimer();
                break;
            case CURRENT_STATE_ERROR:
               /* if (isCurrentMediaListener()) {
                    getGSYVideoManager().releaseMediaPlayer();
                }*/
                break;
            case CURRENT_STATE_AUTO_COMPLETE:
                Debuger.printfLog(GSYExo2ListPlayerView.this.hashCode() + "------------------------------ dismiss CURRENT_STATE_AUTO_COMPLETE");
                cancelProgressTimer();
                if (mProgressBar != null) {
                    mProgressBar.setProgress(100);
                }
                if (mCurrentTimeTextView != null && mTotalTimeTextView != null) {
                    mCurrentTimeTextView.setText(mTotalTimeTextView.getText());
                }
                if (mBottomProgressBar != null) {
                    mBottomProgressBar.setProgress(100);
                }
                break;
        }
        resolveUIState(state);
        if (mGsyStateUiListener != null) {
            mGsyStateUiListener.onStateChanged(state);
        }
    }

    public void setExoCache(boolean exoCache) {
        this.mExoCache = exoCache;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            if (isIfCurrentIsFullscreen()) {
                Debuger.printfLog("onClickSeekbarFullscreen");
                mVideoAllCallBack.onClickSeekbarFullscreen(mOriginUrl, mTitle, this);
            } else {
                Debuger.printfLog("onClickSeekbar");
                mVideoAllCallBack.onClickSeekbar(mOriginUrl, mTitle, this);
            }
        }
        if (getGSYVideoManager() != null && mHadPlay) {
            /**增加这个可以实现拖动后重新播放*/
            if(!isInPlayingState()) {
                setStateAndUi(CURRENT_STATE_PLAYING);
                addTextureView();
            }
            try {
                long time = seekBar.getProgress() * getDuration() / 100;
                getGSYVideoManager().seekTo(time);
            } catch (Exception e) {
                Debuger.printfWarning(e.toString());
            }
        }
        mHadSeekTouch = false;
    }

    @Override
    public void onAutoCompletion() {
        setStateAndUi(CURRENT_STATE_AUTO_COMPLETE);

        mSaveChangeViewTIme = 0;
        mCurrentPosition = 0;

        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (!mIfCurrentIsFullscreen)
            getGSYVideoManager().setLastListener(null);
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        if (mContext instanceof Activity) {
            try {
                ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        releaseNetWorkState();

        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
            Debuger.printfLog("onAutoComplete");
            mVideoAllCallBack.onAutoComplete(mOriginUrl, mTitle, this);
        }

        GSYVideoViewBridge videomManager = getGSYVideoManager();

        if(videomManager instanceof MyExo2VideoManager){
            //videomManager.getPlayPosition()
        }else{
            playNext();
        }
    }

    @Override
    public void onCompletion() {
        //make me normal first
        setStateAndUi(CURRENT_STATE_NORMAL);

        mSaveChangeViewTIme = 0;
        mCurrentPosition = 0;

        if (mTextureViewContainer.getChildCount() > 0) {
            mTextureViewContainer.removeAllViews();
        }

        if (!mIfCurrentIsFullscreen) {
            getGSYVideoManager().setListener(null);
            getGSYVideoManager().setLastListener(null);
        }
        getGSYVideoManager().setCurrentVideoHeight(0);
        getGSYVideoManager().setCurrentVideoWidth(0);

        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        if (mContext instanceof Activity) {
            try {
                ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        releaseNetWorkState();


    }



    /**********以下重载GSYVideoPlayer的GSYVideoViewBridge相关实现***********/

    @Override
    public GSYVideoViewBridge getGSYVideoManager() {
        MyExo2VideoManager.instance().initContext(getContext().getApplicationContext());
        return MyExo2VideoManager.instance();
    }

    @Override
    protected boolean backFromFull(Context context) {
        return MyExo2VideoManager.backFromWindowFull(context);
    }

    @Override
    protected void releaseVideos() {
        MyExo2VideoManager.releaseAllVideos();
    }

    @Override
    protected int getFullId() {
        return MyExo2VideoManager.FULLSCREEN_ID;
    }

    @Override
    protected int getSmallId() {
        return MyExo2VideoManager.SMALL_ID;
    }

    @Override
    public void onInfo(int what, int extra) {
        if (what == POSITION_DISCONTINUITY) {
            int window = ((GSYExo2MediaPlayer) getGSYVideoManager().getPlayer().getMediaPlayer()).getCurrentWindowIndex();
            mPlayPosition = window;
            GSYVideoModel gsyVideoModel = mUriList.get(window);
            if (!TextUtils.isEmpty(gsyVideoModel.getTitle())) {
                mTitleTextView.setText(gsyVideoModel.getTitle());
            }
        } else {
            super.onInfo(what, extra);
        }
    }

    @Override
    public boolean playNext() {
        GSYVideoViewBridge videomManager = getGSYVideoManager();

        if(videomManager instanceof MyExo2VideoManager){
            return  ((MyExo2VideoManager) getGSYVideoManager()).next();
        }
        return super.playNext();
    }

    public void nextUI() {
        resetProgressAndTime();
    }
}