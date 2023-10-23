import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usbtv.demo.ConfigStore;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Feed;
import com.usbtv.demo.sync.BiLi;
import com.usbtv.demo.sync.V8ScriptEngine;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ULink2 {
    @Test
    public void test() throws IOException {

        V8ScriptEngine v8scriptEngine2 = new V8ScriptEngine();
        try {
            InputStream fd1 = new FileInputStream("/Users/alexwang/Downloads/crypto-js.min.js");
            InputStream fd2 = new FileInputStream("/Users/alexwang/Downloads/time.min.js");

            v8scriptEngine2.eval(fd1);
            v8scriptEngine2.eval(fd2);
            String bvid="BV1fw41167Dx";
            int p=1;
            JSONObject info = BiLi.getVideoInfo(v8scriptEngine2, "https://www.bilibili.com/video/" + bvid + "?p=" + p + "&spm_id_from=pageDriver");


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }



    }

}
