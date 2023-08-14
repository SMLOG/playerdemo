import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usbtv.demo.ConfigStore;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Feed;
import com.usbtv.demo.sync.V8ScriptEngine;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ULink {
    @Test
    public void test() throws IOException {


        ConfigStore config = new ConfigStore();
        config.cnnList =  new String[]{
                "https://edition.cnn.com/playlist/top-news-videos/index.json",
                "https://edition.cnn.com/video/data/3.0/video/business/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/health/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/politics/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/tech/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/world/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/economy/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/us/relateds.json",
                "https://edition.cnn.com/video/data/3.0/video/uk/relateds.json",

        };
         config.ipTvList = new String[]{"https://smlog.github.io/data/iptv.m3u"};
        config.feeds = new ArrayList<>();
        Feed feed = new Feed();
        feed.url="https://assets.msn.com/service/MSN/Feed/me?$top=20&DisableTypeSerialization=true&activityId=7E2F0C44-D701-4998-A932-A31D48A50A60&apikey=0QfOX3Vn51YCzitbLaRkTTBadtWpgTN8NZLW0C1SEM&contentType=video&location=21.3744|110.248&market=en-us&query=news%20video&queryType=myfeed&responseSchema=cardview&timeOut=1000&wrapodata=false";
        feed.refreshTime=4*3600*1000;
        feed.keepDate=5;
        feed.isDefRun = true;
        feed.name="News";
        config.feeds.add(feed);
        config.ytPlayList = new String[]{"https://www.youtube.com/playlist?list=PLwQJ7WOhLVizki7Zct_5RYF9wrHuPFYpr"};

        String string = JSON.toJSONString(config,true);
        System.out.println(string);


        System.getProperties().load(new FileReader(System.getProperty("user.home")+"\\.gradle\\gradle.properties"));
        System.getProperties();


        V8ScriptEngine v8scriptEngine2 = new V8ScriptEngine();


        FileInputStream testjs = new FileInputStream(new File(System.getProperty("user.home")+"\\Desktop\\ulink.js"));
        v8scriptEngine2.eval(testjs);
        String targetUrl="https://www.youtube.com/watch?v=4S1V7dTQ09Y";

        getLink(v8scriptEngine2,targetUrl);
    }

    private static String getLink(V8ScriptEngine v8scriptEngine2, String targetUrl) throws IOException {
        String rand = (String) v8scriptEngine2.eval("convert(\""+targetUrl+"\",'dec',0x3).replace(/[^0-9a-z]/gi, '').substring(0x0, 0xf)");

        String content = Utils.get("https://s1.youtube4kdownloader.com/ajax/getLinks.php?video="+URLEncoder.encode(targetUrl,"UTF-8") +"&rand="+ URLEncoder.encode(rand,"UTF-8"),"https://youtube4kdownloader.com/" );
        JSONObject json = JSON.parseObject(content);
        JSONArray jsonArray = json.getJSONObject("data").getJSONArray("av");
        int j=0;
        for(int i=0;i<jsonArray.size();i++){
            String ext = jsonArray.getJSONObject(i).getString("ext");
            if("mp4".equals(ext) || "mpeg".equals(ext)){
                j=i;break;
            }
        }
        String link = jsonArray.getJSONObject(j).getString("url").replace("[[_index_]]",""+j);
       return link;
    }
}
