package com.usbtv.demo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.j256.ormlite.dao.Dao;
import com.nurmemet.nur.nurvideoplayer.TvVideoView;
import com.nurmemet.nur.nurvideoplayer.listener.OnMediaListener;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.view.SpaceDecoration;
import com.usbtv.demo.view.adapter.GameListAdapter;
import com.usbtv.demo.view.adapter.MyRecycleViewAdapter;
import com.usbtv.demo.view.widget.NavigationCursorView;
import com.usbtv.demo.view.widget.NavigationLinearLayout;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.videoView)
    TvVideoView videoView;

    RelativeLayout mInView;

    @BindView(R.id.home)
    View home;
    private List<Folder> movieList;

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


    private Timer timer;
    private TimerTask timerTask;
    public static MyRecycleViewAdapter numTabAdapter;
    private RecyclerView numTabRecyclerView;
    private RecyclerView moviesRecyclerView;
    public static GameListAdapter moviesRecyclerViewAdapter;
    private List<String> storagePathList;
    private static List<String> getStoragePath(Context mContext, boolean is_removale) {

        ArrayList<String> ret = new ArrayList<>();
        String path = "";
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
    @RequiresApi(api = Build.VERSION_CODES.M)
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

        setContentView(R.layout.activity_main);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(App.URLACTION);
        intentFilter.addAction(App.CMD);
        registerReceiver(receiver, intentFilter);
        bindElementViews();
        initVideo();

        Intent intent = getIntent();


        long id = -1;
        if (intent != null) id = intent.getLongExtra("Movie", -1l);
        if (id > 0) {
            Folder folder = null;
            try {
                Dao<Folder, Integer> dao = App.getHelper().getDao(Folder.class);
                folder = dao.queryForId((int) id);
                PlayerController.getInstance().play(folder.getFiles().iterator().next());

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            PlayerController.getInstance().playNext();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        videoView.resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        videoView.stopPlay();
    }

    private void bindElementViews() {
        videoView = findViewById(R.id.videoView);

        home = findViewById(R.id.home);
        home.setOnFocusChangeListener((view,hasFocus)->{
            numTabRecyclerView.requestFocus();
        });
        numTabRecyclerView = findViewById(R.id.rv_tab);

        moviesRecyclerView = findViewById(R.id.rv_game_list);

        numTabAdapter = new MyRecycleViewAdapter(this,numTabRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        numTabRecyclerView.setLayoutManager(linearLayoutManager);
        numTabRecyclerView.setAdapter(numTabAdapter);


        mNavigationLinearLayout = (NavigationLinearLayout) findViewById(R.id.mNavigationLinearLayout_id);
        List<String> data = new ArrayList<>();

        data.addAll(App.getInstance().getAllTypeMap(true).keySet());

        mNavigationLinearLayout.setDataList(data);
        mNavigationLinearLayout.setNavigationListener(mNavigationListener);


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
                PlayerController.getInstance().play(mList,position);
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
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        moviesRecyclerView.addItemDecoration(new SpaceDecoration(30));
        moviesRecyclerView.setAdapter(moviesRecyclerViewAdapter);


        MyListener myListener = new MyListener(moviesRecyclerViewAdapter);
        numTabAdapter.setOnFocusChangeListener(myListener);
        // adapter.setOnItemClickListener(myListener);

        PlayerController.getInstance().setUIs(videoView, home);



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
                            moviesRecyclerViewAdapter.update(App.getAllTypeMap(false).get(s));

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
        videoView.requestFocus();


        videoView.setOnMediaListener(new OnMediaListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onProgress(int progress, int duration) {

            }

            @Override
            public void onChangeScreen(boolean isPortrait) {

            }

            @Override
            public void onEndPlay() {
            }

            @Override
            public void onCompletion(Object mp) {
                 PlayerController.getInstance().playNext();

            }

            @Override
            public boolean onError(Object mp, int what, int extra) {
                Toast.makeText(MainActivity.this, "播放出错", Toast.LENGTH_SHORT).show();
                PlayerController.getInstance().playNext();
                return true;
            }
        });


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

        if(isShowHome &&(keyCode!=KeyEvent.KEYCODE_ENTER&&KeyEvent.KEYCODE_DPAD_CENTER!=keyCode
                &&KeyEvent.KEYCODE_BACK!=keyCode)){
            return super.onKeyDown(keyCode,event);
        }

        switch (keyCode) {

            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (isShowHome) return false;

                return videoView.onKeyDown(keyCode);

            case KeyEvent.KEYCODE_BACK:    //返回键
                Log.d(TAG, "back--->");
                home.setVisibility(home.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                if (home.getVisibility() == View.VISIBLE) {
                    home.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            numTabRecyclerView.requestFocus();
                        }
                    },100);
                }

                return false;

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
                    PlayerController.getInstance().nextFolder();
                }

                break;

            case KeyEvent.KEYCODE_DPAD_UP:   //向上键
                if (isShowHome) return false;

                Log.d(TAG, "up--->");
                PlayerController.getInstance().prev();
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT: //向左键
                if (isShowHome) {
                    home.onKeyDown(keyCode,event);
                    return false;
                }

                Log.d(TAG, "left--->");

                return videoView.onKeyDown(keyCode);


            case KeyEvent.KEYCODE_DPAD_RIGHT:  //向右键
                if (isShowHome){
                    home.onKeyDown(keyCode,event);
                    return false;
                }
                Log.d(TAG, "right--->");
                return videoView.onKeyDown(keyCode);

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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

}
