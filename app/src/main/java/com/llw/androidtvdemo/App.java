package com.llw.androidtvdemo;

import android.app.Application;
import android.content.Context;
import android.util.Log;


import java.net.InetAddress;

public class App extends Application  {
    public static final String URLACTION ="urlaction" ;
    protected static PlayList playList = new PlayList();

    private Context mContext;
    private String TAG = "key";
    private static String DATAFILENAME="data.txt";
    private static App self;
    private ServerManager mServer;
    protected static int curIndex=0;

    public static App getInstance(){
        return self;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        this.mContext =getApplicationContext();

        this.createAndStartWebServer(mContext);
        for(VideoItem item:DbHelper.getActiveList()){
            playList.add(item);
        }
        /*
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
        }*/
      //  if(urls.size()==0){
           // urls.add("https://upos-sz-mirrorkodo.bilivideo.com/upgcxcode/30/93/272399330/272399330-1-208.mp4?e=ig8euxZM2rNcNbeghwdVhoMHhbdVhwdEto8g5X10ugNcXBMvNC8xNbLEkF6MuwLStj8fqJ0EkX1ftx7Sqr_aio8_&uipk=5&nbs=1&deadline=1616075626&gen=playurl&os=kodobv&oi=2071795667&trid=286ec2a027224ddd9941d2f2e690d2e1T&platform=html5&upsig=b480538a885ff5b32ee5c5396be1d0e2&uparams=e,uipk,nbs,deadline,gen,os,oi,trid,platform&mid=0&orderid=0,1&logo=80000000");
           // urls.add("https://upos-sz-mirrorcos.bilivideo.com/upgcxcode/73/44/272784473/272784473-1-208.mp4?e=ig8euxZM2rNcNbeghwdVhoMHhbdVhwdEto8g5X10ugNcXBMvNC8xNbLEkF6MuwLStj8fqJ0EkX1ftx7Sqr_aio8_&uipk=5&nbs=1&deadline=1616068591&gen=playurl&os=cosbv&oi=2071795668&trid=63ff7be59c8b476399f17e6c9df4fc3bT&platform=html5&upsig=bb64c663a712abd5fcd2a1a2e1687bc2&uparams=e,uipk,nbs,deadline,gen,os,oi,trid,platform&mid=0&orderid=0,1&logo=80000000");
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
