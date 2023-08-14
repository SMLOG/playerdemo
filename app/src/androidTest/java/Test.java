import android.app.Application;

import com.alibaba.fastjson.JSON;
import com.usbtv.demo.ConfigStore;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Feed;
import com.usbtv.demo.sync.BiLi;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test extends Application {
    @Override
    public void onCreate() {
///https://www.bilibili.com/video/BV1fF411Z7bZ?spm_id_from=333.1007.tianma.2-2-5.click
        BiLi.getVidoInfo("BV1fF411Z7b", 0);


    }
}
