import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usbtv.demo.comm.BTree;
import com.usbtv.demo.comm.Utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;


public class TestDict {


    @Test
    public void test1() throws Exception {


    }



    @Test
    public void test2() {
        String abc = "a b c";
        abc.split("\\b");
        System.out.println("ok");
    }

}
