package com.usbtv.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
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
import com.usbtv.demo.news.NewsStarter;
import com.usbtv.demo.r.InitChannel;
import com.usbtv.demo.vurl.M3U;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static App getInstance() {
        return self;
    }

    public static Map<String, String>  getTypesMap() {

        if(typesMap==null){
           typesMap = new LinkedHashMap<>();
        }

        return typesMap;
    }

    public static void saveTypesMap() {

        SharedPreferences sp = getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);

       String jsonStr = JSON.toJSONString(typesMap);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("typesMap",jsonStr);
        ed.commit();

    }

    public static Map<String, String> getAllTypeMap(boolean b) {

        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        SharedPreferences sp = getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);

        String jsonStr = sp.getString("typesMap", "");
        if(!jsonStr.equals("")){
            Map<String, String> dummyMap =  JSON.parseObject(jsonStr,LinkedHashMap.class, Feature.OrderedField);
             map.putAll(dummyMap);
        }

       // map.put("电视电影","1");
        map.put("直播","2");
        map.put("CNN2","4");
        map.put("CNN","3");
        map.put("其他","0");

        return map;
    }

    public static HttpProxyCacheServer proxy;

    public static Map<String,String> typesMap = null;

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

    public static StringBuilder updateM3U(boolean force) throws InterruptedException, IOException {


        SharedPreferences sp = App.getInstance().getSharedPreferences("SP", Context.MODE_PRIVATE);


        String lastUpdateM3U = "lastUpdateM3U";
        if(force||System.currentTimeMillis()-sp.getLong(lastUpdateM3U,0l)>3600*24*15){
            String[] filePaths= new String[]{
                    "/storage/36AC6142AC60FDAD/m3u/channels/us.m3u",
                    "/storage/36AC6142AC60FDAD/m3u/channels/uk.m3u"
            };

            String filePath2="/storage/36AC6142AC60FDAD/m3u/channels/us_checked.m3u";

            StringBuilder sb = M3U.check(filePaths);
            OutputStream outputStream2 = App.getInstance().documentStream(filePath2);
            outputStream2.write(sb.toString().getBytes());
            outputStream2.close();

            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(lastUpdateM3U, System.currentTimeMillis());
            editor.apply();
            sp.edit().commit();

            return sb;


        }

        return null;


    }

    public static Uri getUri(VFile vf) {

        String vremote = ServerManager.getServerHttpAddress()+"/api/vfile?id=" + vf.getId();

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

            if(vf.getdLink()!=null&&vf.getdLink().indexOf(".m3u8")>-1){

                // vremote = "http://127.0.0.1:8080/api/r/"+ URLEncoder.encode(vf.getFolder().getName())+"/"+vf.getOrderSeq() +"/index.m3u8?url="+URLEncoder.encode(vf.getdLink());
                //if(true)return Uri.parse("http://192.168.0.101/32.m3u8?t="+System.currentTimeMillis());

                //if(true)return Uri.parse(vf.getdLink());
                if(true){
                    return Uri.parse(
                            ServerManager.getServerHttpAddress()+"/api/m3u8proxy/"+vf.getdLink()
                    );
                }
                return Uri.parse(
                        ServerManager.getServerHttpAddress()+"/api/r/"+ vf.getFolder().getId()
                                +"/"+vf.getOrderSeq()+"/index.m3u8"
                                +"?t="+System.currentTimeMillis()
                );


            }else {
                com.alibaba.fastjson.JSONObject vidoInfo = DownloadMP.getVidoInfo(vf.getFolder().getBvid(), vf.getPage());
                if (vidoInfo != null && null != vidoInfo.getString("video")) {
                    vremote = vidoInfo.getString("video");
                    vremote = App.cache2Disk(vf, vremote);
                }
            }


        } else {
            path = vf.getAbsPath();
            if (new File(path).exists()) {
                vremote = "file://" + path;
            }
        }
        System.out.println(vremote);
        return Uri.parse(App.getProxyUrl(vremote));
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

        SharedPreferences sp = getSharedPreferences("SP", Context.MODE_PRIVATE);


        if(System.currentTimeMillis()-sp.getLong("lastSyncWithRemoteTime",0l)>3600*24)
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {



                    DownloadMP.syncData();
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putLong("lastSyncWithRemoteTime", System.currentTimeMillis());
                    editor.apply();
                    sp.edit().commit();

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

        try {
            NewsStarter.run();
        } catch (Throwable e) {
            e.printStackTrace();
        }


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
