import android.app.Application;

import com.alibaba.fastjson.JSON;
import com.usbtv.demo.ConfigStore;
import com.usbtv.demo.comm.App;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Feed;
import com.usbtv.demo.sync.BiLi;
import com.usbtv.demo.sync.V8ScriptEngine;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Test  {
   // @Test
    public void onCreate() {
///https://www.bilibili.com/video/BV1fF411Z7bZ?spm_id_from=333.1007.tianma.2-2-5.click
        BiLi.getVidoInfo("BV1fF411Z7b", 0);

        V8ScriptEngine v8scriptEngine2 = new V8ScriptEngine();
        try {
            InputStream fd1 = new FileInputStream("/Users/alexwang/Downloads/crypto-js.min.js");
            InputStream fd2 = new FileInputStream("/Users/alexwang/Downloads/time.min.js");

            v8scriptEngine2.eval(fd1);
            v8scriptEngine2.eval(fd2);


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
