package com.usbtv.demo.vurl;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



class Channel{

    private String getTagV(String inf,String tag) {
        int begin = inf.indexOf(tag+"=\"");
        if(begin==-1)return null;
        begin = begin+tag.length()+2;

        int last = inf.indexOf("\"",begin);

        return inf.substring(begin,last);
    }
    public Channel(String inf, long l,String url) {

        id=getTagV( inf,"tvg-id");
        country=getTagV( inf,"tvg-country");
        logo=getTagV( inf,"tvg-logo");
        language=getTagV( inf,"tvg-language");
        groupTitle=getTagV( inf,"group-title");
        m3uUrl=url;
        title=inf.substring(inf.lastIndexOf(",")+1);

        this.speech = l;
    }
    String id;
    String country;
    String logo;
    String language;
    String groupTitle;
    String title;
    String m3uUrl;
    long speech;
    @Override
    public String toString() {
        return "#EXTINF:-1 tvg-id=\""+id+"\" "
                + "tvg-country=\""+country+"\" "
                + "tvg-language=\""+language+"\" "
                + "tvg-logo=\""+logo+"\" "
                + "group-title=\""+groupTitle+"\","
                + title+"\n"+m3uUrl;
    }
    @Override
    public int hashCode() {
        return  Objects.hash(id);
    }
    @Override
    public boolean equals(Object obj) {

        if (this ==obj)return true;
        if(obj == null || getClass() != obj.getClass())return false;

        return id.equals(((Channel)obj).id);
    }

}

public class M3U {


    public static Runnable getCheckThread(String inf,String url,Set<Channel> channelList) {
        return new Runnable() {

            @Override
            public void run() {
                try{

                    long begin = System.currentTimeMillis();
                    if( checkUrl(url)) {
                        System.out.println("OK:"+url);
                        synchronized (channelList) {
                            channelList.add(new Channel(inf,System.currentTimeMillis()-begin,url));
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


    public static void main(String[] args) throws Exception {

        String[] filePaths= new String[]{
                "/Users/alexwang/git/iptv/channels/us.m3u",
                "/Users/alexwang/git/iptv/channels/uk.m3u"
        };
        String outfile="/Users/alexwang/git/iptv/channels/us_checked.m3u";

        if(false) {

            //	getCheckThread("", "https://cdn.untvweb.com/live-stream/playlist.m3u8", new StringBuilder()).run();
            return;
        }


        StringBuilder sb = check(filePaths);

        System.out.println(sb.toString());

        FileOutputStream fos = new FileOutputStream(new File(outfile));
        fos.write(sb.toString().getBytes());
        fos.close();

    }


    public static StringBuilder check(String[] filePaths) throws InterruptedException {
        final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(30);

        LinkedHashSet<Channel> channelList = new LinkedHashSet<Channel>();

        for(String filePath:filePaths) {
            try {
                checkFile(filePath, fixedThreadPool, channelList);

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        fixedThreadPool.shutdown();
        while(!fixedThreadPool.isTerminated()) {
            Thread.sleep(3000);
        }


        StringBuilder sb =new StringBuilder("#EXTM3U\n");


        for(Channel ch:channelList) {
            sb.append(ch.toString()).append("\n");
        }
        return sb;
    }


    private static void checkFile(String filePath, final ExecutorService fixedThreadPool, Set<Channel> channelList)
            throws FileNotFoundException, IOException {
        FileInputStream fs = new FileInputStream(new File(filePath));
        byte[] bytes = new byte[fs.available()];
        fs.read(bytes);
        fs.close();
        String str = new String(bytes,StandardCharsets.UTF_8);
        System.out.println(str);

        String[] lines = str.split("\n");


        for(int i=0;i<lines.length;i++ ) {
            String line=lines[i];

            if(line.startsWith("#EXT")) {

                String url = lines[++i];
                fixedThreadPool.submit(getCheckThread(line, url, channelList));

            }
        }
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
