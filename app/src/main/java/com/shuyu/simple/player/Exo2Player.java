package com.shuyu.simple.player;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.shuyu.simple.model.GSYVideoModel;

import java.io.File;
import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.ExoSourceManager;
import tv.danmaku.ijk.media.exo2.demo.EventLogger;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;


/**
 * Created by guoshuyu on 2018/1/10.
 * Exo
 */
public class Exo2Player extends BasePlayer {

    public static int ON_POSITION_DISCOUNTINUITY = 2702;

    private static final String TAG = "Exo2Player";

    protected MyIjkExo2MediaPlayer mInternalPlayer;


    public Exo2Player(Context context) {
            this.mInternalPlayer = new  MyIjkExo2MediaPlayer(context);
    }

    public Format getVideoFormat() {
        if (mInternalPlayer != null) {
            return mInternalPlayer.getVideoFormat();
        }
        return null;
    }


    @Override
    public void setDisplay(SurfaceHolder sh) {
        if (sh == null)
            setSurface(null);
        else
            setSurface(sh.getSurface());
    }

    @Override
    public void setSurface(Surface surface) {

      this.mInternalPlayer.setSurface(surface);

    }

    @Override
    public void setDataSource(IMediaDataSource mediaDataSource) {

    }


    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) {
        mInternalPlayer.setDataSource(context,uri,headers);
    }

    @Override
    public void setDataSource(String path) {
        mInternalPlayer.setDataSource(path);
    }

    @Override
    public void setDataSource(Context context, Uri uri) {
        mInternalPlayer.setDataSource(context,uri);
    }

    @Override
    public void setDataSource(FileDescriptor fd) {
        throw new UnsupportedOperationException("no support");
    }

    @Override
    public String getDataSource() {
        return mInternalPlayer.getDataSource();
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        this.mInternalPlayer.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        mInternalPlayer.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        mInternalPlayer.release();
    }

    @Override
    public void pause() throws IllegalStateException {
        mInternalPlayer.pause();
    }

    @Override
    public void setWakeMode(Context context, int mode) {
        // FIXME: implement
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        // TODO: do nothing
    }

    @Override
    public IjkTrackInfo[] getTrackInfo() {
        // TODO: implement
        return null;
    }

    @Override
    public int getVideoWidth() {
       return this.mInternalPlayer.getVideoWidth();
      //  return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return this.mInternalPlayer.getVideoHeight();
      //  return mVideoHeight;
    }

    @Override
    public boolean isPlaying() {
     return mInternalPlayer.isPlaying();
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.seekTo(msec);
    }

    @Override
    public long getCurrentPosition() {
        if (mInternalPlayer == null)
            return 0;
        return mInternalPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        if (mInternalPlayer == null)
            return 0;
        return mInternalPlayer.getDuration();
    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public void setMediaItems(List<GSYVideoModel> urls, int i) {

        this.mediaItems = urls;
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
        for (GSYVideoModel m : urls) {
            MediaSource mediaSource = mInternalPlayer.getExoHelper().getMediaSource(m.getUrl(), false, false, false, null, mInternalPlayer.getOverrideExtension());
            concatenatedSource.addMediaSource(mediaSource);
        }
        mInternalPlayer.setMediaSource(concatenatedSource);
        curMediaIndex=0;

    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public boolean prev() {
        return false;
    }

    @Override
    public int getCurPlayIndex() {
        return this.curMediaIndex;
    }

    @Override
    public int getMediaItemCount() {
        return mediaItems.size();
    }

    @Override
    public int getCurrentVideoWidth() {
        return getVideoWidth();
    }

    @Override
    public int getCurrentVideoHeight() {
        return getVideoHeight();
    }

    @Override
    public void setDisplay(Surface surface) {

        setSurface(surface);

    }

    @Override
    public void releaseSurface(Surface surface) {
            setSurface(null);
    }

    @Override
    public int getLastState() {
        return 0;
    }

    @Override
    public int getRotateInfoFlag() {
        return 0;
    }

    @Override
    public void reset() {
        mInternalPlayer.release();

    }

    @Override
    public void setLooping(boolean looping) {
        mInternalPlayer.setLooping(looping);
    }

    @Override
    public boolean isLooping() {
        return mInternalPlayer.isLooping();
    }

    @Override
    public void showDisplay(Message msg) {

    }

    @Override
    public void setNeedMute(boolean needMute) {

    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mInternalPlayer.setVolume(leftVolume,rightVolume);
    }

    @Override
    public void releaseSurface() {

    }

    @Override
    public int getAudioSessionId() {
        return mInternalPlayer.getAudioSessionId();
    }

    @Override
    public MediaInfo getMediaInfo() {
        return null;
    }

    @Override
    public void setLogEnabled(boolean enable) {
        // do nothing
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
            mInternalPlayer.setOnPreparedListener(listener);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mInternalPlayer.setOnCompletionListener(listener);
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mInternalPlayer.setOnBufferingUpdateListener(listener);
    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mInternalPlayer.setOnSeekCompleteListener(listener);
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mInternalPlayer.setOnVideoSizeChangedListener(listener);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mInternalPlayer.setOnErrorListener(listener);
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        mInternalPlayer.setOnInfoListener(listener);
    }

    @Override
    public void setOnTimedTextListener(OnTimedTextListener listener) {
        mInternalPlayer.setOnTimedTextListener(listener);
    }


    @Override
    public void setAudioStreamType(int streamtype) {
        mInternalPlayer.setAudioStreamType(streamtype);
    }

    @Override
    public void setKeepInBackground(boolean keepInBackground) {
        mInternalPlayer.setKeepInBackground(keepInBackground);
    }

    @Override
    public void release() {
        mInternalPlayer.release();;
    }

    @Override
    public int getBufferedPercentage() {
        return mInternalPlayer.getBufferedPercentage();
    }


    @Override
    public long getNetSpeed() {
        return 0;
    }

    @Override
    public void setSpeedPlaying(float speed, boolean soundTouch) {

    }

    @Override
    public boolean isSurfaceSupportLockCanvas() {
        return false;
    }

    @Override
    public void setSpeed(float speed, boolean soundTouch) {

    }


}
