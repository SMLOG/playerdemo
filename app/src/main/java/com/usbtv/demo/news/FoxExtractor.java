package com.usbtv.demo.news;

import com.usbtv.demo.comm.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class FoxExtractor extends ListExtractor {

    @Override
    public Set<UploadItem> getItems() throws Exception {

        String feeds[] = new String[]{"https://www.foxnews.com/",};

        Set<UploadItem> set = new HashSet<UploadItem>();
        Pattern p = Pattern.compile("/images/(\\d{4}/\\d{2})/");
        String day = ("0" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        if (day.length() > 2) day = day.substring(1, 3);

        String fday = day.length() > 2 ? day.substring(1, 3) : day.substring(0, 2);

        for (String feed : feeds) {

            Document doc = Jsoup.connect(feed).userAgent(Utils.AGENT).get();
            Elements links = doc.select(".main-content .article a");
            for (int i = 0; i < links.size(); i++) {
                Element e = links.get(i);
                if (e.absUrl("href").split("-").length > 5) {
                    Matcher m = p.matcher(e.html());
                    if (m.find()) {


                        try {

                            Date d = new SimpleDateFormat("yyyy/MM/dd").parse(m.group(1) + "/" + fday);

                            if (!canSkip(d)) {
                                String url = e.absUrl("href");

                                UploadItem item = new UploadItem(url,
                                        new SimpleDateFormat("yyyyMMdd").format(d));

                                String path = url.replaceAll("[^0-9a-zA-Z]", "");

                                item.setP(item.getDate() + "/" + path.substring(path.length() > 30 ? path.length() - 30 : 0));
                                item.setSrc("fox");
                                set.add(item);
                            }

                        } catch (ParseException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }


                    }
                }


            }
        }

        return set;
    }

}
