import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.junit.Test;

import com.usbtv.demo.comm.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class TestUrl {

    static String url="https://assets.msn.com/service/MSN/Feed/me?$top=20&DisableTypeSerialization=true&activityId=7E2F0C44-D701-4998-A932-A31D48A50A60&apikey=0QfOX3Vn51YCzitbLaRkTTBadtWpgTN8NZLW0C1SEM&contentType=video&location=21.3744|110.248&market=en-us&query=news%20video&queryType=myfeed&responseSchema=cardview&timeOut=1000&wrapodata=false?t=234686";
    @Test
     public void test1() throws IOException {


        String resp = Utils.get(url);
        JSONObject jsonObj = JSONObject.parseObject(resp);
        JSONArray medias = (JSONArray) ( jsonObj.get("subCards"));
        for(int i=0;i<medias.size();i++){
          jsonObj = (JSONObject) medias.get(i);
            JSONArray vfs = jsonObj.getJSONArray("externalVideoFiles");
            String m3u8=null;
            String cc=null;
           // jsonObj.getJSONObject("images");
            //jsonObj.getString("title");
            //new  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(jsonObj.getString("publishedDateTime")).getTime();
            for(int j=0;j<vfs.size();j++){
                String link = vfs.getJSONObject(j).getString("url");
                if(link.indexOf("m3u8-aapl")>-1){
                    m3u8 = link;
                    break;
                }
            }
            if(m3u8!=null){
                JSONArray closedCaptions = jsonObj.getJSONObject("videoMetadata").getJSONArray("closedCaptions");
                if(closedCaptions!=null&&closedCaptions.size()>0)cc=closedCaptions.getJSONObject(0).getString("href");
                System.out.println(m3u8);
                System.out.println(cc);
            }

        }
         String url2="https://smlog.github.io/data/updateData.json";

        String content = Utils.get(url2);
       System.out.println(content);
    }
}
