package com.usbtv.demo.game;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.App;
import com.usbtv.demo.FileDownload;
import com.usbtv.demo.comm.HttpGet;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.ResItem;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.http.HttpResponse;

import okhttp3.*;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

@RestController
public class TestYouController {

    private static String cacheFolder = null;
    HttpGet httpGet = new HttpGet();

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


    @com.yanzhenjie.andserver.annotation.ResponseBody
    @GetMapping(value = "/api2/randList")
    public String randList(@RequestParam(name = "n") int n) throws SQLException {

        Dao<ResItem, Integer> subjectDao = App.getHelper().getDao(ResItem.class);

        long total = subjectDao.queryBuilder().countOf();
        long maxId = total;

        List<ResItem> list = new ArrayList<ResItem>();
        if (total == 0) return JSON.toJSONString(list);
        ;
        Set<Integer> set = new HashSet();
        while (true) {
            int nextId = (int) (Math.random() * maxId);
            if (set.contains(nextId))
                continue;
            //Optional<Subject> subject = subjectsRepository.findById(nextId);
            ResItem subject = subjectDao.queryBuilder().where().eq("typeId", ResItem.IMAGE).and().ge("id", nextId)
                    .and().isNotNull("imgUrl")
                    .and().lt("rTimes", 5).queryForFirst();


            set.add(nextId);
            if (subject == null) continue;
            set.add(subject.getId());

            list.add(subject);

            if (list.size() == n)
                break;
        }

        return JSON.toJSONString(list);
    }


    @com.yanzhenjie.andserver.annotation.ResponseBody
    @PostMapping(value = "/api2/save")
    public String save(com.yanzhenjie.andserver.http.RequestBody content) throws Exception {
        ResItem subject = JSON.parseObject(content.string(), ResItem.class);
        subject.setEnText(subject.getEnText().toLowerCase().trim());
        if(subject.getEnText().equals(""))return null;
        Dao<ResItem, Integer> subjectDao = App.getHelper().getDao(ResItem.class);

        subject.setTypeId(ResItem.IMAGE);
        if (subject.getId() != 0) {
            subjectDao.update(subject);

        } else {
            ResItem target = subjectDao.queryBuilder().where().eq("enText", subject.getEnText()).queryForFirst();
            if (target != null) {
                subject.setId(target.getId());
                subjectDao.update(subject);
            } else
                subjectDao.create(subject);

        }

        return JSON.toJSONString(subject);

    }

    @GetMapping(path = "/api2/say")
    public void say(@RequestParam(name = "str") String str) {

        //exec("say  " + str);

        // exec("say "+ list.get(result.getCurIndex()).getEnText());

    }

    @GetMapping(path = "/api2/tts")
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


    @GetMapping(path = "/api2/ttscacheall")
    public String ttscacheall() throws SQLException {

        List<ResItem> items = App.getHelper().getDao(ResItem.class).queryForAll();

        if (cacheFolder == null || new File(cacheFolder).canWrite()) {

            List<Drive> drives = Utils.getSysAllDriveList();
            String rootDir = drives.get(drives.size() - 1).getP();
            cacheFolder = rootDir + "/audio/";
        }


        for (ResItem item : items) {
            try {

                String cacheFile = cacheFolder + item.getEnText() + ".mp3";

                File file = new File(cacheFile);
                if (!file.exists()) {
                    tts("en", item.getEnText(), null);
                }
                cacheFile = cacheFolder + item.getCnText() + ".mp3";

                file = new File(cacheFile);
                if (!file.exists()) {
                    tts("zh", item.getCnText(), null);
                }

                System.out.println(item.getEnText());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return "OK";
    }

    @GetMapping(value = "/api2/proxy")
    public void proxy(@RequestParam(name = "url") String url, @RequestParam(name = "enText") String enText, HttpResponse resp)
            throws IOException {
        resp.sendRedirect(url);
    }

}
