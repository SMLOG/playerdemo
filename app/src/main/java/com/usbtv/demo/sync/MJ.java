package com.usbtv.demo.sync;


import static com.usbtv.demo.sync.SyncCenter.updateScreenTabs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.RunCron;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;
import com.usbtv.demo.news.BtoAAtoB;


public class MJ {

    public static void syncFromRecnetlyUpate(RunCron.Period srcPeriod,int startTypeId, String[][] arr, Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao) throws IOException, ParseException, InterruptedException, URISyntaxException, SQLException {

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
            doc = Jsoup.connect(next).get();
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
                    int status2 = !"".equals(status) ? Integer.parseInt(status) : 0;
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

                    if(updateDateTime>folder.getUpdateTime()){

                        folder.setRate(rate2);
                        folder.setUpdateTime(Integer.parseInt(updateDate));

                        Collection<VFile> files = folder.getFiles();
                        folderDao.createOrUpdate(folder);

                        getM3u8s(vFileDao,linke.absUrl("href"),folder,files!=null?files.size():0);

                    }

                    // arr.add(row);

                }catch(Throwable t) {
                    t.printStackTrace();
                }



            }
        }



    }

    public static void syncList(RunCron.Period srcPeriod, int startTypeId, String tag, String chName, Map<String, Integer> typesMap, Dao<Folder, Integer> folderDao, Dao<VFile, Integer> vFileDao) throws IOException, ParseException, InterruptedException, URISyntaxException, SQLException {

        String next = "https://www.kanju5.com/category/"+tag;
        Document doc;
        typesMap.put(chName,startTypeId);
        boolean isFirstUpdate=false;
        while(!next.equals("")) {
            System.out.println(next);
            doc = Jsoup.connect(next).get();
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

                    String rate = item.select(".entry-rating").text().replaceAll("[^0-9.]+", "");
                    String status = item.select(".entry-status").text().replaceAll("[^0-9.]+", "");

                    String date = item.select(".date").eq(0).text().replaceAll("[^0-9.]+", "");
                    String updateDate = item.select(".date").eq(1).text().replaceAll("[^\\d](\\d)[^\\d]", "0$1").replaceAll("[^0-9.]+", "");

                    String id = linke.absUrl("href").split("/")[4].split("\\.")[0];
                    String title =  linke.attr("title");

                    String img = item.select("img").get(0).absUrl("src");

                    float rate2= !"".equals(rate) ? Float.parseFloat(rate) : 0;
                    int status2 = !"".equals(status) ? Integer.parseInt(status) : 0;
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

                    if(updateDateTime>folder.getUpdateTime()){

                        folder.setRate(rate2);
                        folder.setUpdateTime(Integer.parseInt(updateDate));

                        Collection<VFile> files = folder.getFiles();
                        folderDao.createOrUpdate(folder);

                        getM3u8s(vFileDao,linke.absUrl("href"),folder,files!=null?files.size():0);

                    }

                   // arr.add(row);

                }catch(Throwable t) {
                    t.printStackTrace();
                }

                isFirstUpdate=true;


            }

            if(isFirstUpdate) updateScreenTabs(typesMap);

        }



    }



    public static  boolean getM3u8s(Dao<VFile, Integer> vFileDao,String url,Folder folder, int off) throws IOException, URISyntaxException, SQLException {


        System.out.println(url);
        List<String> ret =new ArrayList<String>();
        Document doc = Jsoup.connect(url).get();
        Elements urls = doc.select(".play_list#play_list_o li a");
        int total = urls.size();
        for (int i = total - 1 - off; i >= 0; --i) {
            System.out.println(i);
            String href = urls.get(i).absUrl("href");
            if(href==null || href.trim().equals(""))continue;
            String a[]=Jsoup.connect(href).get().toString().split("fc: \"");
            String fc= a[1].split("\"")[0];

            Document doc3 = Jsoup.connect("https://www.kanju5.com/player/player.php")
                    .data("height", "500")
                    .data("fc", fc)
                    .ignoreContentType(true)
                    //.header("Content-Type", "application/json;charset=UTF-8")
                    .post();
            String src = doc3.select("iframe").eq(0).attr("src");
            String m3u8Link = src.split("url=")[1].split("\\\\")[0];
            m3u8Link= BtoAAtoB.atob(m3u8Link);
            System.out.println(m3u8Link);

            VFile vfile = new VFile();
            vfile.setdLink(m3u8Link);
            vfile.setOrderSeq(total-i);
            vfile.setFolder(folder);
            vFileDao.createOrUpdate(vfile);

        }
        return true;

    }

}

