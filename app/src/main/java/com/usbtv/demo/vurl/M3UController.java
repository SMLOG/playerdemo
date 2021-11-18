package com.usbtv.demo.vurl;


import com.usbtv.demo.App;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.HttpResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Interceptor
public class M3UController {


    @GetMapping(path = "/api/m3u")
    String m3u(
            @RequestParam(name = "s", required = false, defaultValue = "0") int showSummary,
            HttpResponse response
    ) throws IOException, InterruptedException {

        StringBuilder outSb = new StringBuilder();
        outSb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"></head><body>");


        String filePath="/storage/36AC6142AC60FDAD/m3u/channels/us.m3u";
        String filePath2="/storage/36AC6142AC60FDAD/m3u/channels/us_checked.m3u";
        final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(30);

        FileInputStream fs = new FileInputStream(new File(filePath));
        byte[] bytes = new byte[fs.available()];
        fs.read(bytes);
        fs.close();
        String str = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(str);

        String[] lines = str.split("\n");

        StringBuilder sb =new StringBuilder();

        for(int i=0;i<lines.length;i++ ) {
            String line=lines[i];

            if(line.startsWith("#EXT")) {

                String url = lines[++i];
                fixedThreadPool.submit(getCheckThread(line, url, sb));

            }else{
                sb.append(line).append("\n");
            }
        }

        fixedThreadPool.shutdown();
        while(!fixedThreadPool.isTerminated()) {
            Thread.sleep(3000);
        }
        System.out.println(sb.toString());


        outSb.append("<body></html>");

        OutputStream outputStream2 = App.getInstance().documentStream(filePath2);
        outputStream2.write(sb.toString().getBytes());
        outputStream2.close();

        if(showSummary>1){
            response.setHeader("Content-Type", "text/html");
            return outSb.toString();

        }else {
            response.setHeader("Content-Type", "application/x-mpegURL");
            return sb.toString();
        }
    }
    public static Runnable getCheckThread(String inf,String url,StringBuilder sb) {
        return new Runnable() {

            @Override
            public void run() {
                try{

                    if( checkUrl(url)) {
                        System.out.println("OK:"+url);
                        synchronized (sb) {
                            sb.append(inf).append("\n").append(url).append("\n");
                        }
                        return;
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Not OK:"+url);

            }
        };
    }
    private static boolean checkUrl(String urls) {
        System.out.println(urls);
        int count = 1;
        HttpURLConnection httpURLConnection = null;
        int retryCount=2;
        Map<String, Object> requestHeaderMap = new HashMap<>();

        requestHeaderMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36");
        int timeoutMillisecond = 5*1000;
        while (count <= retryCount) {
            try {
                URL url = new URL(urls);


                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout((int) timeoutMillisecond);
                httpURLConnection.setReadTimeout((int) timeoutMillisecond);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setFollowRedirects(true);

                for (Map.Entry<String, Object> entry : requestHeaderMap.entrySet())
                    httpURLConnection.addRequestProperty(entry.getKey(), entry.getValue().toString());

                if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    String contentType = httpURLConnection.getHeaderField("Content-Type").toLowerCase();
                    System.out.println(contentType);

                    if(contentType.contains("mpegurl")) {

                        String line;
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        boolean isM3u=false;
                        while ((line = bufferedReader.readLine()) != null)
                        {
                            if( !isM3u&&line.indexOf("#EXT")>-1) {
                                isM3u=true;
                            }
                            if(isM3u && !line.startsWith("#")) {
                                line=line.trim();

                                String absUrl = "";
                                if (line.startsWith("/")) {
                                    absUrl = urls.substring(0,urls.indexOf('/',9)) + line;
                                } else if (line.matches("^(http|https)://.+")) {
                                    absUrl = line;
                                } else {
                                    absUrl = urls.substring(0, urls.lastIndexOf("/") + 1) + line;
                                }

                                bufferedReader.close();
                                inputStream.close();
                                httpURLConnection.disconnect();
                                httpURLConnection=null;

                                return checkUrl(absUrl);
                            }



                        }

                        bufferedReader.close();
                        inputStream.close();

                    }else if(contentType.contains("mp2t")) {
                        InputStream inputStream = httpURLConnection.getInputStream();

                        byte[] buf = new byte[1024];
                        while (inputStream.read(buf)>-1) {

                        }
                        return true;
                    }

                }


                return false;
            } catch (Exception e) {
                //	e.printStackTrace();
                //  Log.d("第" + count + "获取链接重试！\t" + urls);
                count++;
//                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
        }

        return false;
    }


}
