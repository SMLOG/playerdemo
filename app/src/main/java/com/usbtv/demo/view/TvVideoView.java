package com.usbtv.demo.view;

import android.content.Context;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.usbtv.demo.MainActivity;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.PlayerController;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.data.VFile;

import java.util.ArrayList;
import java.util.List;

public class TvVideoView extends StyledPlayerView {
    private SimpleExoPlayer mPlayer;
    private boolean isPlaying;
    private Handler updateHandler = new Handler(Looper.getMainLooper());
    private long duration;
    private long currentPosition;
    private ProgressiveMediaSource.Factory factory;

    public TvVideoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();


    }

    private static boolean isAcronym(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isLowerCase(c)) {
                return false;
            }
        }
        return true;
    }

    private void initialize() {
        this.mPlayer = ExoPlayerFactory.newSimpleInstance(App.getInstance().getApplicationContext());
        this.setPlayer(this.mPlayer);
        setShowNextButton(false);
        setShowPreviousButton(false);
        setShowFastForwardButton(false);
        setShowRewindButton(false);
        setShowShuffleButton(false);
        setControllerShowTimeoutMs(3000);
        setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING);

        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
               // Player.EventListener.super.onPlayerError(error);
                Toast.makeText(App.getInstance().getApplicationContext(), "播放出错", Toast.LENGTH_SHORT).show();
                if (mPlayer.getMediaItemCount() > 1 + mPlayer.getCurrentWindowIndex()) {
                    mPlayer.next();
                } else PlayerController.getInstance().playNextFolder();

            }
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.EventListener.super.onMediaItemTransition(mediaItem, reason);

                PlayerController.getInstance().incPlayCount();
                PlayerController.getInstance().setCurIndex(curIndex);
                curIndex++;

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (mPlayer.getCurrentCues() != null) {
                        for (Cue currentCue : mPlayer.getCurrentCues()) {

                            if (isAcronym(currentCue.text.toString())) {
                                getSubtitleView().setVisibility(View.GONE);

                            } else getSubtitleView().setVisibility(View.VISIBLE);

                            break;

                        }
                    }
                }, 3000);

            }

            @Override
            public void onPlaybackStateChanged(int state) {
                isPlaying = false;

                switch (state) {
                    case Player.STATE_READY:
                        update();
                        break;
                    case Player.STATE_ENDED: {
                        PlayerController.getInstance().incPlayCount();
                        if (PlayerController.getInstance().getCurItem().getdLink() != null)
                            PlayerController.getInstance().playNextFolder();
                        else
                            PlayerController.getInstance().next();

                        break;

                    }
                    case PlaybackState.STATE_ERROR://错误
                        Toast.makeText(App.getInstance().getApplicationContext(), "播放出错", Toast.LENGTH_SHORT).show();
                        if (mPlayer.getMediaItemCount() > 1 + mPlayer.getCurrentWindowIndex()) {
                            mPlayer.next();
                        } else PlayerController.getInstance().playNextFolder();
                        break;
                }
            }
        });
    }

    public void update() {
        Runnable updateRun = new Runnable() {
            @Override
            public void run() {
                TvVideoView.this.isPlaying = TvVideoView.this.mPlayer.isPlaying();
                TvVideoView.this.duration = TvVideoView.this.mPlayer.getDuration();
                TvVideoView.this.currentPosition = TvVideoView.this.mPlayer.getCurrentPosition();


                if (!TvVideoView.this.mPlayer.isPlaying()) {
                    updateHandler.removeCallbacks(this);
                    return;
                }

                updateHandler.postDelayed(this, 5000);
            }

            ;
        };
        updateHandler.postDelayed(updateRun
                , 5000);

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
        new Handler(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null) mPlayer.seekTo(pos);
                    }
                });
    }

    public void pause() {

        new Handler(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null) mPlayer.pause();
                    }
                });
    }

    public void start() {

        new Handler(Looper.getMainLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null)
                            mPlayer.play();
                    }
                });

    }


    public void resume() {
        this.start();
    }

    private VFile[] files;

    private int curIndex = 0;

    public void setVideoURI(Uri uri, VFile res, int curIndex) {
        //mPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);

        files = res.getFolder().getFiles().toArray(new VFile[]{});

        /*if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }*/
       // MediaItem item = MediaItem.fromUri(uri);

        List<MediaSource> mediaItems = new ArrayList<>();
       // mediaItems.add(item);


        MediaItem item;
        for (int i = curIndex ; i < files.length; i++) {
           // if (files[i].getdLink() != null) {
                item = MediaItem.fromUri(SSLSocketClient.ServerManager.getServerHttpAddress()+"/api/vFileUrl?id="+files[i].getId());
                mediaItems.add(this.getProgressiveMediaSource(item));

           // } else break;
        }
        this.curIndex = curIndex;
        mPlayer.setMediaSources(mediaItems,true);
       // mPlayer.prepare();
        mPlayer.prepare();

        int state = mPlayer.getPlaybackState();
        if (state == Player.STATE_ENDED) {
            mPlayer.seekTo(mPlayer.getCurrentWindowIndex(), C.TIME_UNSET);
        } else {
            mPlayer.play();
        }

        //  mPlayer.prepare();
        // mPlayer.play();


    }

    @NonNull
    private ProgressiveMediaSource.Factory getProgressiveMediaFactory() {
        String userAgent = Util.getUserAgent(App.getInstance().getApplicationContext(), "test");

        if(this.factory==null){
            // Default parameters, except allowCrossProtocolRedirects is true
            DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                    userAgent,
                    null /* listener */,
                    DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                    DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                    true /* allowCrossProtocolRedirects */
            );

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                    App.getInstance().getApplicationContext(),
                    null /* listener */,
                    httpDataSourceFactory
            );
            this.factory = new ProgressiveMediaSource.Factory(
                    httpDataSourceFactory);
        }
        return factory;

    }


    @NonNull
    private ProgressiveMediaSource getProgressiveMediaSource(MediaItem item) {
      return  this.getProgressiveMediaFactory().createMediaSource(item);
    }


    public void release() {

        mPlayer.release();
        mPlayer = null;
    }


}
