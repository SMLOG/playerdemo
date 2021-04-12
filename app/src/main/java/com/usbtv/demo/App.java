package com.usbtv.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;
import java.net.InetAddress;

public class App extends Application {
    public static final String URLACTION = "urlaction";
    public static final String TAG ="usbtv.demo" ;
    public static String host;
    protected static PlayList playList = new PlayList();

    private Context mContext;
    private static App self;
    private ServerManager mServer;

    private static boolean mediaMounted =false;

    public static App getInstance() {
        return self;
    }

    private static HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
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

        App.getInstance().getApplicationContext().sendBroadcast(intent);
        return true;
    }
    public static String getProxyUrl(String url) {
      /*  if (url.startsWith("http://") || url.startsWith("https://")) {
            if (App.proxy == null) {
                proxy = getInstance().newProxy();
            }
            return App.proxy.getProxyUrl(url);

        }*/
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
    }

    private void createAndStartWebServer(Context context) {

        mServer = new ServerManager();
        mServer.startServer();



    }

}
