package com.llw.androidtvdemo;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebSettings;


import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.FileNameGenerator;
import com.danikula.videocache.file.Md5FileNameGenerator;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class App extends Application {
    public static final String URLACTION = "urlaction";
    protected static PlayList playList = new PlayList();

    private Context mContext;
    private String TAG = "key";
    private static String DATAFILENAME = "data.txt";
    private static App self;
    private ServerManager mServer;
    protected static int curIndex = 0;
    private UrlServiceApi urlServiceApi;

    public static App getInstance() {
        return self;
    }

    private static HttpProxyCacheServer proxy;

    private static OkHttpClient getOkHttpClient() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .removeHeader("User-Agent")//移除旧的
                                .addHeader("User-Agent", WebSettings.getDefaultUserAgent(App.getInstance()))//添加真正的头部
                                .build();
                        return chain.proceed(request);
                    }
                }).build();
        return httpClient;
    }

    public static UrlServiceApi initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(getOkHttpClient())
                .baseUrl("https://www.ibilibili.com/")
                .build();
        UrlServiceApi urlServiceApi = retrofit.create(UrlServiceApi.class);
        return urlServiceApi;
    }

    public static UrlServiceApi getUrlServiceApi() {
        if (App.getInstance().urlServiceApi == null)
            return App.getInstance().urlServiceApi = initRetrofit();
        else return App.getInstance().urlServiceApi;

    }


    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
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
                /*.fileNameGenerator(new FileNameGenerator() {
                    private Md5FileNameGenerator md5 = new Md5FileNameGenerator();
                    @Override
                    public String generate(String url) {
                        url = url.replace("http://localhost:8080/api/get?url=","");
                        return md5.generate(url);
                    }
                })*/
                //  .cacheDirectory()
                // .maxCacheSize(1024 * 1024 * 1024)
                .build();
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
        for (VideoItem item : DbHelper.getActiveList()) {
            playList.add(item);
        }

        Call<ResponseBody> call = getUrlServiceApi().getParams("http://www.ibilibili.com/video/BV1SJ411K76h?from=search&seid=434365872046738989");
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                try {
                    String body = response.body().string();
                    Pattern pattern = Pattern.compile("data: \\{(.*?)\\}", Pattern.DOTALL | Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(body);
                    if (matcher.find()) {
                        String[] arr = matcher.group(1)
                                .replaceAll("\"", "")
                                .replaceAll("\\s+", "")
                                .split(",");


                        System.out.println(arr);

                        Call<ResponseBody> call2 = getUrlServiceApi()
                                .getData("http://bilibili.applinzi.com/index.php"
                                        , arr[0].split(":")[1]
                                        , arr[1].split(":")[1]
                                        , arr[2].split(":")[1]
                                );
                        call2.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                                try {
                                    String body = response.body().string();
                                    System.out.println(body);
                                } catch (Throwable tt) {

                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                t.printStackTrace();

                            }
                        });
                    }
                } catch (Throwable t) {

                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                t.printStackTrace();

            }
        });

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

        InetAddress ipaddr = NetUtils.getLocalIPAddress();
        Log.d(TAG, ipaddr.getHostAddress());
    }

    private void createAndStartWebServer(Context context) {

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
