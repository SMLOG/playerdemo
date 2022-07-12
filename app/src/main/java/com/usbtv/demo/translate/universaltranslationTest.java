package com.usbtv.demo.translate;



import java.io.UnsupportedEncodingException;

public class universaltranslationTest {

    // 在平台申请的APP_ID 详见 http://api.fanyi.baidu.com/api/trans/product/desktop?req=developer
    public static void main(String[] args) throws Exception {
        TransApi api = new TransApi();

        String query = "hello";
        try {
            String transResult = api.getTransResult(query, "auto", "zh");
            System.out.println(transResult);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
