package com.usbtv.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.usbtv.demo.view.MyVideoView;
import com.usbtv.demo.view.SelectPicPopupWindow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


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

    private int key = 0;
    private Handler handler = new Handler();
    private MediaController mc;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(App.URLACTION)) {
                int aIndex = intent.getIntExtra("aIndex", 0);
                int bIndex = intent.getIntExtra("bIndex", 0);
                reNewVideoUri(aIndex,bIndex);
            }
        }
    };


    private Runnable updateProgressBarThread = new Runnable() {
        public void run() {
            //Utils.exec("input keyevent 4");
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
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (Build.VERSION.SDK_INT == 28) {
            try {
                ViewParent viewRootImpl = getWindow().getDecorView().getParent();
                Class viewRootImplClass = viewRootImpl.getClass();

                Field mAttachInfoField = viewRootImplClass.getDeclaredField("mAttachInfo");
                mAttachInfoField.setAccessible(true);
                Object mAttachInfo = mAttachInfoField.get(viewRootImpl);
                Class mAttachInfoClass = mAttachInfo.getClass();

                Field mHasWindowFocusField = mAttachInfoClass.getDeclaredField("mHasWindowFocus");
                mHasWindowFocusField.setAccessible(true);
                mHasWindowFocusField.set(mAttachInfo, true);
                boolean mHasWindowFocus = (boolean) mHasWindowFocusField.get(mAttachInfo);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //Utils.verifyStoragePermissions(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(App.URLACTION);
        registerReceiver(receiver, intentFilter);

        timeSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        DowloadPlayList.reloadPlayList();
        //Utils.setKeyPress(4);
        //initDelayShutdown();
        timer = new Timer();

        initVideo();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                //
                //mp.setLooping(true);
                reNewVideoUri(App.playList.getaIndex(),-1);
                //mp.start();
                //btnRestartPlay.setVisibility(View.VISIBLE);
                //layFinishBg.setVisibility(View.VISIBLE);
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, "播放出错", Toast.LENGTH_SHORT).show();
                reNewVideoUri(App.playList.getaIndex(),-1);
                return true;
            }
        });




    }

    private void initDelayShutdown() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isVideoPlay(true,0);
                startActivity(new Intent(MainActivity.this, SelectPicPopupWindow.class));
            }
        },30*60*1000);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isVideoPlay(false,0);
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
        mc=new MediaController(this);
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

    }

    private void reNewVideoUri(int aIndex,int bIndex) {
        Uri uri = null;

        App.playList.setAIndex(aIndex);
        String  url = App.playList.nextURL(bIndex);

        try {
            uri = Uri.parse("android.resource://" + getPackageName() + "/raw/test");

            if (url != null && url.trim() != "") {
                uri = Uri.parse(App.getProxyUrl(url));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if(url==null){
            url = "http://www.baidu.com";
        }
        if(url!=null) {
            Log.d(TAG,url);
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            //videoView.start();
        }

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
                }

                break;

            case KeyEvent.KEYCODE_DPAD_UP:   //向上键
                Log.d(TAG, "up--->");

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

    //模拟按键加音量，按六次
//因命令执行会阻塞线程， 所以在子线程里执行
    private void startRun() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = Message.obtain();
                message.what = 6000;
                mHandler.sendMessage(message);
            }
        };
        timer.schedule(timerTask, 10);
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message paramMessage) {
            switch (paramMessage.what) {
                case 6000:
                    execByRuntime("input keyevent 4");

            }
        }
    };



    public static String execByRuntime(String cmd) {
        Process process = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);

            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = bufferedReader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != inputStreamReader) {
                try {
                    inputStreamReader.close();
                } catch (Throwable t) {
                    //
                }
            }
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (Throwable t) {
                    //
                }
            }
            if (null != process) {
                try {
                    process.destroy();
                } catch (Throwable t) {
                    //
                }
            }
        }
    }

}
