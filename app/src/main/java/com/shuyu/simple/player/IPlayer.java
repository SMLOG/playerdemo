package com.shuyu.simple.player;

import android.content.Context;
import android.os.Message;
import android.view.Surface;

import com.shuyu.simple.model.GSYVideoModel;
import com.shuyu.simple.model.VideoOptionModel;
import com.shuyu.simple.cache.ICacheManager;
import com.shuyu.simple.video.base.GSYVideoView;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 播放器差异管理接口
 * Created by guoshuyu on 2018/1/11.
 */

public interface IPlayer extends IMediaPlayer {


    /**
     * 设置渲染显示
     */
    void showDisplay(Message msg);

    /**
     * 是否静音
     */
    void setNeedMute(boolean needMute);

    /**
     * 单独设置 setVolume ，和 setNeedMute 互斥 float 0.0 - 1.0
     */
    void setVolume(float left, float right);

    /**
     * 释放渲染
     */
    void releaseSurface();

    /**
     * 释放内核
     */
    void release();

    /**
     * 缓存进度
     */
    int getBufferedPercentage();

    /**
     * 网络速度
     */
    long getNetSpeed();

    /**
     * 播放速度
     */
    void setSpeedPlaying(float speed, boolean soundTouch);

    /**
     * Surface是否支持外部lockCanvas，来自定义暂停时的绘制画面
     * exoplayer目前不支持，因为外部lock后，切换surface会导致异常
     */
    boolean isSurfaceSupportLockCanvas();

    void setSpeed(float speed, boolean soundTouch);

    void start();

    void stop();

    void pause();

    int getVideoWidth();

    int getVideoHeight();

    boolean isPlaying();

    void seekTo(long time);

    long getCurrentPosition();

    long getDuration();

    int getVideoSarNum();

    int getVideoSarDen();

    void setMediaItems(List<GSYVideoModel> urls, int i);
    boolean next();
    boolean prev();
    int getCurPlayIndex();
    int getMediaItemCount();

    int getCurrentVideoWidth();

    int getCurrentVideoHeight();

    void setDisplay(Surface surface);

    void releaseSurface(Surface surface);

    int getLastState();

    int getRotateInfoFlag();
}
