package com.usbtv.demo.exo;

import static com.shuyu.gsyvideoplayer.utils.CommonUtil.hideNavKey;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.shuyu.gsyvideoplayer.GSYVideoBaseManager;
import com.shuyu.gsyvideoplayer.player.BasePlayerManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.io.File;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by guoshuyu on 2018/5/16.
 * 自定义管理器，连接自定义exo view 和 exo player，实现无缝切换效果
 */
public class MyExo2VideoManager extends GSYVideoBaseManager{
    public static final int SMALL_ID = com.shuyu.gsyvideoplayer.R.id.small_id;

    public static final int FULLSCREEN_ID = com.shuyu.gsyvideoplayer.R.id.full_id;

    public static String TAG = "GSYExoVideoManager";

    @SuppressLint("StaticFieldLeak")
    private static MyExo2VideoManager videoManager;


    private MyExo2VideoManager() {
        init();
    }

    /**
     * 单例管理器
     */
    public static synchronized MyExo2VideoManager instance() {
        if (videoManager == null) {
            videoManager = new MyExo2VideoManager();
        }
        return videoManager;
    }

    //@Override
    /*protected IPlayerManager getPlayManager() {
        return new GSYExoPlayerManager();
    }*/

    public void prepare(List<String> urls, Map<String, String> mapHeadData, int index,  boolean loop, float speed, boolean cache, File cachePath, String overrideExtension) {
        if (urls.size() == 0) return;
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        msg.obj = new GSYExoModel(urls, mapHeadData, index, loop, speed, cache, cachePath, overrideExtension);
        sendMessage(msg);
    }


    /**
     * 上一集
     */
    public void previous() {
        if (playerManager == null) {
            return;
        }
        ((MyExo2PlayerManager)playerManager).previous();
    }

    /**
     * 下一集
     * @return
     */
    public boolean next() {
        if (playerManager == null) {
            return false;
        }
        if(playerManager instanceof  MyExo2PlayerManager)
        return ((MyExo2PlayerManager)playerManager).next();
        return false;
    }

    /**
     * 退出全屏，主要用于返回键
     *
     * @return 返回是否全屏
     */
    @SuppressWarnings("ResourceType")
    public static boolean backFromWindowFull(Context context) {
        boolean backFrom = false;
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(context)).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        if (oldF != null) {
            backFrom = true;
            hideNavKey(context);
            if (MyExo2VideoManager.instance().lastListener() != null) {
                MyExo2VideoManager.instance().lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        if (MyExo2VideoManager.instance().listener() != null) {
            MyExo2VideoManager.instance().listener().onCompletion();
        }
        MyExo2VideoManager.instance().releaseMediaPlayer();
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                cancelTimeOutBuffer();
                if (listener() != null) {
                    listener().onCompletion();
                }
            }
        });
    }
    /**
     * 暂停播放
     */
    public static void onPause() {
        if (MyExo2VideoManager.instance().listener() != null) {
            MyExo2VideoManager.instance().listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public static void onResume() {
        if (MyExo2VideoManager.instance().listener() != null) {
            MyExo2VideoManager.instance().listener().onVideoResume();
        }
    }


    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public static void onResume(boolean seek) {
        if (MyExo2VideoManager.instance().listener() != null) {
            MyExo2VideoManager.instance().listener().onVideoResume(seek);
        }
    }

    /**
     * 当前是否全屏状态
     *
     * @return 当前是否全屏状态， true代表是。
     */
    @SuppressWarnings("ResourceType")
    public static boolean isFullState(Activity activity) {
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(activity)).findViewById(Window.ID_ANDROID_CONTENT);
        final View full = vp.findViewById(FULLSCREEN_ID);
        GSYVideoPlayer gsyVideoPlayer = null;
        if (full != null) {
            gsyVideoPlayer = (GSYVideoPlayer) full;
        }
        return gsyVideoPlayer != null;
    }

    private MyMediaHandler myMediaHandler = new MyMediaHandler((Looper.getMainLooper()));;
    protected void sendMessage(Message message) {
        myMediaHandler.sendMessage(message);
    }

    private class MyMediaHandler extends Handler {

        MyMediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_PREPARE:
                    initVideo(msg);
                    if (needTimeOutOther) {
                        startTimeOutBuffer();
                    }
                    break;
                case HANDLER_SETDISPLAY:
                    break;
                case HANDLER_RELEASE:
                    if (playerManager != null) {
                        playerManager.release();
                    }
                    if (cacheManager != null) {
                        cacheManager.release();
                    }
                    bufferPoint = 0;
                    setNeedMute(false);
                    cancelTimeOutBuffer();
                    break;
                case HANDLER_RELEASE_SURFACE:
                    releaseSurface(msg);
                    break;
            }
        }

    }

    private void initVideo(Message msg) {
        try {
            currentVideoWidth = 0;
            currentVideoHeight = 0;

            if (playerManager != null) {
                playerManager.release();
            }
            playerManager = getPlayManager();
            cacheManager = getCacheManager();
            if (cacheManager != null) {
                cacheManager.setCacheAvailableListener(this);
            }
            if (playerManager instanceof BasePlayerManager) {
                ((BasePlayerManager) playerManager)
                        .setPlayerInitSuccessListener(mPlayerInitSuccessListener);
            }
            playerManager.initVideoPlayer(context, msg, optionModelList, cacheManager);

            setNeedMute(needMute);
            IMediaPlayer mediaPlayer = playerManager.getMediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseSurface(Message msg) {
        if (msg.obj != null) {
            if (playerManager != null) {
                playerManager.releaseSurface();
            }
        }
    }
}
