package com.usbtv.demo;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.VideoView;

import com.usbtv.demo.data.ResItem;

import java.io.IOException;

public final class PlayerController {

    final static int MODE_RANDOM=1;
    final static int MODE_SEQ=0;
    final static int MODE_LOOP=2;

    private static PlayerController instance;
    private Object mediaObj;
    private ResItem curItem;
    private Integer aIndex;
    private Integer bIndex;
    private int mode;

    private View maskView;
    private PlayerController(){
        curItem = new ResItem();
        curItem.setTypeId(ResItem.VIDEO);
    }

    public Integer getaIndex() {
        return aIndex;
    }

    public void setaIndex(Integer aIndex) {
        this.aIndex = aIndex;
    }

    public Integer getbIndex() {
        return bIndex;
    }

    public void setbIndex(Integer bIndex) {
        this.bIndex = bIndex;
    }

    public void setMediaObj(Object mediaObj) {
        this.mediaObj = mediaObj;
    }

    public void setCurItem(ResItem curItem) {
        this.curItem = curItem;
    }

    public ResItem getCurItem() {
        return curItem;
    }

    public static PlayerController getInstance(){
        if(instance==null)instance = new PlayerController();
        return  instance;
    }

    public long getDuration(){
        if(mediaObj instanceof MediaPlayer){
            MediaPlayer m = (MediaPlayer) mediaObj;
            return m.getDuration();
        }else if(mediaObj instanceof VideoView){
            VideoView v = (VideoView) mediaObj;
            return v.getDuration();
        }
        return 0;
    }
    public long getCurrentPosition(){
        if(mediaObj instanceof MediaPlayer){
            MediaPlayer m = (MediaPlayer) mediaObj;
            return m.getCurrentPosition();
        }else if(mediaObj instanceof VideoView){
            VideoView v = (VideoView) mediaObj;
            return v.getCurrentPosition();
        }
        return 0;
    }

    public boolean isPlaying(){
        if(mediaObj instanceof MediaPlayer){
            MediaPlayer m = (MediaPlayer) mediaObj;
            return m.isPlaying();
        }else if(mediaObj instanceof VideoView){
            VideoView v = (VideoView) mediaObj;
            return v.isPlaying();
        }
        return false;
    }
    public void seekTo(int pos){
        if(mediaObj instanceof MediaPlayer){
            MediaPlayer m = (MediaPlayer) mediaObj;
             m.seekTo(pos);
        }else if(mediaObj instanceof VideoView){
            VideoView v = (VideoView) mediaObj;
             v.seekTo(pos);
        }

    }
    public void pause(){
        if(mediaObj instanceof MediaPlayer){
            MediaPlayer m = (MediaPlayer) mediaObj;
            m.pause();
        }else if(mediaObj instanceof VideoView){
            VideoView v = (VideoView) mediaObj;
            v.pause();
        }
    }

    public void start(){
        if(mediaObj instanceof MediaPlayer){
            MediaPlayer m = (MediaPlayer) mediaObj;
            m.start();
        }else if(mediaObj instanceof VideoView){
            VideoView v = (VideoView) mediaObj;
            v.start();
        }
    }

    public void prepare(){
        if(mediaObj instanceof MediaPlayer){
            MediaPlayer m = (MediaPlayer) mediaObj;
            try {
                m.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showMaskView(){
         Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PlayerController.this.maskView.bringToFront();
                PlayerController.this.maskView.setVisibility(View.VISIBLE);
            }
        });

    }
    public void hideMaskView(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PlayerController.this.maskView.setVisibility(View.GONE);
            }
        });
    }

    public void setMastView(View view) {
        this.maskView = view;
    }
    public boolean isShowMask(){
        return this.maskView.getVisibility()==View.VISIBLE;
    }

    public void setMode(int mode) {
        this.mode=mode;
    }

    public int getMode() {
        return mode;
    }
}
