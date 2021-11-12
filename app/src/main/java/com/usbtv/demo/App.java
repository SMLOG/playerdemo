package com.usbtv.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.HttpGet;
import com.usbtv.demo.comm.NetUtils;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.DatabaseHelper;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.r.InitChannel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class App extends Application implements CacheListener {
    public static final String URLACTION = "urlaction";
    public static final String CMD = "cmd";
    public static final String TAG = "demo";
    public static MediaPlayer bgMedia;
    private static DatabaseHelper databaseHelper = null;

    public static String host;

    private Context mContext;
    private static App self;
    private ServerManager mServer;

    protected static List<Drive> diskList = new ArrayList<Drive>();


    private static boolean mediaMounted =false;

    public static App getInstance() {
        return self;
    }

    public static LinkedHashMap<String, String>  getTypesMap() {
        return typesMap;
    }
    public static LinkedHashMap<String, String> getAllTypeMap() {

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("全部","");
        map.putAll(typesMap);

        map.put("电视电影","1");
        map.put("其他","0");
        return map;
    }

    public static HttpProxyCacheServer proxy;

    public static LinkedHashMap<String,String> typesMap = new LinkedHashMap<>();

    public static HttpProxyCacheServer getProxy() {
        App app = (App) App.getInstance().getApplicationContext().getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    public static void broadcastCMD(String cmd, String val) {

        Intent intent = new Intent();
        intent.setAction("cmd");
        intent.putExtra("cmd", cmd);
        intent.putExtra("val", val);
        App.getInstance().getApplicationContext().sendBroadcast(intent);
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(1024*1024*200)
                .build();
    }


    public static String getProxyUrl(String url) {
        if ( url.startsWith("http://") || url.startsWith("https://")) {
            if (App.proxy == null) {
                proxy = getInstance().newProxy();
            }
            return proxy.getProxyUrl(url);

        }
        return url;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        this.mContext = getApplicationContext();

        this.createAndStartWebServer(mContext);


        InetAddress ipaddr = NetUtils.getLocalIPAddress();
       // Log.d(TAG, ipaddr.getHostAddress());
        //DowloadPlayList.loadPlayList(true);


        syncWithRemote();
    }

    public void syncWithRemote() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    DownloadMP.process();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        List<Folder> movies = null;
                        try {
                            movies = App.getHelper().getDao(Folder.class).queryForAll();
                            MainActivity.moviesRecyclerViewAdapter.update(movies);
                            new InitChannel();

                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }

                    }});
            }
        }).start();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
    public static DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(App.getInstance(), DatabaseHelper.class);
        }
        return databaseHelper;
    }
    private void createAndStartWebServer(Context context) {

        mServer = new ServerManager(context);
        mServer.startServer();

    }

    public static Drive getDefaultRootDrive(){

        if(diskList.size()==0 ) initDisks();
        return diskList.get(diskList.size() - 1);
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }

    public static synchronized void initDisks(){
        diskList.clear();
        List<Drive> drives = Utils.getSysAllDriveList();
        diskList.addAll(drives);


    }

    public static synchronized Drive getDefaultRemoveableDrive(){
        for (Drive drive:diskList){
            if(drive.isRemoveable())return drive;
        }
        return null;
    }



    public static String cache2Disk(VFile vfile, String url) {
        HttpGet oInstance = new HttpGet();

        if( App.getDefaultRemoveableDrive()==null){
            App.initDisks();
        }


        if (url != null&&App.getDefaultRemoveableDrive()!=null) {

            for(Drive d:App.diskList){
                vfile.getFolder().setRoot(d);
                if(vfile.exists() && new File(vfile.getAbsPath()).canRead()
                ){
                    try {
                        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);

                        folderDao.update(vfile.getFolder());
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                    return  "file://" + vfile.getAbsPath();
                }
            }
            String finalUrl = url;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //String proxyUrl = App.getProxyUrl("http://127.0.0.1:8080/api/vfile?id=" + id);

                    if (vfile.getFolder().getRoot() == null) {
                        vfile.getFolder().setRoot(App.getDefaultRemoveableDrive());

                    }

                    oInstance.addItem(vfile, finalUrl, vfile.getAbsPath());
                    oInstance.downLoadByList();
                    try {
                        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);

                        folderDao.update(vfile.getFolder());
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }).start();

            return url;
        }
        return url;
    }

    public OutputStream documentStream(String filePath) throws IOException {

       // Log.i(TAG, filePath);
        File file = new File(filePath);
        if (!file.getParentFile().exists()){
            if(DocumentsUtils.mkdirs(this,file.getParentFile())){
                Log.i(TAG,"创建文件夹：" + file.getParentFile().getAbsolutePath());
            }else{
                Log.i(TAG,"创建文件夹失败：" + file.getParentFile().getAbsolutePath());
            }

        }

        String  fileWritePath = filePath ;
        File fileWrite = new File(fileWritePath);


        OutputStream outputStream = DocumentsUtils.getOutputStream(this,fileWrite);  //获取输出流
        //Toast.makeText(this,"路径：" + fileWritePath + "成功",Toast.LENGTH_SHORT ).show();

        return outputStream;

    }


    public static InputStream documentInputStream(File file2) {

        return DocumentsUtils.getInputStream(App.getInstance().getApplicationContext(),file2);  //获取输出流

    }


}
