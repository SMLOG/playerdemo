package com.usbtv.demo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.Toast;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.CueGroup;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.shuyu.simple.model.GSYVideoModel;
import com.shuyu.simple.player.IjkPlayer;
import com.shuyu.simple.video.StandardGSYVideoPlayer;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.data.VFile;

import java.util.ArrayList;
import java.util.List;

public class GsyTvVideoView extends StandardGSYVideoPlayer implements Player.Listener{
    private boolean releaseCompleted;

    public GsyTvVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mDismissControlTime = 2000;

        IjkPlayer ijkplayer = new IjkPlayer(context);

        setPlayer(ijkplayer);


    }



    private SubtitleView mSubtitleView;

    @Override
    protected void init(Context context) {
        super.init(context);
        mSubtitleView = findViewById(R.id.sub_title_view);
        mSubtitleView.setUserDefaultStyle();
        mSubtitleView.setUserDefaultTextSize();

        //mSubtitleView.setStyle(new CaptionStyleCompat(Color.RED, Color.TRANSPARENT, Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_NONE, CaptionStyleCompat.EDGE_TYPE_NONE, null));
       // mSubtitleView.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
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
    int cueCount=0;
    @Override
    public void onCues(CueGroup cueGroup) {
        if (mSubtitleView != null) {
            mSubtitleView.setCues(cueGroup.cues);
            for(Cue cue:cueGroup.cues){
               if(cue.text!=null &&
                       !"".equals(cue.text.toString().trim()) &&
                       isAcronym(cue.text.toString())){
                   cueCount++;
               }else cueCount=0;
            }
            if(cueCount>3)
            mSubtitleView.setVisibility(GONE);
            else mSubtitleView.setVisibility(VISIBLE);

        }
    }
    @Override
    public int getLayoutId() {
        return R.layout.video_layout_subtitle;
    }





    @Override
    public void onAutoCompletion() {

        PlayerController.getInstance().incPlayCount();
        PlayerController.getInstance().setCurIndex(PlayerController.getInstance().getCurIndex()+1);
        mPlayPosition++;

    }

    @Override
    public void onPlayNext() {
        PlayerController.getInstance().incPlayCount();
        PlayerController.getInstance().setCurIndex(PlayerController.getInstance().getCurIndex()+1);

    }

    @Override
    public void onCompletion() {
        super.onCompletion();
        if(!releaseCompleted) PlayerController.getInstance().next();
        else releaseCompleted=false;
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);
        Toast.makeText(App.getInstance().getApplicationContext(), "播放出错", Toast.LENGTH_SHORT).show();

    }

    public void setVideoURI(Uri videoUrl, VFile res, int fi) {

        //releaseVideos();
        mPauseBeforePrepared=false;
        getIPlayer().pause();
        if(res.getFolder().getTypeId()>=500 && res.getFolder().getTypeId()<600){
            // GSYVideoManager.onPause();
            //  GSYExoVideoManager.onPause();
        }else{
            //  GSYVideoManager.onPause();

        }

        VFile[] files = res.getFolder().getFiles().toArray(new VFile[]{});
        int curIndex = fi;


        List<GSYVideoModel> urls = new ArrayList<>();

        for (int i = curIndex; i < files.length; i++) {
            urls.add(
                    new GSYVideoModel(files[i].getdLink() != null ? App.getUrl(files[i].getdLink()) :
                            (SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vFileUrl.mp4?id=" + files[i].getId())

                            , files[i].getName()));
        }

        getIPlayer().setMediaItems(urls,0);

        releaseCompleted = true;
        startPlayLogic();
        releaseCompleted = false;

    }


    private boolean playing;

    public boolean isPlaying() {

        updateHandler.post(() -> {
            playing = this.getCurrentState()==CURRENT_STATE_PLAYING;
        });
        return playing;

    }
    private long seekTime;
    @Override
    public void seekTo(long time) {
        seekTime = time;
        updateHandler.postDelayed(() -> {
            super.seekTo(seekTime);
        },300);

    }
    private Handler updateHandler = new Handler(Looper.getMainLooper());
    private long currentPosition;

    private long duration;
    public long getDuration(){
        updateHandler.post(() -> {
            duration = super.getDuration();
        });
        return duration;
    }
    public long getCurrentPosition() {

        updateHandler.post(() -> {
            currentPosition = super.getCurrentPositionWhenPlaying();
        });
        return currentPosition;
    }

    public void onPause() {
        updateHandler.post(() -> {
            super.onVideoPause();

            // super.pauseLogic(null, true);
        });
    }

    public void start() {
       onResume();
    }

    public void onResume() {
        updateHandler.post(() -> {
            onVideoResume();
           getIPlayer().start();
        });
    }

    public boolean onKeyDown(int keyCode) {
        return false;
    }
}
