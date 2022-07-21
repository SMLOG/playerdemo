package com.usbtv.demo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.leanback.widget.BrowseFrameLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.king.zxing.util.CodeUtils;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.DocumentsUtils;
import com.usbtv.demo.comm.MyBroadcastReceiver;
import com.usbtv.demo.comm.PlayerController;
import com.usbtv.demo.comm.SSLSocketClient;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.view.SpaceDecoration;
import com.usbtv.demo.view.TvVideoView;
import com.usbtv.demo.view.adapter.FolderCatsListRecycleViewAdapter;
import com.usbtv.demo.view.adapter.FolderListAdapter;
import com.usbtv.demo.view.adapter.FolderNumListRecycleViewAdapter;
import com.usbtv.demo.view.adapter.QtabListRecycleViewAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;

import static android.view.View.FOCUS_DOWN;
import static android.view.View.FOCUS_UP;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.videoView)
    TvVideoView videoView;


    @BindView(R.id.menuPanel)
    BrowseFrameLayout menuPanel;

    public static RecyclerView folderCatsRV;


    private final BroadcastReceiver receiver = new MyBroadcastReceiver();


    public static FolderNumListRecycleViewAdapter numAdapter;
    private RecyclerView numTabRecyclerView;
    private RecyclerView foldersRecyclerView;
    public static FolderListAdapter foldersAdapter;
    private List<String> storagePathList;
    private FolderCatsListRecycleViewAdapter catsAdaper;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

           /* requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
                    */
        try {
            requestPermissionAndStorage();
        }catch (Throwable e){
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        registBroadcastReceiver();
        bindElementViews();
        initVideo();

        continuePlayPrevious();

        try {
            //android 10.0 startup when completed
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            }
        }catch (Throwable e){

        }

    }

    private void registBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(App.URLACTION);
        intentFilter.addAction(App.CMD);
        registerReceiver(receiver, intentFilter);
    }

    private void continuePlayPrevious() {
        Intent intent = getIntent();

        long id = -1;
        if (intent != null) id = intent.getLongExtra("Movie", -1l);

        PlayerController.getInstance().init(id);

    }

    private void requestPermissionAndStorage() {
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

        storagePathList = Utils.getStoragePath(this, true);

        if (storagePathList != null) {
            for (int i = 0; i < storagePathList.size(); i++) {

                String rootPath = storagePathList.get(i);
                if (DocumentsUtils.checkWritableRootPath(this, rootPath)) {   //检查sd卡路径是否有 权限 没有显示dialog
                    Intent intent = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        StorageManager sm = getSystemService(StorageManager.class);

                        StorageVolume volume = sm.getStorageVolume(new File(rootPath));

                        if (volume != null) {
                            intent = volume.createAccessIntent(null);


                            //startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE2 + i);
                            //return;
                        }

                        if (intent==null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            intent = volume.createOpenDocumentTreeIntent();
                        }
                    }

                    if (intent == null) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |  Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    }
                    startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE + i);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode >= DocumentsUtils.OPEN_DOCUMENT_TREE_CODE2 && requestCode < DocumentsUtils.OPEN_DOCUMENT_TREE_CODE2 + storagePathList.size()) {

            if (data != null && data.getData() != null) {
                Uri uri = data.getData();

                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, takeFlags);

                DocumentsUtils.saveTreeUri(this, this.storagePathList.get(requestCode - DocumentsUtils.OPEN_DOCUMENT_TREE_CODE), uri);

            }

        }
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
    }


    private void bindElementViews() {



        videoView = findViewById(R.id.videoView);

        menuPanel = findViewById(R.id.menuPanel);


        numTabRecyclerView = findViewById(R.id.numTabRV);

        foldersRecyclerView = findViewById(R.id.foldersRV);
        RecyclerView qTabRecyclerView = findViewById(R.id.qTab);
        QtabListRecycleViewAdapter qAdapter = new QtabListRecycleViewAdapter(this, qTabRecyclerView);
        qTabRecyclerView.setAdapter(qAdapter);

        numAdapter = new FolderNumListRecycleViewAdapter(this, numTabRecyclerView);

        // numTabRecyclerView.setLayoutManager( new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        numTabRecyclerView.setAdapter(numAdapter);



        foldersAdapter = new FolderListAdapter(foldersRecyclerView, this, new FolderListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Folder folder, int position) {
                PlayerController.getInstance().hideMenu();
                PlayerController.getInstance().play(folder, position,0);

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
        folderCatsRV = findViewById(R.id.folderCats);

        catsAdaper = new FolderCatsListRecycleViewAdapter(this, folderCatsRV, foldersAdapter);


        // folderCatsRV.setLayoutManager( new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        folderCatsRV.setAdapter(catsAdaper);
        //folderCatsRV.setItemAnimator(null);

        // foldersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        foldersRecyclerView.addItemDecoration(new SpaceDecoration(30));
        foldersRecyclerView.setAdapter(foldersAdapter);

        menuPanel.setOnFocusSearchListener(new BrowseFrameLayout.OnFocusSearchListener() {
            @Override
            public View onFocusSearch(View focused, int direction) {
                if (foldersRecyclerView.hasFocus()) {
                    switch (direction) {
                        case FOCUS_DOWN:
                            return folderCatsRV;
                        case FOCUS_UP:
                            if(numTabRecyclerView.getVisibility()==VISIBLE)
                            return numTabRecyclerView;
                            else return foldersRecyclerView;
                    }
                } else if (folderCatsRV.hasFocus()) {

                    switch (direction) {
                        case FOCUS_DOWN:
                            return folderCatsRV;
                        case FOCUS_UP:
                            return foldersRecyclerView;
                    }

                } else if (numTabRecyclerView.hasFocus()) {

                    switch (direction) {
                        case FOCUS_DOWN:
                            return foldersRecyclerView;
                        case FOCUS_UP:
                            return qTabRecyclerView.getVisibility()==VISIBLE?qTabRecyclerView: numTabRecyclerView;
                    }

                }
                    else if (qTabRecyclerView.hasFocus()) {

                    switch (direction) {
                        case FOCUS_DOWN:
                            return numTabRecyclerView;
                        case FOCUS_UP:
                            return qTabRecyclerView;
                    }

                }

                return null;
            }
        });

       // MyListener myListener = new MyListener(moviesRecyclerViewAdapter);
       // numTabAdapter.setOnFocusChangeListener(myListener);

       PlayerController.getInstance().setUIs(videoView, menuPanel,numTabRecyclerView,qTabRecyclerView,foldersRecyclerView);
       videoView.setUp(this);

        PlayerController.getInstance().setRVAdapts(catsAdaper, foldersAdapter, numAdapter,qAdapter);
        PlayerController.getInstance().refreshCats();



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("grant");
        //new File(android.os.Environment.getExternalStorageDirectory()+File.separator+"test/abc").getParentFile().mkdirs();

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

        PlayerController.getInstance().setContext(this);



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
        boolean isShowHome = menuPanel.getVisibility() == View.VISIBLE;

        if (isShowHome && (keyCode != KeyEvent.KEYCODE_ENTER && KeyEvent.KEYCODE_DPAD_CENTER != keyCode
                && KeyEvent.KEYCODE_BACK != keyCode)) {
            return super.onKeyDown(keyCode, event);
        }

        switch (keyCode) {

            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (isShowHome) return false;

                return videoView.onKeyDown(keyCode);

            case KeyEvent.KEYCODE_BACK:    //返回键
                Log.d(TAG, "back--->");
                menuPanel.setVisibility(menuPanel.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                if (menuPanel.getVisibility() == View.VISIBLE) {
                    menuPanel.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            foldersRecyclerView.requestFocus();
                        }
                    }, 100);
                }

                return false;

            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_MENU: //设置键
                Log.d(TAG, "KEYCODE_MENU--->");
                View menu = findViewById(R.id.menu);
                if(menu.getVisibility()==View.GONE){
                    ImageView erm = findViewById(R.id.erm);

                    String ip = Utils.getIPAddress();
                    TextView ermText = findViewById(R.id.ermText);

                    if(ip!=null){
                        ermText.setText("本机:"+ip);
                        Bitmap qrCode = CodeUtils.createQRCode(SSLSocketClient.ServerManager.getServerHttpAddress().replaceAll("127.0.0.1",ip), 120, null);
                        erm.setImageBitmap(qrCode);
                        menu.setVisibility(VISIBLE);
                    }
                }else{
                    menu.setVisibility(View.GONE);
                }

                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:   //向下键

                if (isShowHome) return false;
                /*    实际开发中有时候会触发两次，所以要判断一下按下时触发 ，松开按键时不触发
                 *    exp:KeyEvent.ACTION_UP
                 */
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    Log.d(TAG, "down--->");
                    PlayerController.getInstance().playNextFolder();
                }

                break;

            case KeyEvent.KEYCODE_DPAD_UP:   //向上键
                if (isShowHome) return false;

                Log.d(TAG, "up--->");
                PlayerController.getInstance().prev();
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT: //向左键
                if (isShowHome) {
                    menuPanel.onKeyDown(keyCode, event);
                    return false;
                }

                Log.d(TAG, "left--->");

                return videoView.onKeyDown(keyCode);


            case KeyEvent.KEYCODE_DPAD_RIGHT:  //向右键
                if (isShowHome) {
                    menuPanel.onKeyDown(keyCode, event);
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
        unregisterReceiver(receiver);
        videoView.release();
        super.onDestroy();

    }

}
