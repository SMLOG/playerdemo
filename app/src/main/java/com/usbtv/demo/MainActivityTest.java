package com.usbtv.demo;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.RequiresApi;

import com.nurmemet.nur.nurvideoplayer.NurVideoPlayer;
import com.nurmemet.nur.nurvideoplayer.NurVideoView;

public class MainActivityTest extends Activity {

    private NurVideoView nurVideoPlayer;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);



//        String url = "https://stream7.iqilu.com/10339/upload_transcode/202002/09/20200209105011F0zPoYzHry.mp4";
        String url = "http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4";
       /* String url="file:///storage/2067-B583/videos/dy/阳光电影www.ygdy8.com.秘密访客.2021.HD.1080P.国语中英双字.mkv/阳光电影www.ygdy8.com.秘密访客.2021.HD.1080P.国语中英双字.mkv";
        url="file:///storage/36AC6142AC60FDAD/videos/[电影天堂www.dygod.org]忠犬八公的故事BD中英双字.rmvb";
        url="file:///storage/36AC6142AC60FDAD/videos/[电影天堂www.dygod.org]v字仇杀队bd中英双字.rmvb";
        */

        url="file:///storage/36AC6142AC60FDAD/videos/[电影天堂www.dygod.org]v字仇杀队bd中英双字.rmvb";
        url="file:///storage/36AC6142AC60FDAD/a.rmvb";
        url="https://new.iskcd.com/20210913/hLIjsB5w/index.m3u8";

        nurVideoPlayer = findViewById(R.id.videoView);
        nurVideoPlayer.setUp(this, url, "This is video title");
        //nurVideoPlayer.initPlayer(this, Uri.parse(url), "test");
        nurVideoPlayer.start();
    }

    @Override
    public void onBackPressed() {

            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        nurVideoPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nurVideoPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean b = nurVideoPlayer.onKeyDown(keyCode);


        return b || super.onKeyDown(keyCode, event);
    }

}
