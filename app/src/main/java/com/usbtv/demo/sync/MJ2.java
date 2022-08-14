package com.usbtv.demo.sync;


import static com.usbtv.demo.sync.SyncCenter.updateScreenTabs;

import androidx.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.CatType;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.news.BtoAAtoB;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MJ2 {

    public static boolean stop;

    public static void syncFromRecnetlyUpate(RunCron.Period srcPeriod, int startTypeId, String[][] arr,
                                             Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao) throws IOException, ParseException, InterruptedException, URISyntaxException, SQLException {

        Map<String,Integer> strIdMap = new HashMap<>();
        Map<String,String> strTaskIdMap = new HashMap<>();
        int ii=0;
        for (String[] a:arr){
            strIdMap.put(a[2],ii++);
            strTaskIdMap.put(a[2],a[2]);
        }
        String next = "https://www.kanju5.com/new";
        Document doc;
        while(!next.equals("")) {
            System.out.println(next);
            doc = Jsoup.connect(next).userAgent(Utils.AGENT).get();
            Elements itmes = doc.select(".post.clearfix");
            Elements nextLink = doc.select(".nextpostslink");
            if (nextLink.size() > 0)
                next = nextLink.get(0).absUrl("href");
            else
                next = "";

            for (int i = 0; i < itmes.size(); i++) {

                try {
                    Element item = itmes.get(i);
                    Element linke = item.select("a").get(0);
                    String cat = item.select("a[rel=category tag]").get(0).text().trim();
                    String rate = item.select(".entry-rating").text().replaceAll("[^0-9.]+", "");
                    String status = item.select(".entry-status").text().replaceAll("[^0-9.]+", "");

                    String date = item.select(".date").eq(0).text().replaceAll("[^0-9.]+", "");
                    String updateDate = item.select(".date").eq(1).text().replaceAll("[^\\d](\\d)[^\\d]", "0$1").replaceAll("[^0-9.]+", "");

                    String id = linke.absUrl("href").split("/")[4].split("\\.")[0];
                    String title =  linke.attr("title");

                    String img = item.select("img").get(0).absUrl("src");

                    float rate2= !"".equals(rate) ? Float.parseFloat(rate) : 0;
                    int nums = !"".equals(status) ? Integer.parseInt(status) : 0;
                  /*  Object row[] = new Object[] {
                            id,
                           title,
                            rate2,
                            status2,
                            Integer.parseInt(date),
                            Integer.parseInt(updateDate),

                    };*/

                    int updateDateTime = Integer.parseInt(updateDate);
                    if(strIdMap.get(cat)==null) continue;
                    int typeId= startTypeId + strIdMap.get(cat);

                   RunCron.Period period= RunCron.peroidMap.get("dsj"+strIdMap.get(cat));

                   if(period==null || !period.getEnable())continue;


                    Folder folder = folderDao.queryBuilder().where().eq("typeId",typeId).and().eq("aid",id).queryForFirst();
                    if(folder==null) {
                        folder = new Folder();
                        folder.setTypeId(typeId);
                        folder.setName(title);
                        folder.setAid(id);
                        folder.setCoverUrl(img);
                        folder.setPubTime(Integer.parseInt(date));
                        folder.setOrderSeq(Integer.parseInt(updateDate));
                        folder.setRate(rate2);
                        folder.setJob(srcPeriod.getId());
                    }

                    updateFolderItems(folderDao, vFileDao, linke.absUrl("href"), updateDate, rate2, nums, updateDateTime, folder);


                }catch(Throwable t) {
                    t.printStackTrace();
                }



            }
        }



    }


    public static void syncList(RunCron.Period srcPeriod, int startTypeId, String tag, String chName,
                                Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao) throws IOException, ParseException, InterruptedException, URISyntaxException, SQLException {

        String next = "https://www.kanju5.com/category/"+tag;
        Document doc;
        CatType catType = new CatType();
        catType.setName(chName);
        catType.setTypeId(startTypeId);
        catType.setStatus("A");
        catType.setJob(srcPeriod.getId());

        App.getCatTypeDao().createOrUpdate(catType);
        //typesMap.put(chName,startTypeId);
        boolean isFirstUpdate=false;
        while(!next.equals("")) {
            System.out.println(next);
            doc = Jsoup.connect(next).userAgent(Utils.AGENT).get();
            Elements itmes = doc.select(".post.clearfix");
            Elements nextLink = doc.select(".nextpostslink");
            if (nextLink.size() > 0)
                next = nextLink.get(0).absUrl("href");
            else
                next = "";
            
            if(MJ2.stop){
                stop=false;
                return;
            }

            for (int i = 0; i < itmes.size(); i++) {

                try {
                    Element item = itmes.get(i);
                    Element linke = item.select("a").get(0);

                    String rate = item.select(".entry-rating").text().replaceAll("[^0-9.]+", "");
                    String status = item.select(".entry-status").text().replaceAll("[^0-9.]+", "");

                    String date = item.select(".date").eq(0).text().replaceAll("[^0-9.]+", "");
                    String updateDate = item.select(".date").eq(1).text().replaceAll("[^\\d](\\d)[^\\d]", "0$1").replaceAll("[^0-9.]+", "");

                    String id = linke.absUrl("href").split("/")[4].split("\\.")[0];
                    String title =  linke.attr("title");

                    String img = item.select("img").get(0).absUrl("src");

                    float rate2= !"".equals(rate) ? Float.parseFloat(rate) : 0;
                    int nums = !"".equals(status) ? Integer.parseInt(status) : 0;
                  /*  Object row[] = new Object[] {
                            id,
                           title,
                            rate2,
                            status2,
                            Integer.parseInt(date),
                            Integer.parseInt(updateDate),

                    };*/

                    int updateDateTime = Integer.parseInt(updateDate);
                    int typeId=startTypeId;
                    Folder folder = folderDao.queryBuilder().where().eq("typeId",typeId).and().eq("aid",id).queryForFirst();
                    if(folder==null) {
                        folder = new Folder();
                        folder.setTypeId(typeId);
                        folder.setName(title);
                        folder.setAid(id);
                        folder.setCoverUrl(img);
                        folder.setPubTime(Integer.parseInt(date));
                        folder.setOrderSeq(Integer.parseInt(updateDate));
                        folder.setRate(rate2);
                        folder.setJob(srcPeriod.getId());
                    }

                    updateFolderItems(folderDao, vFileDao, linke.absUrl("href"), updateDate, rate2, nums, updateDateTime, folder);

                    // arr.add(row);

                }catch(Throwable t) {
                    t.printStackTrace();
                }

                isFirstUpdate=true;


            }

            if(isFirstUpdate) updateScreenTabs();

        }



    }

    private static void updateFolderItems(Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao, String detailUrl, String updateDate, float rate2, int nums, int updateDateTime, Folder folder) throws IOException, URISyntaxException, SQLException {
        if(updateDateTime > folder.getUpdateTime()){

            folder.setRate(rate2);
            folder.setUpdateTime(Integer.parseInt(updateDate));

           // folder.setNum(nums);

            if(nums ==0){
                List<String> urls = getWatchUrlsAsc(detailUrl);
                nums = urls.size();
            }

            Collection<VFile> files = folder.getFiles();
            folderDao.createOrUpdate(folder);

            createEmptyItems(vFileDao, folder, nums, files==null?0:files.size());


            // getM3u8s(vFileDao,linke.absUrl("href"),folder,files!=null?files.size():0);

        }
    }

    public static void updateVfileLink(VFile vfile) throws SQLException, IOException, URISyntaxException {
       String detailUrl = "https://www.kanju5.com/views/" +vfile.getFolder().getAid()+".html";

        List<String> urls = getWatchUrlsAsc(detailUrl);
        String watchUrl = urls.get(vfile.getPage());

        String m3u8 = getM3u8LinkFromWatchUrl(watchUrl);
        if(m3u8!=null&&m3u8.contains(""))m3u8=m3u8.replace("new.qqaku.com","new.iskcd.com");
        vfile.setdLink(m3u8);

    }
    static HashMap<String,List<String>> itemUrlsCacheMap = new HashMap<>();

    public static List<String> getWatchUrlsAsc(String url) throws IOException, URISyntaxException, SQLException {

        if(itemUrlsCacheMap.get(url)!=null) return itemUrlsCacheMap.get(url);

        System.out.println(url);
        Document doc = Jsoup.connect(url).userAgent(Utils.AGENT).get();
        Elements urls = doc.select(".play_list#play_list_o li a");
        int total = urls.size();

        List<String> hrefs = new ArrayList<>();
        for (int i = total-1 ; i >= 0; --i) {
            System.out.println(i);
            String href = urls.get(i).absUrl("href");
            if (href == null || href.trim().equals("")) continue;
            hrefs.add(href);
        }
        itemUrlsCacheMap.put(url,hrefs);
        return hrefs;

    }


    private static void createEmptyItems(Dao<VFile, Integer> vFileDao, Folder folder, int itemCount, int startIndex) throws SQLException {
        for (int j = startIndex; j < itemCount; j++) {
            VFile vFile = new VFile();
            vFile.setFolder(folder);
            vFile.setPage(j);
            vFile.setOrderSeq(j);
            vFileDao.createOrUpdate(vFile);
        }
    }


    @Nullable
    private static String getM3u8LinkFromWatchUrl(String watchUrl) throws IOException {
        String a[] = Jsoup.connect(watchUrl).userAgent(Utils.AGENT).get().toString().split("fc: \"");
        String fc = a[1].split("\"")[0];

        Document doc3 = Jsoup.connect("https://www.kanju5.com/player/player.php").userAgent(Utils.AGENT)
                .data("height", "500")
                .data("fc", fc)
                .ignoreContentType(true)
                //.header("Content-Type", "application/json;charset=UTF-8")
                .post();
        String src = doc3.select("iframe").eq(0).attr("src");
        String m3u8Link = src.split("url=")[1].split("\\\\")[0];
        m3u8Link = BtoAAtoB.atob(m3u8Link);
        System.out.println(m3u8Link);
        return m3u8Link;
    }


}

