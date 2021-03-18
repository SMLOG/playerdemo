package com.llw.androidtvdemo;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyApplication extends Application  {
    protected static List<String> urls = new ArrayList<String>();

    private Context mContext;
    private String TAG = "key";
    private static String DATAFILENAME="data.txt";
    private static MyApplication self;
    private ServerManager mServer;

    public static MyApplication getInstance(){
        return self;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        this.mContext =getApplicationContext();

        this.createAndStartWebServer(mContext);

        File urlsFile = new File(mContext.getFilesDir(), DATAFILENAME);
        try {
            urlsFile.createNewFile();
            FileInputStream in= new FileInputStream(urlsFile);
            BufferedReader br=new BufferedReader(new InputStreamReader(in));
            do{
                String line = br.readLine();
                if(line==null){
                    br.close();
                    in.close();
                    break;
                }
                urls.add(line.trim());
            }while(true);


        } catch ( IOException e) {
            Log.d( TAG, "files err:"+e.getMessage() );
        }
      //  if(urls.size()==0){
            urls.add("https://upos-sz-mirrorkodo.bilivideo.com/upgcxcode/30/93/272399330/272399330-1-208.mp4?e=ig8euxZM2rNcNbeghwdVhoMHhbdVhwdEto8g5X10ugNcXBMvNC8xNbLEkF6MuwLStj8fqJ0EkX1ftx7Sqr_aio8_&uipk=5&nbs=1&deadline=1616047844&gen=playurl&os=kodobv&oi=2071795667&trid=60bd210bb6f84eac9efbbc145d56497bT&platform=html5&upsig=7d46e9ddcd16807a453b4cd4b800736d&uparams=e,uipk,nbs,deadline,gen,os,oi,trid,platform&mid=0&orderid=0,1&logo=80000000");
      //  }

        InetAddress ipaddr =  NetUtils.getLocalIPAddress();
        Log.d(TAG,ipaddr.getHostAddress());
    }

    private void createAndStartWebServer(Context context){

        mServer = new ServerManager();
        mServer.startServer();

        /*
        AssetManager assetManager = getAssets();
        WebSite webSite = new AssetsWebsite(assetManager, "");
        AndServer.serverBuilder();
        AndServer andServer = new AndServer.Build()
                .website(webSite)
                .port(8080)
                .timeout(10 * 1000)
                .registerHandler("urls", this)
                .build();
        Server server= andServer.createServer();
        server.start();
        */

    }
/*
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parse(request);
        String act = params.get("act");
        String url = params.get("url");
        if("add".equals(act)){
            if(url!=null&&!"".equals(url.trim())){
                urls.add(url);
                StringEntity stringEntity = new StringEntity("add "+url+" Succeed! "+urls.size(), "utf-8");
                response.setEntity(stringEntity);

                try {
                    FileOutputStream outputStream = mContext.openFileOutput( DATAFILENAME, Context.MODE_PRIVATE|Context.MODE_APPEND );
                    outputStream.write( (url+"\n").getBytes() );
                    outputStream.close();
                } catch (IOException e) {
                    Log.d( TAG, "outputStream err:"+e.getMessage() );
                }
            }
        }


    }*/
}
