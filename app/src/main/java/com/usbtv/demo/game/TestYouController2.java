package com.usbtv.demo.game;

import com.alibaba.fastjson.JSONObject;
import com.usbtv.demo.comm.HttpGet;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Drive;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.http.HttpResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@RestController
public class TestYouController2 {

    private static String cacheFolder = null;
    HttpGet httpGet = new HttpGet();
    private Map<Integer,List> cacheHisMap = new HashMap<Integer, List>();

    public static String exec(String cmd) {
        try {

            Process process = null;
            if (cmd.indexOf("|") > -1) {
                process = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            } else
                process = Runtime.getRuntime().exec(cmd);
            InputStream errorInput = process.getErrorStream();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String error = "";
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(errorInput));
            while ((line = bufferedReader.readLine()) != null) {
                error += line;
            }
            // Log.d("usb",result);
            return sb.toString().trim();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }





    private boolean isBlank(String str){
        return str==null||str.trim().equals("");
    }


    @GetMapping(path = "/api3/tts")
    public FileBody tts(@RequestParam(name = "lan") String lan, @RequestParam(name = "str") String str, HttpResponse resp) throws IOException {

        if (cacheFolder == null || new File(cacheFolder).canWrite()) {

            List<Drive> drives = Utils.getSysAllDriveList();
            String rootDir = drives.get(drives.size() - 1).getP();
            cacheFolder = rootDir + "/audio/";
        }

        String cacheFile = cacheFolder + str + ".mp3";

        File file = new File(cacheFile);
        if (file.exists()) {
            return new FileBody(file);
        }

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody2 = new FormBody.Builder().add("lang", lan.equals("en") ? "Joey" : "Zhiyu").add("msg", str.trim())
                .add("source", "ttsmp3").build();


        final Request request = new Request.Builder().url("https://ttsmp3.com/makemp3_new.php")
                .addHeader("Referer", "https://ttsmp3.com/")
                .addHeader("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36")
                .post(requestBody2).build();
        final Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        // System.out.println( "run: " + response.body().string());
        // return response.body().string();
        JSONObject jsonObj = JSONObject.parseObject(response.body().string());
        // resp.setHeader("content-type","application/json");

        String audioUrl = jsonObj.getString("URL");

        httpGet.saveToFile(audioUrl, cacheFile);
        return new FileBody(file);

        // return jsonObj.get("URL");

        // exec("say "+ list.get(result.getCurIndex()).getEnText());

    }


    @GetMapping(value = "/api3/proxy")
    public void proxy(@RequestParam(name = "url") String url, @RequestParam(name = "enText") String enText, HttpResponse resp)
            throws IOException {
        resp.sendRedirect(url);
    }




}
