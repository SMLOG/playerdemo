package com.usbtv.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.danikula.videocache.HttpProxyCacheServer;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.usbtv.demo.data.DatabaseHelper;
import com.usbtv.demo.data.ResItem;
import com.usbtv.demo.view.MyVideoView;

import org.json.JSONObject;

import java.io.File;
import java.net.InetAddress;

public class App extends Application {
    public static final String URLACTION = "urlaction";
    public static final String exit = "exit";
    public static final String TAG = "demo";
    public static ResItem curResItem;
    private static DatabaseHelper databaseHelper = null;

    public static String host;
    protected static PlayList playList = new PlayList();
    private static Status status;

    private Context mContext;
    private static App self;
    private ServerManager mServer;

    protected static MyVideoView videoView;

    private static boolean mediaMounted =false;

    public static App getInstance() {
        return self;
    }

    public static HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    public static MyVideoView getVideoView() {
        return videoView;
    }

    public static void setVideoView(MyVideoView videoView) {
        App.videoView = videoView;
    }

    public synchronized static boolean isMediaMounted() {
        return mediaMounted;
    }

    public synchronized static void setMediaMounted(boolean mediaMounted) {
        App.mediaMounted = mediaMounted;
    }

    public static void updateCacheFolder(File file) {
        App app = getInstance();
        app.proxy = null;
        if (file != null && file.canWrite()) {
            app.proxy = new HttpProxyCacheServer.Builder(app)

                    .cacheDirectory(file)
                    // .maxCacheSize(1024 * 1024 * 1024)
                    .build();
        } else app.proxy = app.newProxy();

    }

    public static void sendExit() {

        Intent intent = new Intent();
        intent.setAction(App.exit);

        App.getInstance().getApplicationContext().sendBroadcast(intent);
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(1024*1024*1024)
                .build();
    }

    public static boolean  sendPlayBroadCast(int aIndex,int bIndex){

            if(aIndex<0){
                SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
                aIndex  = sp.getInt("aIndex",0);
                bIndex = sp.getInt("bIndex",0);
            }

        Intent intent = new Intent();
        intent.setAction(App.URLACTION);
        intent.putExtra("aIndex", aIndex);
        intent.putExtra("bIndex", bIndex);
        Log.d("deom",""+System.currentTimeMillis());
        App.getInstance().getApplicationContext().sendBroadcast(intent);
        return true;
    }


    public static String getProxyUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            if (App.proxy == null) {
                proxy = getInstance().newProxy();
            }
            return App.proxy.getProxyUrl(url);

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
        Log.d(TAG, ipaddr.getHostAddress());
        DowloadPlayList.loadPlayList(true);
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

        SharedPreferences sp = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        String statusJsonstr = sp.getString("status",null);
        if(statusJsonstr!=null&&!statusJsonstr.equals("")){
            Status  status = JSON.parseObject(statusJsonstr,Status.class);
            App.status = status;
        }else App.status = new Status();
    }

}
