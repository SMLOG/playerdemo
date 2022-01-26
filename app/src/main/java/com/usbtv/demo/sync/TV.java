package com.usbtv.demo.sync;


import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public static Runnable getCheckThread(String inf, String url, List<Channel> channelList, ChannelFilter filter) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    Channel ch = new Channel(inf, 0, url);

                    long begin = System.currentTimeMillis();
                    if (filter.filter(ch)
                        //    && checkUrl(url)
                    ) {

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


        //   List<Channel> channels = getChannels();

        //   System.out.println(channels.size());

    }

    public interface ChannelFilter {

        boolean filter(Channel ch);
    }

    public static List<Channel> getChannels(ChannelFilter filter, Comparator<Channel> sorter) throws InterruptedException {
        String[] urls = new String[]{"https://iptv-org.github.io/iptv/index.m3u"};

        List<Channel> channels = checkM3uUrl(urls, filter);

        Collections.sort(channels, sorter);
        return channels;
    }

    public static List<Channel> checkM3uUrl(String[] urls, ChannelFilter filter) throws InterruptedException {
        final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

        List<Channel> channelList = new ArrayList<Channel>();

        for (String url : urls) {
            try {
                checkM3U8(url, fixedThreadPool, channelList, filter);

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


    private static void checkM3U8(String m3uUrl, final ExecutorService fixedThreadPool, List<Channel> channelList, ChannelFilter filter)
            throws IOException {
        String str = Utils.get(m3uUrl);

        String[] lines = str.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.startsWith("#EXT")) {
                String url = lines[++i];
                fixedThreadPool.submit(getCheckThread(line, url, channelList, filter));

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
                    long length = Long.parseLong(httpURLConnection.getHeaderField("Content-Length"));
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

                    } else if (contentType.contains("mp2t") || contentType.contains("video/mpeg") || length > 300 * 1024) {
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

    public static void channelTV(TV.ChannelFilter filter, int channelID, String channelname,
                                 Comparator<Channel> sort,
                                 ArrayList<Integer> housekeepTypeIdList, Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> validFoldersMap) throws InterruptedException, SQLException {
       try {
           List<Channel> chs = TV.getChannels(
                   filter, sort
           );

           int i = chs.size();
           for (Channel ch : chs) {
               Folder zhbFolder = folderDao.queryBuilder().where().eq("typeId", 2).and().eq("name", ch.title.replaceAll("'","''")).queryForFirst();
               if (zhbFolder == null) {
                   zhbFolder = new Folder();
                   zhbFolder.setTypeId(channelID);
                   zhbFolder.setName(ch.title);
                   zhbFolder.setCoverUrl(ch.logo);
                   folderDao.createOrUpdate(zhbFolder);

                   VFile vf = new VFile();
                   vf.setFolder(zhbFolder);
                   vf.setdLink(ch.m3uUrl);
                   vf.setOrderSeq(i);
                   vFileDao.createOrUpdate(vf);
               } else {
                   VFile vf = zhbFolder.getFiles().iterator().next();
                   vf.setdLink(ch.m3uUrl);
                   vf.setName(ch.title);
                   vf.setOrderSeq(i);
                   vFileDao.createOrUpdate(vf);
               }
               i--;
               validFoldersMap.put(zhbFolder.getId(), true);
           }

           typesMap.put(channelname, channelID);
           housekeepTypeIdList.add(channelID);
       }catch (Throwable e){
           e.printStackTrace();
       }
    }

    public static void liveStream(ArrayList<Integer> housekeepTypeIdList, Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> validFoldersMap) throws SQLException, InterruptedException {


       channelTV(new TV.ChannelFilter() {
                      @Override
                      public boolean filter(Channel ch) {
                          return ch.title.indexOf("1080") > -1 &&
                                  ch.language.indexOf("English") > -1 &&
                                  ch.groupTitle.indexOf("Kids") > -1
                                  ;
                      }
                  }, 5, "TV(Kids)",
                new Comparator<Channel>() {
                    @Override
                    public int compare(Channel o1, Channel o2) {
                        return (int) (o1.speech - o2.speech);
                    }
                },
                housekeepTypeIdList, typesMap, folderDao, vFileDao, validFoldersMap);


        channelTV(new TV.ChannelFilter() {
                      @Override
                      public boolean filter(Channel ch) {
                          return ch.title.indexOf("1080") > -1 &&
                                  "CN".equals(ch.country);
                      }
                  }, 2, "电视",
                new Comparator<Channel>() {
                    @Override
                    public int compare(Channel o1, Channel o2) {
                        int w1 = o1.groupTitle.indexOf("News") > -1 || o1.groupTitle.indexOf("General") > -1 ? 10 : (
                                o1.groupTitle.indexOf("广东") > -1 || o1.groupTitle.indexOf("卫视") > -1 ? 5 : 0
                        );
                        int w2 = o2.groupTitle.indexOf("News") > -1 || o2.groupTitle.indexOf("General") > -1 ? 10 : (
                                o2.groupTitle.indexOf("广东") > -1 || o2.groupTitle.indexOf("卫视") > -1 ? 5 : 0
                        );
                        int r = w1 - w2;
                        if (r == 0)
                            r = o1.id.compareTo(o2.id);
                        if (r == 0)
                            r = (int) (o1.speech - o2.speech);

                        return r;
                    }
                },
                housekeepTypeIdList, typesMap, folderDao, vFileDao, validFoldersMap);

        channelTV(new TV.ChannelFilter() {
                      @Override
                      public boolean filter(Channel ch) {
                          return ch.title.indexOf("1080") > -1 &&
                                  ch.language.indexOf("English") > -1
                                  && ch.groupTitle.indexOf("Kids") == -1
                                  ;
                      }
                  }, 6, "TV(English)",
                new Comparator<Channel>() {
                    @Override
                    public int compare(Channel o1, Channel o2) {
                        int w1 = o1.groupTitle.indexOf("News") > -1 || o1.groupTitle.indexOf("General") > -1 ? 10 : 0;
                        int w2 = o2.groupTitle.indexOf("News") > -1 || o2.groupTitle.indexOf("General") > -1 ? 10 : 0;
                        int r = w1 - w2;
                        if (r == 0)
                            r = o1.id.compareTo(o2.id);
                        if (r == 0)
                            r = (int) (o1.speech - o2.speech);

                        return r;
                    }
                },
                housekeepTypeIdList, typesMap, folderDao, vFileDao, validFoldersMap);

        channelTV(new TV.ChannelFilter() {
                      @Override
                      public boolean filter(Channel ch) {
                          return ch.title.indexOf("1080") > -1 &&
                                  ch.language.indexOf("English") == -1 &&
                                  !ch.country.equals("CN")
                                  ;
                      }
                  }, 7, "TV(other)",
                new Comparator<Channel>() {
                    @Override
                    public int compare(Channel o1, Channel o2) {
                        int w1 = o1.groupTitle.indexOf("News") > -1 || o1.groupTitle.indexOf("General") > -1 ? 10 : 0;
                        int w2 = o2.groupTitle.indexOf("News") > -1 || o2.groupTitle.indexOf("General") > -1 ? 10 : 0;
                        int r = w1 - w2;
                        if (r == 0)
                            r = o1.id.compareTo(o2.id);
                        if (r == 0)
                            r = (int) (o1.speech - o2.speech);

                        return r;
                    }
                },
                housekeepTypeIdList, typesMap, folderDao, vFileDao, validFoldersMap);

    }
}
