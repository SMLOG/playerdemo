package com.usbtv.demo.sync;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Cache;
import com.usbtv.demo.data.ChannelCheck;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.usbtv.demo.sync.SyncCenter.updateScreenTabs;


class Channel {

    private String getTagV(String inf, String tag) {
        int begin = inf.indexOf(tag + "=\"");
        if (begin == -1)
            return "";
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
        chTitle = getTagV(inf, "ch-title");
        m3uUrl = url;
        title = inf.substring(inf.lastIndexOf(",") + 1);

        this.speech = l;
    }

    String id;
    String country;
    String logo;
    String language;
    String groupTitle;
    String chTitle;
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

    public static final Pattern PATTERN = Pattern.compile("\\d+p", Pattern.CASE_INSENSITIVE);

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

    public synchronized static void checkTvUrls(String id) {


        List<Folder> folders = null;
        try {
            Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
            folders = folderDao.queryBuilder().where().ge("typeId", 300)
                    .and().lt("typeId", 400).and().eq("isFav", 0).query();

            Dao<VFile, Integer> vFileDao = App.getHelper().getDao(VFile.class);

            for (int i = 0; i < folders.size(); i++) {

                Folder folder = folders.get(i);
                for (VFile file : folder.getFiles()) {
                    try {
                        if (!checkUrl(file.getdLink())) {
                            vFileDao.delete(file);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        continue;
                    }

                }

                if (folder.getFiles().size() == 0) {
                    folderDao.delete(folder);
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public interface ChannelFilter {

        String getChannelName();

        boolean filter(Channel ch);

        int compare(Channel o1, Channel o2);
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


    public static boolean checkUrl(String urls) {
        boolean ret = false;
        ChannelCheck channelCheck = null;
        try {
            Dao<ChannelCheck, Integer> dao = App.getHelper().getDao(ChannelCheck.class);
            channelCheck = dao.queryBuilder().where().eq("url", urls).queryForFirst();
            if (channelCheck != null && System.currentTimeMillis() - channelCheck.getDt() < 3 * 24 * 3600 * 1000) {
                return ret = channelCheck.isOk();
            }


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
                            ret = true;
                            break;
                        }

                    }
                    ret = false;
                    break;
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

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (channelCheck == null) {
                channelCheck = new ChannelCheck();
                channelCheck.setUrl(urls);
                channelCheck.setDt(System.currentTimeMillis());

            }else{
                channelCheck.setOk(ret);

            }
            try {
                App.getHelper().getDao(ChannelCheck.class).createOrUpdate(channelCheck);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        return ret;
    }

    public static void channelTV(List<Channel> channels, int channelID, String channelname,
                                 ArrayList<Integer> housekeepTypeIdList, Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> validFoldersMap) throws InterruptedException, SQLException {
        try {


            int i = channels.size();
            for (Channel ch : channels) {

              //  if (!checkUrl(ch.m3uUrl)) continue;

                Where<Folder, Integer> where = folderDao.queryBuilder().where()
                        .eq("typeId", channelID);
                if (ch.id != null && !ch.id.equals(""))
                    where.and().eq("aid", ch.id.replaceAll("'", "''"));
                else {
                    where.and().like("name", "%" + ch.title.replaceAll("'", "''").trim() + "%");
                }

                Folder zhbFolder = where.queryForFirst();
                if (zhbFolder == null) {

                    zhbFolder = new Folder();
                    zhbFolder.setTypeId(channelID);
                    zhbFolder.setName(ch.title);
                    zhbFolder.setAid(ch.id);
                    zhbFolder.setCoverUrl(ch.logo);
                    zhbFolder.setOrderSeq(i);
                    folderDao.createOrUpdate(zhbFolder);

                } else {
                    zhbFolder.setOrderSeq(i);
                    folderDao.createOrUpdate(zhbFolder);
                }
                VFile vf = vFileDao.queryBuilder().where().eq("folder_id", zhbFolder.getTypeId()).and().eq("dLink", ch.m3uUrl.replaceAll("'", "''")).queryForFirst();

                if (vf == null) {
                    vf = new VFile();
                    vf.setFolder(zhbFolder);
                    vf.setdLink(ch.m3uUrl);
                    Matcher m = PATTERN.matcher(ch.title);
                    if (m.find()) {
                        vf.setName(m.group());
                    }
                    vf.setOrderSeq(i);
                    vFileDao.createOrUpdate(vf);
                }

                i--;
                  validFoldersMap.put(zhbFolder.getId(), true);
            }

            typesMap.put(channelname, channelID);
             housekeepTypeIdList.add(channelID);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static boolean contains(String title, String[] es) {
        for (String e : es) {
            if (title.indexOf(e) > -1) return true;
        }
        return false;
    }



    public static void liveStream(int tvStartTypeId, ArrayList<Integer> housekeepTypeIdList, Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao, Map<Integer, Boolean> validFoldersMap) throws SQLException, InterruptedException, IOException {

        LinkedHashMap<String, List<Channel>> map = new LinkedHashMap<String, List<Channel>>();
        String[] urls = new String[]{"https://smlog.github.io/data/iptv.m3u"};
        List<Channel> channels = new ArrayList<>();
        for (String m3uUrl : urls) {
            String str = Utils.get(m3uUrl);
            extractChannels(channels, str);
        }

        for(Channel ch:channels){
            if(null!=ch.chTitle){
                if(map.get(ch.chTitle)==null)map.put(ch.chTitle,new ArrayList<Channel>());
                map.get(ch.chTitle).add(ch);
            }

        }

/*        LinkedHashMap<String, List<Channel>> map = (LinkedHashMap<String, List<Channel>>) TV.getChannels(
                new ChannelFilter[]{new ChannelFilter() {
                    @Override
                    public String getChannelName() {
                        return "Kids(English)";
                    }

                    @Override
                    public boolean filter(Channel ch) {
                        return
                                ch.language.indexOf("English") > -1
                                        && ch.groupTitle.indexOf("Kids") > -1

                                ;
                    }map

                    @Override
                    public int compare(Channel o1, Channel o2) {

                        return (int) (o1.speech - o2.speech);

                    }
                },


                }
        );*/

        int startTypeId = tvStartTypeId;
        for (String name : map.keySet()) {
            channelTV(map.get(name), startTypeId++, name, housekeepTypeIdList, typesMap, folderDao, vFileDao, validFoldersMap);
            updateScreenTabs(typesMap);
        }

        System.out.println("done");

    }

    public static boolean isGongDong(Channel ch) {
        return contains(ch.title, "广东,深圳,广州,珠海,东莞,佛山,中山,惠州,汕头,江门,湛江,肇庆,梅州,茂名,阳江,清远,韶关,揭阳,汕尾,潮州,河源,云浮".split(","));
    }

    private static Map<String, List<Channel>> getChannels(ChannelFilter[] channelFilters) throws IOException {


        String[] urls = new String[]{"https://iptv-org.github.io/iptv/index.m3u"};
        List<Channel> channels = new ArrayList<>();
        for (String m3uUrl : urls) {
            String str = Utils.get(m3uUrl);

            extractChannels(channels, str);
        }

        Map<String, List<Channel>> mapList = new LinkedHashMap<>();

        for (ChannelFilter def : channelFilters) {
            mapList.put(def.getChannelName(), new ArrayList<>());

        }

        for (Channel ch : channels) {
            for (ChannelFilter def : channelFilters) {
                if (def.filter(ch)) {
                    mapList.get(def.getChannelName()).add(ch);
                }
            }
        }

        for (ChannelFilter def : channelFilters) {
            Collections.sort(mapList.get(def.getChannelName()), new Comparator<Channel>() {
                @Override
                public int compare(Channel t1, Channel t2) {
                    return def.compare(t1, t2);

                }
            });

        }

        return mapList;
    }

    private static void extractChannels(List<Channel> channels, String str) {
        String[] lines = str.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.startsWith("#EXTINF")) {
                String url = lines[++i];

                Channel ch = new Channel(line, 0, url);
                channels.add(ch);

            }
        }
    }
}
