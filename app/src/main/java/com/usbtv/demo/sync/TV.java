package com.usbtv.demo.sync;


import com.usbtv.demo.comm.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class Channel {

    private String getTagV(String inf, String tag) {
        int begin = inf.indexOf(tag + "=\"");
        if (begin == -1)
            return null;
        begin = begin + tag.length() + 2;

        int last = inf.indexOf("\"", begin);

        return inf.substring(begin, last);
    }

    public Channel(String inf, long l, String url) {

        id = getTagV(inf, "tvg-id");
        country = getTagV(inf, "tvg-country");
        logo = getTagV(inf, "tvg-logo");
        language = getTagV(inf, "tvg-language");
        groupTitle = getTagV(inf, "group-title");
        m3uUrl = url;
        title = inf.substring(inf.lastIndexOf(",") + 1);

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
        return "#EXTINF:-1 tvg-id=\"" + id + "\" " + "tvg-country=\"" + country + "\" " + "tvg-language=\"" + language
                + "\" " + "tvg-logo=\"" + logo + "\" " + "group-title=\"" + groupTitle + "\"," + title + "\n" + m3uUrl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        return id.equals(((Channel) obj).id);
    }

}

public class TV {

    public static Runnable getCheckThread(String inf, String url, Set<Channel> channelList) {
        return new Runnable() {

            @Override
            public void run() {
                try {

                    long begin = System.currentTimeMillis();
                    if (checkUrl(url)) {
                        System.out.println("OK:" + url);
                        synchronized (channelList) {
                            channelList.add(new Channel(inf, System.currentTimeMillis() - begin, url));
                        }
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Not OK:" + url);

            }

        };
    }

    public static Runnable getCheckThread(String inf, String url, List<Channel> channelList) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    Channel ch = new Channel(inf, 0, url);

                    long begin = System.currentTimeMillis();
                    if (ch.title.indexOf("1080") >-1
                            &&("News".equals(ch.groupTitle) || ch.groupTitle.indexOf("General")>-1)
                            && (ch.language.indexOf("Chinese")>-1 || ch.language.equals("English"))
                            && checkUrl(url)) {

                        ch = new Channel(inf, System.currentTimeMillis() - begin, url);
                        System.out.println("OK:" + url);
                        synchronized (channelList) {
                            channelList.add(ch);
                        }
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Not OK:" + url);

            }

        };
    }

    public static void main(String[] args) throws Exception {


        List<Channel> channels = getChannels();

        System.out.println(channels.size());

    }

    public static List<Channel> getChannels() throws InterruptedException {
        String[] urls = new String[] { "https://iptv-org.github.io/iptv/index.m3u" };

        List<Channel> channels = checkM3uUrl(urls);

        Collections.sort(channels, new Comparator<Channel>() {
            @Override
            public int compare(Channel o1, Channel o2) {
                int r = o1.country.compareTo(o2.country);
                if (r == 0)
                    r = o1.id.compareTo(o2.id);
                if (r == 0)
                    r = (int) (o1.speech - o2.speech);

                return r;
            }
        });
        return channels;
    }

    public static List<Channel> checkM3uUrl(String[] urls) throws InterruptedException {
        final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

        List<Channel> channelList = new ArrayList<Channel>();

        for (String url : urls) {
            try {
                checkUrl(url, fixedThreadPool, channelList);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fixedThreadPool.shutdown();
        while (!fixedThreadPool.isTerminated()) {
            Thread.sleep(3000);
        }

        return channelList;
    }


    private static void checkUrl(String m3uUrl, final ExecutorService fixedThreadPool, List<Channel> channelList)
            throws FileNotFoundException, IOException {
        String str = Utils.get(m3uUrl);

        String[] lines = str.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.startsWith("#EXT")) {
                String url = lines[++i];
                fixedThreadPool.submit(getCheckThread(line, url, channelList));

            }
        }
    }



    private static boolean checkUrl(String urls) {
        System.out.println(urls);
        int count = 1;
        HttpURLConnection httpURLConnection = null;
        int retryCount = 2;
        Map<String, Object> requestHeaderMap = new HashMap<>();

        requestHeaderMap.put("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36");
        int timeoutMillisecond = 5 * 1000;
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

                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    String contentType = httpURLConnection.getHeaderField("Content-Type").toLowerCase();
                    System.out.println(contentType);

                    if (contentType.contains("mpegurl")) {

                        String line;
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        boolean isM3u = false;
                        while ((line = bufferedReader.readLine()) != null) {
                            if (!isM3u && line.indexOf("#EXT") > -1) {
                                isM3u = true;
                            }
                            if (isM3u && !line.startsWith("#")) {
                                line = line.trim();

                                String absUrl = "";
                                if (line.startsWith("/")) {
                                    absUrl = urls.substring(0, urls.indexOf('/', 9)) + line;
                                } else if (line.matches("^(http|https)://.+")) {
                                    absUrl = line;
                                } else {
                                    absUrl = urls.substring(0, urls.lastIndexOf("/") + 1) + line;
                                }

                                bufferedReader.close();
                                inputStream.close();
                                httpURLConnection.disconnect();
                                httpURLConnection = null;

                                return checkUrl(absUrl);
                            }

                        }

                        bufferedReader.close();
                        inputStream.close();

                    } else if (contentType.contains("mp2t") || contentType.contains("video/mpeg")) {
                        InputStream inputStream = httpURLConnection.getInputStream();

                        byte[] buf = new byte[1024];
                        while (inputStream.read(buf) > -1) {

                        }
                        return true;
                    }

                }

                return false;
            } catch (Exception e) {
                // e.printStackTrace();
                // Log.d("第" + count + "获取链接重试！\t" + urls);
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
