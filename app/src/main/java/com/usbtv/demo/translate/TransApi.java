package com.usbtv.demo.translate;



import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bright
 */
public class TransApi {

    /**
     * 请求api接口
     */
    private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";
    /**
     * 开发者ID
     */
    private static final String APP_ID = "20181025000225318";
    /**
     * 开发者密钥
     */
    private static final String SECURITY_KEY = "s0rbKVj44RcEH9m4yXrf";

    /**
     * 获取翻译结果
     * 模版:{"from":"zh","to":"en","trans_result":[{"src":"\u963f\u62c9\u65af\u52a0","dst":"Alaska"}]}
     *
     * @param query
     * @param from
     * @param to
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getTransResult(String query, String from, String to) throws Exception {
        Map<String, String> params = buildParams(query, from, to);
        //返回的结果是一个JSON字符串,
        // {"error_code":"52003","error_msg":"UNAUTHORIZED USER"}
        // 如{"from":"zh","to":"en","trans_result":[{"src":"\u963f\u62c9\u65af\u52a0","dst":"Alaska"}]}
        JSONArray trans_result= null;
        int count = 30;
        while(trans_result == null && count >0) {
            String body = HttpGet.get(TRANS_API_HOST, params);
            System.out.println(body);
            JSONObject jsonObject = JSONArray.parseObject(body);
            trans_result = jsonObject.getJSONArray("trans_result");
            count--;
        }

        return JSONArray.parseObject(trans_result.get(0).toString()).get("dst").toString();
    }

    private Map<String, String> buildParams(String query, String from, String to) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);
        params.put("appid", APP_ID);
        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);
        // 签名
        String src = APP_ID + query + salt + SECURITY_KEY;
        params.put("sign", MD5.md5(src));
        return params;
    }
}

