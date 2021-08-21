package com.usbtv.demo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.usbtv.demo.comm.HttpCallback;
import com.usbtv.demo.comm.RetrofitServiceApi;
import com.usbtv.demo.comm.RetrofitUtil;
import com.usbtv.demo.data.ResItem;
import com.usbtv.demo.view.MyImageView;
import com.usbtv.demo.view.MyMediaPlayer;
import com.usbtv.demo.view.MyVideoView;
import com.usbtv.demo.view.SelectPicPopupWindow;

import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;

import butterknife.BindView;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {


    @BindView(R.id.video_view)
    MyVideoView videoView;

    @BindView(R.id.tv_play_time)
    TextView tvPlayTime;
    @BindView(R.id.time_seekBar)
    SeekBar timeSeekBar;
    @BindView(R.id.tv_total_time)
    TextView tvTotalTime;
    @BindView(R.id.lay_finish_bg)
    RelativeLayout layFinishBg;
    @BindView(R.id.btn_play_or_pause)
    ImageButton btnPlayOrPause;
    @BindView(R.id.btn_restart_play)
    ImageButton btnRestartPlay;
    @BindView(R.id.status)
    RelativeLayout status;
    RelativeLayout mInView;
    @BindView(R.id.imageView)
    MyImageView imageView;

    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.bgTextView)
    TextView bgTextView;

    private int key = 0;
    private Handler handler = new Handler();
    private MediaController mc;

    MyMediaPlayer mediaPlayer = new MyMediaPlayer();

    private RetrofitServiceApi api = RetrofitUtil.getApi("http://127.0.0.1:8080/");

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

           if (intent.getAction().equals(App.CMD)) {

                String cmd = intent.getExtras().getString("cmd");
                String val = intent.getExtras().getString("val");

                if("detach".equals(cmd) ){

                    if(Boolean.parseBoolean(val)){
                        if(PlayerController.getInstance().isDetach())return;
                        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(
                                Context.WINDOW_SERVICE);
                        wm.removeViewImmediate(mInView);
                        setContentView(mInView);
                        PlayerController.getInstance().setDetach(true);
                    }else{
                        //setContentView(null);
                        if(!PlayerController.getInstance().isDetach())return;

                        setTopView();
                    }
                }else if("play".equals(cmd)){
                    PlayerController.getInstance().play(null);

                }
            }
        }
    };


    private Runnable updateProgressBarThread = new Runnable() {
        public void run() {
            if (videoView.isPlaying()) {
                int current = videoView.getCurrentPosition();
                timeSeekBar.setProgress(current);

                tvPlayTime.setText(time(videoView.getCurrentPosition()));
                status.setVisibility(View.GONE);

            }
            handler.postDelayed(updateProgressBarThread, 500);
        }
    };
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

           /* requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
                    */
        //申请权限
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }
//new File("/storage/36AC6142AC60FDAD/videos/541422159/3/a/a.mp4").getParentFile().mkdirs();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(App.URLACTION);
        intentFilter.addAction(App.CMD);
        registerReceiver(receiver, intentFilter);
        setTopView();
        bindElementViews();
        initViews();
        PlayerController.getInstance().playNext();

    }

    private void setTopView() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {

            }
        }

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        layoutParams.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;


        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        //layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;


        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());//加载需要的XML布局文件


        //View view = LayoutInflater.from(this).inflate(R.layout.activity_main,null);

        if(mInView==null){
            mInView = (RelativeLayout) inflater.inflate(R.layout.activity_main, null, false);//......//添加到WindowManager里面

        }else{
            ViewGroup vg =  (ViewGroup) mInView.getParent();
            if(vg!=null){
                vg.removeAllViews();
            }
        }

        //wm.addView(mInView, layoutParams);
        setContentView(mInView);
        PlayerController.getInstance().setDetach(false);


    }

    private void initViews() {
        timeSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        timer = new Timer();

        initVideo();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    private void bindElementViews() {
        videoView = mInView.findViewById(R.id.video_view);
        timeSeekBar = mInView.findViewById(R.id.time_seekBar);
        tvPlayTime = mInView.findViewById(R.id.tv_play_time);
        tvTotalTime = mInView.findViewById(R.id.tv_total_time);
        layFinishBg = mInView.findViewById(R.id.lay_finish_bg);
        btnPlayOrPause = mInView.findViewById(R.id.btn_play_or_pause);
        btnRestartPlay = mInView.findViewById(R.id.btn_restart_play);
        status = mInView.findViewById(R.id.status);
        textView = mInView.findViewById(R.id.textView);

        bgTextView = mInView.findViewById(R.id.bgTextView);
        imageView = mInView.findViewById(R.id.imageView);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        PlayerController.getInstance().setUIs(bgTextView,imageView,textView,videoView,mediaPlayer);


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       // System.out.println("grant");android.os.Environment.getExternalStorageDirectory();
      //  new File("/storage/36AC6142AC60FDAD/videos/541422159/3/a/a.mp4").getParentFile().mkdirs();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isVideoPlay(false, 0);
    }

    /**
     * 时间转换方法
     *
     * @param millionSeconds
     * @return
     */
    protected String time(long millionSeconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millionSeconds);
        return simpleDateFormat.format(c.getTime());
    }

    /**
     * 初始化VideoView
     */
    private void initVideo() {
        mc = new MediaController(this);
        mc.setVisibility(View.GONE);
        videoView.setMediaController(mc);
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int totalTime = videoView.getDuration();//获取视频的总时长
                tvTotalTime.setText(stringForTime(totalTime));

                // 开始线程，更新进度条的刻度
                handler.postDelayed(updateProgressBarThread, 0);
                timeSeekBar.setMax(videoView.getDuration());
                //视频加载完成,准备好播放视频的回调
                videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                PlayerController.getInstance().playNext();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, "播放出错", Toast.LENGTH_SHORT).show();
                PlayerController.getInstance().playNext();
                return true;
            }
        });

    }


    /**
     * 控制视频是  播放还是暂停  或者是重播
     *
     * @param isPlay
     * @param keys
     */
    private void isVideoPlay(boolean isPlay, int keys) {
        switch (keys) {
            case 0:
                if (isPlay) {//暂停
                    btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.icon_player));
                    btnPlayOrPause.setVisibility(View.VISIBLE);
                    videoView.pause();


                } else {//继续播放
                    btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.icon_pause));
                    btnPlayOrPause.setVisibility(View.VISIBLE);
                    // 开始线程，更新进度条的刻度
                    handler.postDelayed(updateProgressBarThread, 0);
                    videoView.start();
                    timeSeekBar.setMax(videoView.getDuration());
                    timeGone();
                }
                break;
            case 1://重新播放
                initVideo();
                btnRestartPlay.setVisibility(View.GONE);
                layFinishBg.setVisibility(View.GONE);
                key = 0;
                break;
        }

    }

    /**
     * 延时隐藏
     */
    private void timeGone() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnPlayOrPause.setVisibility(View.INVISIBLE);
            }
        }, 1500);

    }

    /**
     * 进度条监听
     */
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        // 当进度条停止修改的时候触发
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 取得当前进度条的刻度
            int progress = seekBar.getProgress();
            if (videoView.isPlaying()) {
                // 设置当前播放的位置
                videoView.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

        }
    };

    //将长度转换为时间
    StringBuilder mFormatBuilder = new StringBuilder();
    Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    private String TAG = "key";

    /**
     * 遥控器按键监听
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_ENTER:     //确定键enter
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d(TAG, "enter--->");
                //如果是播放中则暂停、如果是暂停则继续播放
                isVideoPlay(videoView.isPlaying(), key);
                status.setVisibility(View.VISIBLE);
                break;

            case KeyEvent.KEYCODE_BACK:    //返回键
                Log.d(TAG, "back--->");
                //startRun();
                return false;   //这里由于break会退出，所以我们自己要处理掉 不返回上一层

            case KeyEvent.KEYCODE_SETTINGS: //设置键
                Log.d(TAG, "setting--->");

                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:   //向下键

                /*    实际开发中有时候会触发两次，所以要判断一下按下时触发 ，松开按键时不触发
                 *    exp:KeyEvent.ACTION_UP
                 */
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    Log.d(TAG, "down--->");
                    PlayerController.getInstance().next2();
                }

                break;

            case KeyEvent.KEYCODE_DPAD_UP:   //向上键
                Log.d(TAG, "up--->");
                PlayerController.getInstance().prev();
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT: //向左键
                Log.d(TAG, "left--->");
                if (videoView.getCurrentPosition() > 4) {
                    videoView.seekTo(videoView.getCurrentPosition() - 5 * 1000);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:  //向右键
                Log.d(TAG, "right--->");
                videoView.seekTo(videoView.getCurrentPosition() + 5 * 1000);
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:   //调大声音键
                Log.d(TAG, "voice up--->");
                //startRun();
                break;

            case KeyEvent.KEYCODE_VOLUME_DOWN: //降低声音键
                Log.d(TAG, "voice down--->");

                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE: //禁用声音
                Log.d(TAG, "voice mute--->");
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        MyMediaPlayer mymediaPlayer = (MyMediaPlayer) mediaPlayer;
        if (mymediaPlayer.isAllCompletedOrContinuePlayNext())
            PlayerController.getInstance().playNext();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        PlayerController.getInstance().playNext();
        return false;
    }
}
