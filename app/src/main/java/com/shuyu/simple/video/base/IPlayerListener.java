package com.shuyu.simple.video.base;

import android.content.Context;
import android.view.Surface;

import com.shuyu.simple.cache.ICacheManager;
import com.shuyu.simple.listener.GSYMediaPlayerListener;
import com.shuyu.simple.player.IPlayer;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Manager 与 View之间的接口
 * Created by guoshuyu on 2018/1/25.
 */

public interface IPlayerListener extends IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnInfoListener{


}
