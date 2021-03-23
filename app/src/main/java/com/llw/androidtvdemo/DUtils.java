package com.llw.androidtvdemo;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.csvreader.CsvReader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DUtils {

    public static void download(String url){
        Request request = new Request.Builder().url(url).build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
           }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    try {
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        //获取下载的文件的大小
                        long fileSize = response.body().contentLength();
                        long sum = 0;
                        int porSize = 0;
                        InputStream is = response.body().byteStream();

                        ByteArrayOutputStream fos = new ByteArrayOutputStream();
                        while ((len = is.read(bytes)) != -1) {
                            fos.write(bytes);
                        }

                        CsvReader csvReader = new CsvReader(new StringReader(fos.toString()));
                        csvReader.readHeaders();
                        while (csvReader.readRecord()){
                            //System.out.println(csvReader.getRawRecord());
                            VideoItem item = new VideoItem();
                            item.url=csvReader.get("URL");
                            if(item.url.indexOf("https://")==-1&&item.url.indexOf("http://")==-1){
                                item.url = url.split("/playlist")[0]+"/" +item.url;
                            }
                            //item.title=csvReader.get("TAGS");
                            item.title=csvReader.get("TITLE");
                            App.playList.add(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    public static void reloadPlayList() {


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Process process = Runtime.getRuntime().exec("id");
                    InputStream errorInput = process.getErrorStream();
                    InputStream inputStream = process.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String error = "";
                    String result = "";
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(errorInput));
                    while ((line = bufferedReader.readLine()) != null) {
                        error += line;
                    }
                    Log.d("usb",result);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                //scan local playlist
                File localPlaylist =  new File("/storage/udisk0/part1/bilibili/playlist.csv");
                App.playList.clear();
                if(localPlaylist.exists() && localPlaylist.isFile()&&localPlaylist.canRead()){
                    try{
                        CsvReader csvReader = new CsvReader(new FileReader(localPlaylist.getAbsolutePath()));
                        csvReader.readHeaders();
                        while (csvReader.readRecord()){
                            //System.out.println(csvReader.getRawRecord());
                            VideoItem item = new VideoItem();
                            item.url=csvReader.get("URL");
                            if(item.url.indexOf("https://")==-1&&item.url.indexOf("http://")==-1){
                                item.url = "/storage/udisk0/part1/bilibili/"+item.url;

                            }
                            //item.title=csvReader.get("TAGS");
                            item.title=csvReader.get("TITLE");
                            App.playList.add(item);
                        }

                        List<String> list = new ArrayList<String>();
                        File dirFile = new File("/storage/udisk0/part1/bilibili");
                        loopListFile(list, dirFile);
                        for(String file:list) {
                            //System.out.println(file);
                            //String info = file.split("_")[0]+".mp4";
                            //File infoFile = new File(info);
                            //System.out.println("bilibili/"+file.replace("/Volumes/Portable/bilibili/", "")+",test,abc");
                            VideoItem item = new VideoItem();
                            item.url = file;
                            item.title=file;
                            App.playList.add(item);

                        }


                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
                String remotePlayHost = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE)
                        .getString("remoteplaylisthost","http://192.168.0.101/playlist.csv");
                if(!remotePlayHost.trim().equalsIgnoreCase("")){
                    DUtils.download(remotePlayHost);
                }
            }
        }).start();
    }

    public static void loopListFile(List<String> list, File dirFile) {
        File[] files = dirFile.listFiles();
        for(File file:files) {
            if(file.getName().equals(".")||file.getName().equals(".."))
                continue;

            if(file.isDirectory()) {
                loopListFile(list,file);
            }else if(file.isFile() && file.getName().endsWith(".mp4")) {
                list.add(file.getAbsolutePath());
            }
        }
    }

}