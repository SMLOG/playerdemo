package com.usbtv.demo.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.sync.V8ScriptEngine;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

public class ULinkDownload {



    public static synchronized String getLink(String targetUrl) throws IOException {

        V8ScriptEngine v8scriptEngine2 = null;
        try {
          //  HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketClient.getSSLSocketFactory());
            v8scriptEngine2 = new V8ScriptEngine();
            InputStream fd2 = App.getInstance().getApplicationContext().getAssets().open("ulink.js");
            v8scriptEngine2.eval(fd2);
            String rand = (String) v8scriptEngine2.eval("convert(\"" + targetUrl + "\",'dec',0x3).replace(/[^0-9a-z]/gi, '').substring(0x0, 0xf)");

            String url = "https://s1.youtube4kdownloader.com/ajax/getLinks.php?video=" + URLEncoder.encode(targetUrl, "UTF-8") + "&rand=" + URLEncoder.encode(rand, "UTF-8");
            System.out.println(url);
            String content = Utils.get(url,
                    "https://youtube4kdownloader.com/");
            JSONObject json = JSON.parseObject(content);
            JSONArray jsonArray = json.getJSONObject("data").getJSONArray("av");
            int j=0;
            for(int i=0;i<jsonArray.size();i++){
                String ext = jsonArray.getJSONObject(i).getString("ext");
                if("mp4".equals(ext) ){
                    j=i;break;
                }
            }
            String link = jsonArray.getJSONObject(j).getString("url").replace("[[_index_]]",""+j);
            return link;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (v8scriptEngine2 != null) v8scriptEngine2.release();
            } catch (Throwable ee) {
                ee.printStackTrace();
            }
        }

    }
}

