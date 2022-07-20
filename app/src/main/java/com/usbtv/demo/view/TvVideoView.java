package com.usbtv.demo.view;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.usbtv.demo.MainActivity;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.PlayerController;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.data.VFile;

import java.util.Timer;
import java.util.TimerTask;

public class TvVideoView extends StyledPlayerView {
    private SimpleExoPlayer mPlayer;
    private boolean isPlaying;
    private Handler updateHandler = new Handler(Looper.getMainLooper());
    private long duration;
    private long currentPosition;

    public TvVideoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mPlayer = ExoPlayerFactory.newSimpleInstance(App.getInstance().getApplicationContext());
        this.setPlayer(this.mPlayer);
        setShowNextButton(false);
        setShowPreviousButton(false);
        setShowFastForwardButton(false);
        setShowRewindButton(false);
        setShowShuffleButton(false);
        setControllerShowTimeoutMs(3000);
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Player.EventListener.super.onPlayerError(error);
                Toast.makeText(App.getInstance().getApplicationContext(), "播放出错", Toast.LENGTH_SHORT).show();
                mPlayer.next();
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                isPlaying=false;

                switch (state) {
                    case Player.STATE_READY:
                        update();
                        break;
                    case Player.STATE_ENDED: {
                        PlayerController.getInstance().next();
                        break;
                    }
                }
            }
        });


    }

    public void update(){
        Runnable updateRun = new Runnable() {
            @Override
            public void run() {
                TvVideoView.this.isPlaying = TvVideoView.this.mPlayer.isPlaying();
                TvVideoView.this.duration = TvVideoView.this.mPlayer.getDuration();
                TvVideoView.this.currentPosition = TvVideoView.this.mPlayer.getCurrentPosition();

                if(!TvVideoView.this.mPlayer.isPlaying()){
                    updateHandler.removeCallbacks(this);
                    return;
                }

                updateHandler.postDelayed(this,5000);
            };
        };
        updateHandler.postDelayed(updateRun
       ,5000);

    }

    public void setUp(MainActivity mainActivity) {
    }

    public boolean onKeyDown(int keyCode) {
        return false;
    }

    public long getDuration() {
        return duration;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public boolean isPlaying() {

        return isPlaying;
    }

    public void seekTo(int pos) {
        if (mPlayer != null) mPlayer.seekTo(pos);
    }

    public void pause() {
        if (mPlayer != null) mPlayer.setPlayWhenReady(false);
    }

    public void start() {
        if (mPlayer != null) mPlayer.setPlayWhenReady(true);
        mPlayer.play();


    }


    public void resume() {
        this.start();
    }

    private VFile[] files;

    public void setVideoURI(Uri uri, VFile res,int curIndex) {
        //mPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);

        files = res.getFolder().getFiles().toArray(new VFile[]{});



        MediaItem item = MediaItem.fromUri(uri);

        int c = mPlayer.getMediaItemCount();
        mPlayer.addMediaItem(item);

        for (int i = curIndex + 1; i < files.length; i++) {
            if (files[i].getdLink() != null) {
                item = MediaItem.fromUri(files[i].getdLink());
                mPlayer.addMediaItem(item);
            }else break;
        }
        int state = mPlayer.getPlaybackState();
        if (state == Player.STATE_ENDED || mPlayer.getPlayWhenReady()) {
            mPlayer.next();
            mPlayer.seekTo(mPlayer.getCurrentWindowIndex(), C.TIME_UNSET);
           for(int j=0;j<c;j++) mPlayer.removeMediaItem(j);

        } else {
            mPlayer.setPlayWhenReady(true);
            mPlayer.prepare();
        }



    }



    public void release() {
        mPlayer.release();
    }
}
