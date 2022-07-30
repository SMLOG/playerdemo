package com.usbtv.demo.proxy;


import com.yanzhenjie.andserver.annotation.Controller;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.StreamBody;
import com.yanzhenjie.andserver.framework.body.StringBody;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import download.Log;
import download.M3u8Exception;

@RestController
public class ProxyController {

    private String getAbsUrl(String fromUrl, String relativeUrl) {
        String absUrl;
        if (relativeUrl.startsWith("/")) {
            absUrl = fromUrl.substring(0,fromUrl.indexOf('/',9)) + relativeUrl;
        } else if (relativeUrl.matches("^(http|https)://.+")) {
            absUrl = relativeUrl;
        } else {
            absUrl = fromUrl.substring(0, fromUrl.lastIndexOf("/") + 1) + relativeUrl;
        }
        return absUrl;
    }
    @GetMapping(path = "/api/speed.m3u8")
    ResponseBody speed(
            HttpRequest request, HttpResponse response,
            @RequestParam(name = "url") String url
    ) throws Exception {

        int count = 1;
        int retryCount = 3;
        HttpURLConnection httpURLConnection = null;
        long timeoutMillisecond = 100000L;

        HashMap<String, String> requestHeaderMap = new HashMap<String, String>();
        requestHeaderMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36");
        String contentType = null;


        InputStream in = null;
        if (url.indexOf("m3u8") == -1) {
            CacheItem item = MemCacheManager.curTsUrl(url);
            if (item != null) {

                Map<String, List<String>> hfs = item.headers;
                for (String name : hfs.keySet()) {
                    if (name != null && !name.equalsIgnoreCase("content-length")) {
                        response.setHeader(name, hfs.get(name).get(0));
                    }
                }

                StreamBody responseBody = new StreamBody(new ByteArrayInputStream(item.data), item.data.length, MediaType.parseMediaType(item.contentType));

                response.setBody(responseBody);
                return responseBody;

            }
            return null;
        }
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
                byte[] bytes = new byte[8192];
                while ((len = in.read(bytes)) != -1) {
                    byos.write(bytes, 0, len);
                }
                in.close();

                bytes = new byte[byos.size()];
                System.arraycopy(byos.toByteArray(), 0, bytes, 0, bytes.length);
                byos.close();
                String str = new String(bytes);
                boolean widthproxy = str.contains(".m3u8");
                boolean withEnd = str.contains("#EXT-X-ENDLIST");
                StringBuilder sb = new StringBuilder();
                Pattern pattern = Pattern.compile(",URI=\"(.*?)\"", Pattern.DOTALL);
                ArrayList<String> tsUrls = new ArrayList<String>();

                if (contentType.toLowerCase().contains("mpegurl")) {
                    String[] lines = str.split("\n");
                    for (int i=0;i< lines.length;i++) {
                        String line = lines[i];
                        if (line.startsWith("#")) {
                            if(line.startsWith("#EXT-X-I-FRAME-STREAM-INF") || line.startsWith("#EXT-X-MEDIA")) {
                                Matcher m = pattern.matcher(line);
                                if(m.find()) {
                                    String uri = m.group(1);
                                    line = line.replace(uri,  getAbsUrl(url, uri));

                                }
                            }
                            sb.append(line);
                        } else  if(!line.trim().equals("")) {
                            String absUrl = getAbsUrl(url, line);


                            if(i>1) {

                                String pline = lines[i-1];
                                if(widthproxy && pline.startsWith("#EXT-X-STREAM-INF" ) || withEnd&&pline.startsWith("#EXTINF:" )  )
                                    sb.append("/api/speed.m3u8?url=" + URLEncoder.encode(absUrl));
                                else
                                    sb.append(absUrl);

                                if( withEnd&& pline.contains("#EXTINF:")) {
                                    tsUrls.add(absUrl);
                                }
                            }else sb.append(line);

                        }

                        sb.append("\n");

                    }

                    if (tsUrls.size() > 0) {
                        MemCacheManager.buildIndexFrom(url, tsUrls);
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

    @GetMapping(path = "/api/proxy.m3u8")
    ResponseBody cacheProxy(
            HttpRequest request, HttpResponse response,
            @RequestParam(name = "url") String url
    ) throws Exception {

        int count = 1;
        int retryCount = 3;
        HttpURLConnection httpURLConnection = null;
        long timeoutMillisecond = 100000L;

        HashMap<String, String> requestHeaderMap = new HashMap<String, String>();
        requestHeaderMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36");
        String contentType = null;

        System.out.println(url);

        InputStream in = null;

        if (url.indexOf("m3u8") == -1) {
            CacheItem item = MemCacheManager.curTsUrl(url);
            if (item != null) {

                Map<String, List<String>> hfs = item.headers;
                for (String name : hfs.keySet()) {
                    if (name != null && !name.equalsIgnoreCase("content-length")) {
                        response.setHeader(name, hfs.get(name).get(0));
                    }
                }

                StreamBody responseBody = new StreamBody(new ByteArrayInputStream(item.data), item.data.length, MediaType.parseMediaType(item.contentType));
                item.data=null;
                item.status=0;
                response.setBody(responseBody);
                return responseBody;

            }
            return null;
        }

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
                byte[] bytes = new byte[8192];
                while ((len = in.read(bytes)) != -1) {
                    byos.write(bytes, 0, len);
                }
                in.close();

                bytes = new byte[byos.size()];
                System.arraycopy(byos.toByteArray(), 0, bytes, 0, bytes.length);
                byos.close();
                String string = new String(bytes);

                ArrayList<String> tsUrls = new ArrayList<String>();
                if (contentType.toLowerCase().contains("mpegurl")) {
                    String[] lines = string.split("\n");
                    StringBuilder sb = new StringBuilder();
                    boolean widthproxy = string.contains(".m3u8");
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        if (line.startsWith("#")) {
                            sb.append(line);
                        } else {
                            String absUrl = "";

                            if (line.startsWith("/")) {
                                absUrl = url.substring(0, url.indexOf('/', 9)) + line;
                            } else if (line.matches("^(http|https)://.+")) {
                                absUrl = line;
                            } else {
                                absUrl = url.substring(0, url.lastIndexOf("/") + 1) + line;
                            }


                            if (widthproxy|| string.contains("#EXT-X-ENDLIST"))
                                sb.append("/api/proxy.m3u8?url=" + URLEncoder.encode(absUrl));
                            else
                                sb.append(absUrl);

                            if (lines[i - 1].contains("#EXTINF:")) {
                                tsUrls.add(absUrl);
                            }
                        }

                        sb.append("\n");

                    }
                    bytes = sb.toString().getBytes();
                }
                if (tsUrls.size() > 0) {
                    MemCacheManager.buildIndexFrom(url, tsUrls);
                }

                Map<String, List<String>> hfs = httpURLConnection.getHeaderFields();
                for (String name : hfs.keySet()) {
                    if (name != null && !name.equalsIgnoreCase("content-length")) {
                        response.setHeader(name, httpURLConnection.getHeaderField(name));
                    }
                }
                // response.getOutputStream().write(bytes);
                //  return;

                //response.getWriter().write( bytes);

                StringBody responseBody = new StringBody(new String(bytes),MediaType.ALL);
               // StreamBody responseBody = new StreamBody(new ByteArrayInputStream(bytes), bytes.length, MediaType.parseMediaType(contentType));//

               // response.setBody(responseBody);
                return responseBody;

            } catch (Exception e) {
                e.printStackTrace();

                System.err.println("第" + count + "获取链接重试！\t" + url);
                count++;

            } finally {

                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

            }
        }

        if (count > retryCount)
            throw new Exception("连接超时！");
        return null;
    }
}
