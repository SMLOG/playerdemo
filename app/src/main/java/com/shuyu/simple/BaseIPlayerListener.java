package com.shuyu.simple;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.shuyu.simple.cache.ICacheManager;
import com.shuyu.simple.model.VideoOptionModel;
import com.shuyu.simple.player.IPlayer;
import com.shuyu.simple.player.IPlayerInitSuccessListener;
import com.shuyu.simple.cache.CacheFactory;
import com.shuyu.simple.utils.Debuger;
import com.shuyu.simple.video.base.GSYVideoView;
import com.shuyu.simple.video.base.IPlayerListener;

import java.io.File;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 基类管理器
 GSYVideoViewBridge接口说明可以查阅GSYVideoViewBridge类
 Created by guoshuyu on 2018/1/25.
 */

public abstract class BaseIPlayerListener implements IPlayerListener {

    public static String TAG = "GSYVideoBaseManager";

    protected static final int HANDLER_PREPARE = 0;

    protected static final int HANDLER_SETDISPLAY = 1;

    protected static final int HANDLER_RELEASE = 2;

    protected static final int HANDLER_RELEASE_SURFACE = 3;

    protected static final int BUFFER_TIME_OUT_ERROR = -192;//外部超时错误码

    protected Context context;


    protected Handler mainThreadHandler;

    protected GSYVideoView videoView;



    protected IPlayerInitSuccessListener mPlayerInitSuccessListener;



    /**
     配置ijk option
     */
    protected List<VideoOptionModel> optionModelList;

    /**
     播放的tag，防止错位置，因为普通的url也可能重复
     */
    protected String playTag = "";

    /**
     缓存管理
     */
    protected ICacheManager cacheManager;

    /**
     当前播放的视频宽的高
     */
    protected int currentVideoWidth = 0;

    /**
     当前播放的视屏的高
     */
    protected int currentVideoHeight = 0;

    /**
     当前视频的最后状态
     */
    protected int lastState;

    /**
     播放的tag，防止错位置，因为普通的url也可能重复
     */
    protected int playPosition = -22;

    /**
     缓冲比例
     */
    protected int bufferPoint;

    /**
     播放超时
     */
    protected int timeOut = 8 * 1000;

    /**
     是否需要静音
     */
    protected boolean needMute = false;

    /**
     是否需要外部超时判断
     */
    protected boolean needTimeOutOther;



    protected void init(GSYVideoView videoView) {
        this.videoView = videoView;
        mainThreadHandler = new Handler();
    }


    @Override
    public void onPrepared(IMediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelTimeOutBuffer();
                videoView.onPrepared();

            }
        });
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        if(!player.next()){
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    cancelTimeOutBuffer();
                    videoView.onAutoCompletion();

                }
            });
        }else{
            videoView.onPlayNext();
        }

    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, final int percent) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (percent > bufferPoint) {
                    videoView.onBufferingUpdate(percent);
                } else {
                    videoView.onBufferingUpdate(bufferPoint);
                }
            }
        });
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelTimeOutBuffer();
                videoView.onSeekComplete();

            }
        });
    }

    @Override
    public boolean onError(IMediaPlayer mp, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelTimeOutBuffer();
                videoView.onError(what, extra);

            }
        });
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (needTimeOutOther) {
                    if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        startTimeOutBuffer();
                    } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        cancelTimeOutBuffer();
                    }
                }
                videoView.onInfo(what, extra);

            }
        });
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        currentVideoWidth = mp.getVideoWidth();
        currentVideoHeight = mp.getVideoHeight();
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                videoView.onVideoSizeChanged();

            }
        });
    }


    /**
     启动十秒的定时器进行 缓存操作
     */
    protected void startTimeOutBuffer() {
        // 启动定时
        com.shuyu.simple.utils.Debuger.printfError("startTimeOutBuffer");
        mainThreadHandler.postDelayed(mTimeOutRunnable, timeOut);

    }

    /**
     取消 十秒的定时器进行 缓存操作
     */
    protected void cancelTimeOutBuffer() {
        com.shuyu.simple.utils.Debuger.printfError("cancelTimeOutBuffer");
        // 取消定时
        if (needTimeOutOther)
            mainThreadHandler.removeCallbacks(mTimeOutRunnable);
    }


    private Runnable mTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Debuger.printfError("time out for error listener");
            videoView.onError(BUFFER_TIME_OUT_ERROR, BUFFER_TIME_OUT_ERROR);
        }
    };




    public void initContext(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     打开raw播放支持

     @param context
     */
    public void enableRawPlay(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<VideoOptionModel> getOptionModelList() {
        return optionModelList;
    }

    /**
     设置IJK视频的option
     */
    public void setOptionModelList(List<VideoOptionModel> optionModelList) {
        this.optionModelList = optionModelList;
    }

    public boolean isNeedMute() {
        return needMute;
    }



    public int getTimeOut() {
        return timeOut;
    }

    public boolean isNeedTimeOutOther() {
        return needTimeOutOther;
    }




    /**
     播放器初始化后接口
     */
    public void setPlayerInitSuccessListener(IPlayerInitSuccessListener listener) {
        this.mPlayerInitSuccessListener = listener;
    }

    protected IPlayer player;
    public  void setIPlayer(IPlayer player){
        if(this.player!=null){
            player.setOnCompletionListener(null);
            player.setOnBufferingUpdateListener(null);
            player.setScreenOnWhilePlaying(false);
            player.setOnPreparedListener(null);
            player.setOnSeekCompleteListener(null);
            player.setOnErrorListener(null);
            player.setOnInfoListener(null);
            player.setOnVideoSizeChangedListener(null);
        }

        player.setOnCompletionListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setScreenOnWhilePlaying(true);
        player.setOnPreparedListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnErrorListener(this);
        player.setOnInfoListener(this);
        player.setOnVideoSizeChangedListener(this);
    }
}
