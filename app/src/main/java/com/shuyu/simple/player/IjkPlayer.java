package com.shuyu.simple.player;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.shuyu.simple.model.GSYVideoModel;
import com.shuyu.simple.model.VideoOptionModel;
import com.shuyu.simple.utils.Debuger;
import com.shuyu.simple.utils.GSYVideoType;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;

public class IjkPlayer extends BasePlayer {

    /**
     * log level
     */
    private static int logLevel = IjkMediaPlayer.IJK_LOG_DEFAULT;

    private static IjkLibLoader ijkLibLoader;

    private IjkMediaPlayer mediaPlayer;

    private List<VideoOptionModel> optionModelList;

    private Surface surface;
    private Context context;

    public IjkPlayer(Context context){

        this.context = context;
        this.init(context);
    }

    private void init(Context context) {
        mediaPlayer = (ijkLibLoader == null) ? new IjkMediaPlayer() : new IjkMediaPlayer(ijkLibLoader);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnNativeInvokeListener(new IjkMediaPlayer.OnNativeInvokeListener() {
            @Override
            public boolean onNativeInvoke(int i, Bundle bundle) {
                return true;
            }
        });



            //开启硬解码
            if (GSYVideoType.isMediaCodec()) {
                Debuger.printfLog("enable mediaCodec");
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            }



            mediaPlayer.native_setLogLevel(logLevel);
            initIJKOption(mediaPlayer, optionModelList);


    }

    @Override
    public void showDisplay(Message msg) {
        if (msg.obj == null && mediaPlayer != null) {
            mediaPlayer.setSurface(null);
        } else {
            Surface holder = (Surface) msg.obj;
            surface = holder;
            if (mediaPlayer != null && holder.isValid()) {
                mediaPlayer.setSurface(holder);
            }
        }
    }

    @Override
    public void setSpeed(float speed, boolean soundTouch) {
        if (speed > 0) {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.setSpeed(speed);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (soundTouch) {
                VideoOptionModel videoOptionModel =
                    new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
                List<VideoOptionModel> list = getOptionModelList();
                if (list != null) {
                    list.add(videoOptionModel);
                } else {
                    list = new ArrayList<>();
                    list.add(videoOptionModel);
                }
                setOptionModelList(list);
            }

        }
    }

    @Override
    public void setNeedMute(boolean needMute) {
        if (mediaPlayer != null) {
            if (needMute) {
                mediaPlayer.setVolume(0, 0);
            } else {
                mediaPlayer.setVolume(1, 1);
            }
        }
    }

    @Override
    public void setVolume(float left, float right) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(left, right);
        }
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    @Override
    public MediaInfo getMediaInfo() {
        return mediaPlayer.getMediaInfo();
    }

    @Override
    public void setLogEnabled(boolean enable) {
        mediaPlayer.setLogEnabled(enable);
    }

    @Override
    public boolean isPlayable() {
        return mediaPlayer.isPlayable();
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mediaPlayer.setOnPreparedListener(listener);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mediaPlayer.setOnCompletionListener(listener);
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mediaPlayer.setOnBufferingUpdateListener(listener);
    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mediaPlayer.setOnSeekCompleteListener(listener);
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mediaPlayer.setOnVideoSizeChangedListener(listener);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mediaPlayer.setOnErrorListener(listener);
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        mediaPlayer.setOnInfoListener(listener);
    }

    @Override
    public void setOnTimedTextListener(OnTimedTextListener listener) {
        mediaPlayer.setOnTimedTextListener(listener);
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        mediaPlayer.setAudioStreamType(streamtype);
    }

    @Override
    public void setKeepInBackground(boolean keepInBackground) {
        mediaPlayer.setKeepInBackground(keepInBackground);
    }

    @Override
    public void releaseSurface() {
        if (surface != null) {
            //surface.release();
            surface = null;
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void reset() {
        mediaPlayer.reset();
    }

    @Override
    public int getBufferedPercentage() {
        return -1;
    }

    @Override
    public long getNetSpeed() {
        if (mediaPlayer != null) {
            return mediaPlayer.getTcpSpeed();
        }
        return 0;
    }

    @Override
    public void setSpeedPlaying(float speed, boolean soundTouch) {
        if (mediaPlayer != null) {
            mediaPlayer.setSpeed(speed);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", (soundTouch) ? 1 : 0);
        }
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        mediaPlayer.setDisplay(sh);
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mediaPlayer.setDataSource(context,uri);
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mediaPlayer.setDataSource(context,uri,headers);
    }

    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        mediaPlayer.setDataSource(fd);
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mediaPlayer.setDataSource(path);
    }

    @Override
    public String getDataSource() {
        return mediaPlayer.getDataSource();
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mediaPlayer.prepareAsync();
    }

    @Override
    public void start() {
            mediaPlayer.start();
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        mediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    @Override
    public int getVideoWidth() {
            return mediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
            return mediaPlayer.getVideoHeight();
    }

    @Override
    public boolean isPlaying() {
            return mediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
            mediaPlayer.seekTo(time);
    }

    @Override
    public long getCurrentPosition() {
            return mediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
            return mediaPlayer.getDuration();
    }

    @Override
    public int getVideoSarNum() {
        return mediaPlayer.getVideoSarNum();
    }

    @Override
    public int getVideoSarDen() {
        return mediaPlayer.getVideoSarDen();
    }


    @Override
    public void setMediaItems(List<GSYVideoModel> urls, int i) {

        this.mediaItems = urls;
        if(i>=0 && i<this.mediaItems.size()){
            this.curMediaIndex = i;
        }else this.curMediaIndex = 0 ;

        playIndex(this.curMediaIndex);

    }

    private void playIndex(int i){

        GSYVideoModel item = this.mediaItems.get(i);
        try {
            setDataSource(context,Uri.parse(item.getUrl()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.curMediaIndex = i;
    }
    @Override
    public boolean next() {
        if(curMediaIndex < this.getMediaItemCount()-1){
            playIndex(curMediaIndex+1);
            return true;
        }
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
        return this.mediaItems.size();
    }



    @Override
    public int getCurrentVideoWidth() {
        return this.mediaPlayer.getVideoWidth();
    }

    @Override
    public int getCurrentVideoHeight() {
        return this.mediaPlayer.getVideoHeight();
    }

    @Override
    public void setDisplay(Surface surface) {

        this.mediaPlayer.setSurface(surface);
    }

    @Override
    public void releaseSurface(Surface surface) {
        mediaPlayer.setSurface(null);
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
    public void setWakeMode(Context context, int mode) {
        mediaPlayer.setWakeMode(context,mode);
    }

    @Override
    public void setLooping(boolean looping) {
        mediaPlayer.setLooping(looping);
    }

    @Override
    public boolean isLooping() {
        return mediaPlayer.isLooping();
    }


    @Override
    public boolean isSurfaceSupportLockCanvas() {
        return true;
    }


    public IjkTrackInfo[] getTrackInfo() {
        if (mediaPlayer != null) {
            return mediaPlayer.getTrackInfo();
        }
        return null;
    }

    @Override
    public void setSurface(Surface surface) {
        mediaPlayer.setSurface(surface);
    }

    @Override
    public void setDataSource(IMediaDataSource mediaDataSource) {
        mediaPlayer.setDataSource(mediaDataSource);
    }

    public int getSelectedTrack(int trackType) {
        if (mediaPlayer != null) {
            return mediaPlayer.getSelectedTrack(trackType);
        }
        return -1;
    }

    public void selectTrack(int track) {
        if (mediaPlayer != null) {
            mediaPlayer.selectTrack(track);
        }
    }

    public void deselectTrack(int track) {
        if (mediaPlayer != null) {
            mediaPlayer.deselectTrack(track);
        }
    }

    private void initIJKOption(IjkMediaPlayer ijkMediaPlayer, List<VideoOptionModel> optionModelList) {
        if (optionModelList != null && optionModelList.size() > 0) {
            for (VideoOptionModel videoOptionModel : optionModelList) {
                if (videoOptionModel.getValueType() == VideoOptionModel.VALUE_TYPE_INT) {
                    ijkMediaPlayer.setOption(videoOptionModel.getCategory(),
                        videoOptionModel.getName(), videoOptionModel.getValueInt());
                } else {
                    ijkMediaPlayer.setOption(videoOptionModel.getCategory(),
                        videoOptionModel.getName(), videoOptionModel.getValueString());
                }
            }
        }
    }

    public List<VideoOptionModel> getOptionModelList() {
        return optionModelList;
    }

    public void setOptionModelList(List<VideoOptionModel> optionModelList) {
        this.optionModelList = optionModelList;
    }

    public static IjkLibLoader getIjkLibLoader() {
        return ijkLibLoader;
    }

    public static void setIjkLibLoader(IjkLibLoader ijkLibLoader) {
        IjkPlayer.ijkLibLoader = ijkLibLoader;
    }

    public static int getLogLevel() {
        return logLevel;
    }

    public static void setLogLevel(int logLevel) {
        IjkPlayer.logLevel = logLevel;
    }
}
