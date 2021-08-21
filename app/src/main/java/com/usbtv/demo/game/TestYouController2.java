package com.usbtv.demo.game;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.usbtv.demo.App;
import com.usbtv.demo.comm.HttpGet;
import com.usbtv.demo.comm.Utils;
import com.usbtv.demo.data.CnDict;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.His;
import com.usbtv.demo.data.ResItem;
import com.usbtv.demo.translate.TransApi;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.http.HttpResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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


    @com.yanzhenjie.andserver.annotation.ResponseBody
    @GetMapping(value = "/api3/randList")
    public String randList(@RequestParam(name = "n") int n,@RequestParam(name = "langId",required = false,defaultValue = "0") int langId) throws SQLException {

        Map<String,Object> ret = new HashMap();


        if(langId==0){
            langId =1+(int) (Math.random() * 1);
        }


        Dao<His, Integer> dao = App.getHelper().getDao(His.class);



        long maxId =0;

        List<His> list = new ArrayList<His>();

        List<His> cacheList = this.cacheHisMap.get(new Integer(langId));
        if(cacheList ==null || cacheList.size()<=10){
            List<His> retList = dao.queryBuilder().where().eq("langId", langId)
                    //.and()
                    //.lt("rTimes", 5)
                    .queryBuilder()
                    .orderBy("orderN", true)
                    .orderBy("level", true)
                   // .orderBy("rTimes", true)
                    .limit(30l).query();
            this.cacheHisMap.put(new Integer(langId),retList);

             cacheList = this.cacheHisMap.get(new Integer(langId));

        }

        Set<Integer> set = new HashSet();

        maxId =  cacheList.size();

        ret.put("langId",langId);
        if(maxId<=n){
            ret.put("list",cacheList);
            return JSON.toJSONString(ret);

        }

        while (true) {
            int nextId = (int) (Math.random() * maxId);
            if (set.contains(new Integer(nextId)))
                continue;
            set.add(nextId);
            list.add(cacheList.get(nextId));

            if (list.size() == n)
                break;
        }

        ret.put("list",list);
        return JSON.toJSONString(ret);
    }


    private boolean isBlank(String str){
        return str==null||str.trim().equals("");
    }
    @com.yanzhenjie.andserver.annotation.ResponseBody
    @PostMapping(value = "/api3/save")
    public String save(com.yanzhenjie.andserver.http.RequestBody content) throws Exception {
        His subject = JSON.parseObject(content.string(), His.class);

        Dao<His, Integer> subjectDao = App.getHelper().getDao(His.class);

        List<His> list = this.cacheHisMap.get(new Integer(subject.getLangId()));

        int index = -1;
        for(int i=0;i<list.size();i++){
            if(list.get(i).getId()==subject.getId()){
                list.remove(i);
                index = i;
                break;
            }
        }

        if(subject.getrTimes()>5){
            subject.setrTimes(0);
            subject.setOrderN(subject.getOrderN()+1);
           // if(index>-1)list.remove(index);
            //subject.setUpdateTime(System.currentTimeMillis());
            //subjectDao.update(subject);
        }else{
            list.add(subject);
        }

        subject.setUpdateTime(System.currentTimeMillis());
        subjectDao.update(subject);

        return JSON.toJSONString(subject);

    }
    @PostMapping(path = "/api3/del")
    String delRes(@RequestParam(name = "id") int id) {
        try {
            Dao<His, Integer> dao = App.getHelper().getDao(His.class);
            dao.deleteById(id);

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    @GetMapping(path = "/api3/say")
    public void say(@RequestParam(name = "str") String str) {

        //exec("say  " + str);

        // exec("say "+ list.get(result.getCurIndex()).getEnText());

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


    @GetMapping(path = "/api3/ttscacheall")
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

    @GetMapping(value = "/api3/proxy")
    public void proxy(@RequestParam(name = "url") String url, @RequestParam(name = "enText") String enText, HttpResponse resp)
            throws IOException {
        resp.sendRedirect(url);
    }



    @ResponseBody
    @GetMapping("/api3/trf")
    public String trf() throws Exception {

      //  TransApi api = new TransApi();

        Dao<ResItem, Integer> dao = App.getHelper().getDao(ResItem.class);
        Dao<CnDict, Integer> cnDictdao = App.getHelper().getDao(CnDict.class);
        Dao<His, Integer> hisDao = App.getHelper().getDao(His.class);
        List<ResItem> list = dao.queryForAll();
        for(ResItem item:list){

            //String transResult = api.getTransResult(item.getCnText(), "zh", "jp");
            //item.setJpText(transResult);

            //transResult = api.getTransResult(item.getCnText(), "zh", "en");
            //item.setEnText(transResult);

            CnDict cn = cnDictdao.queryBuilder().where().eq("cnText",item.getCnText().trim()).queryForFirst();
            if(cn==null){
                cn= new CnDict();
                cn.setCnText(item.getCnText().trim());
            }
            cn.setImgUrl(item.getImgUrl());
            cnDictdao.createOrUpdate(cn);

            String enText = item.getEnText().trim();
            His en = hisDao.queryBuilder().where().eq("langId",His.LANG_EN)
                    .and().eq("langText", enText).queryForFirst();
            if(en==null){

                en = new His();
                en.setCn(cn);
                en.setLangId(His.LANG_EN);
                en.setLangText(enText);
                en.setLevel(enText.length());

            }
            hisDao.createOrUpdate(en);

            String jpText = item.getJpText().trim();

            His jp = hisDao.queryBuilder().where().eq("langId",His.LANG_JP)
                    .and().eq("langText", jpText).queryForFirst();
            if(jp==null){

                jp = new His();
                jp.setCn(cn);
                jp.setLangId(His.LANG_JP);
                jp.setLangText(jpText);
                jp.setLevel(jpText.length());

            }

            hisDao.createOrUpdate(jp);


        }

        return "done";
    }

}
