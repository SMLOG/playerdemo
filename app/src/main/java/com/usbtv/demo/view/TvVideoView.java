package com.usbtv.demo.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.annotation.Nullable;

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

import java.util.Collection;

public class TvVideoView extends StyledPlayerView {
    private  SimpleExoPlayer mPlayer;

    public TvVideoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mPlayer =  ExoPlayerFactory.newSimpleInstance(App.getInstance().getApplicationContext());
        this.setPlayer(this.mPlayer);

        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Player.EventListener.super.onPlayerError(error);
                Toast.makeText(App.getInstance().getApplicationContext(), "播放出错", Toast.LENGTH_SHORT).show();
                mPlayer.next();
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state){
                   case Player.STATE_ENDED:{
                        PlayerController.getInstance().playNextFolder();

                        break;
                    }
                }
            }
        });
    }



    public void setUp(MainActivity mainActivity) {
    }

    public boolean onKeyDown(int keyCode) {
        return false;
    }

    public long getDuration() {
        if(mPlayer!=null)return  mPlayer.getDuration();
        return 0l;
    }

    public long getCurrentPosition() {
        if(mPlayer!=null)return  mPlayer.getCurrentPosition();
        return 0l;
    }

    public boolean isPlaying() {

       return mPlayer != null &&  mPlayer.isPlaying();
    }

    public void seekTo(int pos) {
        if(mPlayer!=null) mPlayer.seekTo(pos);
    }

    public void pause() {
        if(mPlayer!=null)mPlayer.setPlayWhenReady(false);
    }

    public void start() {
        if(mPlayer!=null)mPlayer.setPlayWhenReady(true);
        mPlayer.play();


    }


    public void resume() {
        this.start();
    }

    public void setVideoURI(VFile res) {

        mPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);
        //mPlayer.clearMediaItems();
        VFile[] files = res.getFolder().getFiles().toArray(new VFile[]{});
        int i= files.length-1;
        for(;i>0;i--){
            if(files[i].getId()==res.getId()){
                break;
            }
        }
        int c=mPlayer.getMediaItemCount();

        for(;i<files.length;i++){
            MediaItem item = MediaItem.fromUri( files[i].getdLink()!=null?files[i].getdLink(): SSLSocketClient.ServerManager.getServerHttpAddress() + "/vFileUrl?id="+files[i].getId() );
            mPlayer.addMediaItem(item);
        }
        mPlayer.setPlayWhenReady(true);
        mPlayer.prepare();
        if(c>0){
            mPlayer.next();
            for(;c>=0;c--){
                mPlayer.removeMediaItem(c);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        mPlayer.release();
        super.finalize();
    }
}
