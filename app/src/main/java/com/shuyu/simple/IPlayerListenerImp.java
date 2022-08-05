package com.shuyu.simple;


import android.annotation.SuppressLint;

import com.shuyu.simple.video.base.GSYVideoView;


/**
 * 视频管理，单例
 * Created by shuyu on 2016/11/11.
 */

public class IPlayerListenerImp extends BaseIPlayerListener {

    public static final int SMALL_ID = com.shuyu.gsyvideoplayer.R.id.small_id;

    public static final int FULLSCREEN_ID = com.shuyu.gsyvideoplayer.R.id.full_id;

    public static String TAG = "GSYVideoManager";

    @SuppressLint("StaticFieldLeak")
    private static IPlayerListenerImp videoManager;


    public IPlayerListenerImp(GSYVideoView videoView) {
        super.init(videoView);
    }


}