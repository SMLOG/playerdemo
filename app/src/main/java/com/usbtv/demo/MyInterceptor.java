package com.usbtv.demo;

import androidx.annotation.NonNull;

import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.Interceptor;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.Resolver;
import com.yanzhenjie.andserver.framework.ExceptionResolver;
import com.yanzhenjie.andserver.framework.HandlerInterceptor;
import com.yanzhenjie.andserver.framework.body.StreamBody;
import com.yanzhenjie.andserver.framework.handler.MethodHandler;
import com.yanzhenjie.andserver.framework.handler.RequestHandler;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import download.Log;
import download.M3u8Exception;

@Resolver
public class MyInterceptor implements ExceptionResolver {


    @Override
    public void onResolve(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull Throwable e) {
        System.out.println("OK");
        if("HEAD".equalsIgnoreCase(request.getMethod().toString())){
            response.setHeader("content-type","video/mp2t");
            response.setHeader("access-control-allow-headers","X-Requested-With");
            response.setHeader("access-control-allow-methods","POST, GET, OPTIONS");
            response.setHeader("access-control-allow-origin","*");
            response.setHeader(" Content-Length","381681");

        }
        if(request.getURI().contains("/api/m3u8proxy")){
            String url =  request.getURI().split("/api/m3u8proxy/")[1];
            try {
                url = url.replace(":/","://");
                m3u8Proxy(request,response,url);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }


    }


   // @GetMapping(path = "/api/m3u8proxy")
    ResponseBody m3u8Proxy(
            HttpRequest request, HttpResponse response,
            @RequestParam(name = "url") String url
    ) throws Exception {

        int count = 1;
        int retryCount = 10;
        HttpURLConnection httpURLConnection = null;
        long timeoutMillisecond = 100000L;

        HashMap<String, String> requestHeaderMap = new HashMap<String, String>();
        requestHeaderMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36");
        String contentType = null;


        InputStream in = null;

        //重试次数判断
        while (count <= retryCount) {
            try {
                httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                httpURLConnection.setConnectTimeout((int) timeoutMillisecond);
                for (Map.Entry<String, String> entry : requestHeaderMap.entrySet())
                    httpURLConnection.addRequestProperty(entry.getKey(), entry.getValue().toString());
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setReadTimeout((int) timeoutMillisecond);
                httpURLConnection.setDoInput(true);

                contentType = httpURLConnection.getHeaderField("content-type");
                in = httpURLConnection.getInputStream();

                ByteArrayOutputStream byos = new ByteArrayOutputStream();
                int len = 0;
                byte[] bytes = new byte[4096];
                while ((len = in.read(bytes)) != -1) {
                    byos.write(bytes, 0, len);
                }
                in.close();

                bytes = new byte[byos.size()];
                System.arraycopy(byos.toByteArray(), 0, bytes, 0, bytes.length);
                byos.close();

                if (contentType.toLowerCase().contains("mpegurl")) {
                    String[] lines = new String(bytes).split("\n");
                    StringBuilder sb = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith("#")) {
                            sb.append(line);
                        } else {
                            String absUrl = "";

                            if (line.startsWith("/")) {
                                absUrl = url.substring(0,url.indexOf('/',9)) + line;
                            } else if (line.matches("^(http|https)://.+")) {
                                absUrl = line;
                            } else {
                                absUrl = url.substring(0, url.lastIndexOf("/") + 1) + line;
                            }
                            sb.append(ServerManager.getServerHttpAddress()).append("/api/m3u8proxy/" + absUrl);
                        }

                        sb.append("\n");

                    }
                    bytes = sb.toString().getBytes();
                }

                Map<String, List<String>> hfs = httpURLConnection.getHeaderFields();
                for (String name : hfs.keySet()) {
                     //if(name!=null && !name.equalsIgnoreCase("content-length")){
                      //   response.setHeader(name, httpURLConnection.getHeaderField(name));
                     //}
                }

                StreamBody responseBody = new StreamBody(new ByteArrayInputStream(bytes), bytes.length, MediaType.parseMediaType(contentType));

                response.setBody(responseBody);
                return responseBody;

            } catch (Exception e) {
                e.printStackTrace();

                Log.d("第" + count + "获取链接重试！\t" + url);
                count++;

            } finally {

                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

            }
        }

        if (count > retryCount)
            throw new M3u8Exception("连接超时！");
        return null;
    }
}
