package com.usbtv.demo.view;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;

public class MyMediaPlayer extends MediaPlayer {
    private ArrayList<String> playSources = new ArrayList<>();
    private ArrayList<Integer> durations = new ArrayList<>();
    private Handler handler = new Handler();
    private int curIndex;
    private CountDownTimer cntr_aCounter;
    public void addPlaySource(String proxyUrl, int i) throws IOException {
        playSources.add(proxyUrl);
        durations.add(i);
        if(playSources.size()==1)
        {
            this.setDataSource(playSources.get(0));
            curIndex = 0;

        }
    }

    @Override
    public void start() throws IllegalStateException {
        if(durations.get(curIndex)>0) {
            cntr_aCounter = new CountDownTimer(1000*durations.get(curIndex), 1000) {
                public void onTick(long millisUntilFinished) {

                    //MyMediaPlayer.super.start();
                }

                public void onFinish() {
                    if( MyMediaPlayer.this.isPlaying()) {
                       // MyMediaPlayer.this.stop();
                       MyMediaPlayer.this.seekTo( MyMediaPlayer.this.getDuration());
                    }
                }
            };
            cntr_aCounter.start();
        }
        super.start();

    }

    @Override
    public void reset() {
        synchronized (this){
            handler.removeCallbacksAndMessages(null);
            super.reset();
            if(playSources!=null)playSources.clear();
            if(durations!=null)durations.clear();
            if(cntr_aCounter!=null) cntr_aCounter.cancel();
        }

    }

    public  synchronized boolean isAllCompletedOrContinuePlayNext() {
        if(playSources.size()==0)return true;
        if(curIndex == playSources.size()-1)return true;
        else {
            curIndex++;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {

                        MyMediaPlayer.this.play();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }},2000);

            return false;
        }
    }

    private void play() {

        try {
            super.reset();
            this.setDataSource(playSources.get(curIndex));
            this.prepare();
            this.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
