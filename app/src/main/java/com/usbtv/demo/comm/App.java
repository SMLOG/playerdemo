package com.usbtv.demo.comm;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.danikula.videocache.HttpProxyCacheServer;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.PlayerController;
import com.usbtv.demo.data.CatType;
import com.usbtv.demo.data.DatabaseHelper;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.sync.SyncCenter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class App extends Application{
    public static final String URLACTION = "urlaction";
    public static final String CMD = "cmd";
    public static final String TAG = "demo";
    public static MediaPlayer bgMedia;
    private static DatabaseHelper databaseHelper = null;

    public static String host;

    private Context mContext;
    private static App self;
    private SSLSocketClient.ServerManager mServer;

    public static List<Drive> diskList = new ArrayList<Drive>();

    public static App getInstance() {
        return self;
    }

    public static Map<String, Integer> getStoreTypeMap() {

        SharedPreferences sp = getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);

        Map<String, Integer>   map=null;
        String jsonStr = sp.getString("typesMap", "");
        if (!jsonStr.equals("")) {
            Map<String, Integer> dummyMap = JSON.parseObject(jsonStr, LinkedHashMap.class, Feature.OrderedField);

            map= dummyMap;
        }else
        {
            map = new LinkedHashMap<>();
        }
        map.put("Favorite",0);
        return map;
    }



    public void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] { new TrustAllTrustManager() };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcastCMD(String cmd, String val) {

        Intent intent = new Intent();
        intent.setAction("cmd");
        intent.putExtra("cmd", cmd);
        intent.putExtra("val", val);
        App.getInstance().getApplicationContext().sendBroadcast(intent);
    }
    public static Uri getUri(VFile vf) {

        String vremote = SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/vfile?id=" + vf.getId();

        String path = vf.getAbsPath();

        if (path == null || !new File(path).exists())
            for (Drive d : App.diskList) {
                vf.getFolder().setRoot(d);
                if (vf.exists() && new File(vf.getAbsPath()).canRead()
                ) {
                    try {
                        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);

                        folderDao.update(vf.getFolder());

                        //  path = vf.getAbsPath();
                        break;

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                }
            }

        if (!vf.exists()) {

            String dlink = vf.getdLink();
            if (dlink != null && dlink.indexOf(".m3u8") > -1) {

                // vremote = "http://127.0.0.1:8080/api/r/"+ URLEncoder.encode(vf.getFolder().getName())+"/"+vf.getOrderSeq() +"/index.m3u8?url="+URLEncoder.encode(vf.getdLink());
                //if(true)return Uri.parse("http://192.168.0.101/32.m3u8?t="+System.currentTimeMillis());

                String rate = PlayerController.getInstance().getRate();
                if (dlink.startsWith(":/")) return Uri.parse(SSLSocketClient.ServerManager.getServerHttpAddress()+dlink+"&rate="+ rate);

                if (true) {

                   App.getInstance().player(3);

                    return Uri.parse(dlink);

                }

                if (true) {
                    return Uri.parse(
                            SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/m3u8proxy/" + dlink
                    );
                }
                return Uri.parse(
                        SSLSocketClient.ServerManager.getServerHttpAddress() + "/api/r/" + vf.getFolder().getId()
                                + "/" + vf.getOrderSeq() + "/index.m3u8"
                                + "?t=" + System.currentTimeMillis()
                );


            }


        } else {
            path = vf.getAbsPath();
            if (new File(path).exists()) {
                vremote = "file://" + path;
            }
        }
        System.out.println(vremote);
        //return Uri.parse(vremote);
        return Uri.parse(vremote);
    }

    private static int playerType=0;
    private void player(int i) {
        if(mContext!=null && playerType!=i){
            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
            SharedPreferences.Editor ed = mSharedPreferences.edit();
            ed.putString("pref.player",""+i);
            ed.commit();
            playerType = i;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        this.mContext = getApplicationContext();

        this.createAndStartWebServer(mContext);

        syncWithRemote();
        trustAllCertificates();
    }


    public void syncWithRemote() {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        SyncCenter.syncData(null);

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                PlayerController.getInstance().refreshCats();
                              //  new InitChannel();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }

                        }
                    });
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

        mServer = new SSLSocketClient.ServerManager(context);
        mServer.startServer();

    }

    public static Drive getDefaultRootDrive() {

        if (diskList.size() == 0) initDisks();
        return diskList.get(diskList.size() - 1);
    }



    public static synchronized void initDisks() {
        diskList.clear();
        List<Drive> drives = Utils.getSysAllDriveList();
        diskList.addAll(drives);


    }

    public static synchronized Drive getDefaultRemoveableDrive() {
        for (Drive drive : diskList) {
            if (drive.isRemoveable()) return drive;
        }
        return null;
    }


    public static String cache2Disk(VFile vfile, String url) {
        HttpGet oInstance = new HttpGet();

        if (App.getDefaultRemoveableDrive() == null) {
            App.initDisks();
        }


        if (url != null && App.getDefaultRemoveableDrive() != null) {

            for (Drive d : App.diskList) {
                vfile.getFolder().setRoot(d);
                if (vfile.exists() && new File(vfile.getAbsPath()).canRead()
                ) {
                    try {
                        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);

                        folderDao.update(vfile.getFolder());
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                    return "file://" + vfile.getAbsPath();
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

                    File[] files = App.getInstance().getApplicationContext().getExternalFilesDirs(null);

                    String dest = vfile.getAbsPath();

                    if(false) {
                        String prefix = vfile.getAbsPath().split("videos")[0];
                        String path = vfile.getAbsPath().split("videos")[1];
                        dest = "/storage/44C4-1615/Android/data/com.usbtv.demo.exo/files" + path;//vfile.getAbsPath().replaceAll("videos","Download/videos");

                        for (File file : files) {
                            if (file != null && file.getAbsolutePath().startsWith(prefix)) {
                                dest = file.getAbsolutePath() + path;
                                break;
                            }
                        }
                    }
                    ///storage/44C4-1615/Android/data/com.usbtv.demo.exo/files
                    oInstance.addItem(vfile, finalUrl, dest);
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
        if (!file.getParentFile().exists()) {
            if (DocumentsUtils.mkdirs(this, file.getParentFile())) {
                Log.i(TAG, "创建文件夹：" + file.getParentFile().getAbsolutePath());
            } else {
                Log.i(TAG, "创建文件夹失败：" + file.getParentFile().getAbsolutePath());
            }

        }

        String fileWritePath = filePath;
        File fileWrite = new File(fileWritePath);


        OutputStream outputStream = DocumentsUtils.getOutputStream(this, fileWrite);  //获取输出流
        //Toast.makeText(this,"路径：" + fileWritePath + "成功",Toast.LENGTH_SHORT ).show();

        return outputStream;

    }


    public static InputStream documentInputStream(File file2) {

        return DocumentsUtils.getInputStream(App.getInstance().getApplicationContext(), file2);  //获取输出流

    }

    public static String getUrl( String link) {
        if(false){
            return SSLSocketClient.ServerManager.getServerHttpAddress()+"/api/speed.m3u8?url="+ URLEncoder.encode(link);
        }
        return link;
    }

    private static HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                //.maxCacheSize(1024*1024*300)
                .build();
    }

    public static String getProxyUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            if (App.proxy == null) {
                proxy = getInstance().newProxy();
            }
            return proxy.getProxyUrl(url);
        }
        return url;

    }

    private static Dao<Folder, Integer> folderDao = null;
    private static Dao<VFile, Integer> vFileDao = null;
    private static Dao<CatType,Integer> catTypeDao =null;

    public static Dao<Folder, Integer> getFolderDao(){
        if (folderDao == null) {
            try {
                folderDao = App.getHelper().getDao(Folder.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return folderDao;

    }
    public static Dao<VFile, Integer> getVFileDao(){
            try {
                if (vFileDao == null) vFileDao = App.getHelper().getDao(VFile.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        return vFileDao;

    }

    public static Dao<CatType, Integer> getCatTypeDao(){
        try {
            if (catTypeDao == null) catTypeDao = App.getHelper().getDao(CatType.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return catTypeDao;

    }

}
