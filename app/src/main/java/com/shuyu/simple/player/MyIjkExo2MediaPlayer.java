package com.shuyu.simple.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import tv.danmaku.ijk.media.exo2.IjkExo2MediaPlayer;
import tv.danmaku.ijk.media.exo2.demo.EventLogger;

public class MyIjkExo2MediaPlayer extends IjkExo2MediaPlayer {
    public MyIjkExo2MediaPlayer(Context context) {
        super(context);

        if (this.mTrackSelector == null) {
            this.mTrackSelector = new DefaultTrackSelector(this.mAppContext);
        }

        this.mEventLogger = new EventLogger(this.mTrackSelector);
        boolean preferExtensionDecoders = true;
        boolean useExtensionRenderers = true;
        int extensionRendererMode = useExtensionRenderers ? (preferExtensionDecoders ? 2 : 1) : 0;
        if (this.mRendererFactory == null) {
            this.mRendererFactory = new DefaultRenderersFactory(this.mAppContext);
            this.mRendererFactory.setExtensionRendererMode(extensionRendererMode);
        }

        if (this.mLoadControl == null) {
            this.mLoadControl = new DefaultLoadControl();
        }

        this.mInternalPlayer = (new ExoPlayer.Builder(this.mAppContext, this.mRendererFactory)).setLooper(Looper.myLooper()).setTrackSelector(this.mTrackSelector).setLoadControl(this.mLoadControl).build();
        this.mInternalPlayer.addListener(this);
        this.mInternalPlayer.addAnalyticsListener(this);
        this.mInternalPlayer.addListener(this.mEventLogger);
        if (this.mSpeedPlaybackParameters != null) {
            this.mInternalPlayer.setPlaybackParameters(this.mSpeedPlaybackParameters);
        }

        if (this.isLooping) {
            this.mInternalPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
        }

        if (this.mSurface != null) {
            this.mInternalPlayer.setVideoSurface(this.mSurface);
        }

    }

    public void prepareAsync() throws IllegalStateException {
        this.prepareAsyncInternal();
    }
    public void setMediaSource(MediaSource mediaSource) {
        super.setMediaSource(mediaSource);
        this.mInternalPlayer.setMediaSource(mediaSource);
    }
    protected void prepareAsyncInternal() {

        //this.mInternalPlayer.setMediaSource(this.mMediaSource);
        this.mInternalPlayer.prepare();
        //this.mInternalPlayer.setPlayWhenReady(false);
    }
}
