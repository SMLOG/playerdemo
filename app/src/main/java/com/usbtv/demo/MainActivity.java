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
import android.os.Looper;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.RetrofitServiceApi;
import com.usbtv.demo.comm.RetrofitUtil;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.view.FocusFixedLinearLayoutManager;
import com.usbtv.demo.view.MyMediaPlayer;
import com.usbtv.demo.view.MyVideoView;
import com.usbtv.demo.view.SpaceDecoration;
import com.usbtv.demo.view.adapter.GameListAdapter;
import com.usbtv.demo.view.adapter.MyRecycleViewAdapter;
import com.usbtv.demo.view.widget.MyNumRecyclerView;
import com.usbtv.demo.view.widget.NavigationCursorView;
import com.usbtv.demo.view.widget.NavigationLinearLayout;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


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


    @BindView(R.id.textView)
    TextView textView;


    @BindView(R.id.home)
    View home;
    private List<Folder> movieList;

    private int key = 0;
    private Handler handler = new Handler();
    private MediaController mc;

    MyMediaPlayer mediaPlayer = new MyMediaPlayer();

    private RetrofitServiceApi api = RetrofitUtil.getApi("http://127.0.0.1:8080/");

    public static NavigationLinearLayout mNavigationLinearLayout;
    private NavigationCursorView mNavigationCursorView;


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(App.CMD)) {

                String cmd = intent.getExtras().getString("cmd");
                String val = intent.getExtras().getString("val");
                if ("play".equals(cmd)) {
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
               // status.setVisibility(View.GONE);

            }
            try{
                handler.postDelayed(updateProgressBarThread, 500);

            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    };
    private Timer timer;
    private TimerTask timerTask;
    public static MyRecycleViewAdapter numTabAdapter;
    private MyNumRecyclerView numTabRecyclerView;
    private RecyclerView moviesRecyclerView;
    public static GameListAdapter moviesRecyclerViewAdapter;
    private List<String> storagePathList;


    private static List<String> getStoragePath(Context mContext, boolean is_removale) {

        ArrayList<String> ret = new ArrayList<>();
        String path = "";
        //使用getSystemService(String)检索一个StorageManager用于访问系统存储功能。
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);

            for (int i = 0; i < Array.getLength(result); i++) {
                Object storageVolumeElement = Array.get(result, i);
                path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    ret.add(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        storagePathList = getStoragePath(this, true);

        if (storagePathList != null) {
            for (int i = 0; i < storagePathList.size(); i++) {

                String rootPath = storagePathList.get(i);
                Log.i(TAG, " rootPath： " + rootPath);
                if (DocumentsUtils.checkWritableRootPath(this, rootPath)) {   //检查sd卡路径是否有 权限 没有显示dialog
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        StorageManager sm = getSystemService(StorageManager.class);

                        StorageVolume volume = sm.getStorageVolume(new File(rootPath));

                        if (volume != null) {
                            intent = volume.createAccessIntent(null);
                        }
                    }

                    if (intent == null) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    }
                    startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE + i);
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

        Intent intent = getIntent();


        long id = -1;
        if (intent != null) id = intent.getLongExtra("Movie", -1l);
        if (id > 0) {
            Folder folder = null;
            try {
                Dao<Folder, Integer> dao = App.getHelper().getDao(Folder.class);
                folder = dao.queryForId((int) id);
                PlayerController.getInstance().setTypeId(folder.getTypeId());
                PlayerController.getInstance().play(folder.getFiles().iterator().next());

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            PlayerController.getInstance().playNext();
        }


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

        if (mInView == null) {
            mInView = (RelativeLayout) inflater.inflate(R.layout.activity_main, null, false);//......//添加到WindowManager里面

        } else {
            ViewGroup vg = (ViewGroup) mInView.getParent();
            if (vg != null) {
                vg.removeAllViews();
            }
        }

        //wm.addView(mInView, layoutParams);
        setContentView(mInView);


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


        home = findViewById(R.id.home);
        home.setOnFocusChangeListener((view,hasFocus)->{
            numTabRecyclerView.requestFocus();
        });
        numTabRecyclerView = findViewById(R.id.rv_tab);
       /* numTabRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                View chv = numTabRecyclerView.getChildAt(PlayerController.getInstance().getCurIndex());
              if(chv!=null) chv.requestFocus();
            }

        });*/
        moviesRecyclerView = findViewById(R.id.rv_game_list);

        numTabAdapter = new MyRecycleViewAdapter(this,numTabRecyclerView);

        LinearLayoutManager linearLayoutManager = new FocusFixedLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        numTabRecyclerView.setLayoutManager(linearLayoutManager);
        numTabRecyclerView.setAdapter(numTabAdapter);


        mNavigationLinearLayout = (NavigationLinearLayout) findViewById(R.id.mNavigationLinearLayout_id);
        //mNavigationCursorView = (NavigationCursorView) findViewById(R.id.mNavigationCursorView_id);
        List<String> data = new ArrayList<>();

        data.addAll(App.getInstance().getAllTypeMap().keySet());

        mNavigationLinearLayout.setDataList(data);
        mNavigationLinearLayout.setNavigationListener(mNavigationListener);
        //mNavigationLinearLayout.setNavigationCursorView(mNavigationCursorView);
        //mNavigationLinearLayout.requestFocus();


        movieList = new ArrayList<>();

        try {
            movieList = App.getHelper().getDao(Folder.class).queryForAll();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


        moviesRecyclerViewAdapter = new GameListAdapter(moviesRecyclerView,movieList, this, new GameListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, List<Folder> mList, int position) {
                PlayerController.getInstance().hideMenu();
                PlayerController.getInstance().setTypeId(mList.get(position).getTypeId());
                PlayerController.getInstance().play(mList.get(position).getFiles().iterator().next());
            }
        }) {
            @Override
            protected void onItemFocus(View itemView) {
                itemView.setSelected(true);
                View view = itemView.findViewById(R.id.iv_bg);
                view.setSelected(true);
            }

            @Override
            protected void onItemGetNormal(View itemView) {
                itemView.setSelected(true);
                View view = itemView.findViewById(R.id.iv_bg);
                view.setSelected(true);
            }
        };
        moviesRecyclerView.setLayoutManager(new FocusFixedLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        moviesRecyclerView.addItemDecoration(new SpaceDecoration(30));
        moviesRecyclerView.setAdapter(moviesRecyclerViewAdapter);


        MyListener myListener = new MyListener(moviesRecyclerViewAdapter);
        numTabAdapter.setOnFocusChangeListener(myListener);
       // adapter.setOnItemClickListener(myListener);

        PlayerController.getInstance().setUIs(textView, videoView, home);



    }

    private NavigationLinearLayout.NavigationListener mNavigationListener = new NavigationLinearLayout.NavigationListener() {
        @Override
        public void onNavigationChange(String s, int pos, int keyCode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT: //模拟刷新内容区域
                  //  moviesRecyclerViewAdapter.update(App.getAllTypeMap().get(s));
                    //rvGameList.smoothScrollToPosition(0);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            moviesRecyclerViewAdapter.update(App.getAllTypeMap().get(s));

                        }});
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    break;
                case KeyEvent.KEYCODE_MENU:
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("grant");
        android.os.Environment.getExternalStorageDirectory();
        //new File(android.os.Environment.getExternalStorageDirectory()+File.separator+"test/abc").getParentFile().mkdirs();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode >= DocumentsUtils.OPEN_DOCUMENT_TREE_CODE && requestCode < DocumentsUtils.OPEN_DOCUMENT_TREE_CODE + storagePathList.size()) {

            if (data != null && data.getData() != null) {
                Uri uri = data.getData();

                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, takeFlags);

                DocumentsUtils.saveTreeUri(this, this.storagePathList.get(requestCode - DocumentsUtils.OPEN_DOCUMENT_TREE_CODE), uri);

            }

        } else {
            isVideoPlay(false, 0);

        }


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
                //mp.release();
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
        private TimerTask task;

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
            if(fromUser){
                videoView.seekTo(progress);
                MainActivity.this.startDelayHideStatusTask();
            }

        }
    };

    private void startDelayHideStatusTask() {

        try{
            timerTask.cancel();
        }catch (Exception e){

        }
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                status.post(new Runnable() {
                    @Override
                    public void run() {
                        status.setVisibility(View.GONE);
                    }
                });
            }
        };

        timer.schedule(timerTask,5000);
    }

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

        boolean isShowHome = home.getVisibility() == View.VISIBLE;
        switch (keyCode) {

            case KeyEvent.KEYCODE_ENTER:     //确定键enter
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d(TAG, "enter--->");
                if (isShowHome) {
                   // home.setVisibility(View.GONE);
                    return super.onKeyDown(keyCode, event);
                }

                //如果是播放中则暂停、如果是暂停则继续播放
                if(!videoView.isPlaying() || status.getVisibility()==View.VISIBLE)
                    isVideoPlay(videoView.isPlaying(), key);
                else{

                    showStatusAndAutoHide();
                }

                break;

            case KeyEvent.KEYCODE_BACK:    //返回键
                Log.d(TAG, "back--->");
                home.bringToFront();
                home.setVisibility(home.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                if (home.getVisibility() == View.VISIBLE) {
                    home.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            numTabRecyclerView.requestFocus();
                        }
                    },100);
                    /*recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.requestFocus();
                        }
                    },1000);*/
                }

                //startRun();

                //Intent intent = new Intent(MainActivity.this, RegularVerticalActivity.class);
                //startActivity(intent);

                /*if(this.showList==null){


                WindowManager wm = (WindowManager) getApplicationContext().getSystemService(
                        Context.WINDOW_SERVICE);

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
                layoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                layoutParams.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;


                //layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                //layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;


                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());//加载需要的XML布局文件
                    FrameLayout mInView = (FrameLayout)inflater.inflate(R.layout.activity_regular_vertical, null, false);

                VerticalGridView gridView = mInView.findViewById(R.id.id_grid_vertical);



                gridView.addItemDecoration(new SpaceItemDecoration());
                gridView.setNumColumns(3);
                GridObjectAdapter adapter = new GridObjectAdapter(new RegularVerticalPresenter(this));
                gridView.setFocusZoomFactor(FocusHighlightHelper.ZOOM_FACTOR_SMALL);

                try {
                    List<Folder> list = App.getHelper().getDao(Folder.class).queryForAll();

                    gridView.setAdapter(adapter);
                    for (int i = 0; i < list.size(); i++) {

                        adapter.add(list.get(i));
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                    mInView.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
                            if(keycode==KeyEvent.KEYCODE_BACK&&keyEvent.getAction()==KeyEvent.ACTION_UP){

                                getWindowManager().removeViewImmediate(MainActivity.this.showList);
                                MainActivity.this.showList = null;

                                return true;
                            }
                            return false;
                        }
                    });
                wm.addView(mInView, layoutParams);
                this.showList = mInView;
                mInView.setFocusable(true);
                mInView.setFocusableInTouchMode(true);
                mInView.requestFocus();


                }else {
                    getWindowManager().removeViewImmediate(this.showList);
                    this.showList = null;
                }*/

                return false;   //这里由于break会退出，所以我们自己要处理掉 不返回上一层

            case KeyEvent.KEYCODE_SETTINGS: //设置键
                Log.d(TAG, "setting--->");

                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:   //向下键

                if (isShowHome) return false;
                /*    实际开发中有时候会触发两次，所以要判断一下按下时触发 ，松开按键时不触发
                 *    exp:KeyEvent.ACTION_UP
                 */
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    Log.d(TAG, "down--->");
                    PlayerController.getInstance().next2();
                }

                break;

            case KeyEvent.KEYCODE_DPAD_UP:   //向上键
                if (isShowHome) return false;

                Log.d(TAG, "up--->");
                PlayerController.getInstance().prev();
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT: //向左键
                if (isShowHome) return false;

                Log.d(TAG, "left--->");

                showStatusAndAutoHide();
                timeSeekBar.requestFocus();

                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:  //向右键
                if (isShowHome) return false;
                Log.d(TAG, "right--->");
                showStatusAndAutoHide();
                timeSeekBar.requestFocus();

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

    private void showStatusAndAutoHide() {
        status.setVisibility(View.VISIBLE);
        startDelayHideStatusTask();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
