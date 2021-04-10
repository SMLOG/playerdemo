package com.usbtv.demo;

import android.content.Context;

import com.alibaba.fastjson.JSON;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DowloadPlayList {

    public static void download(String url) {
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

                        InputStream is = response.body().byteStream();

                        ByteArrayOutputStream fos = new ByteArrayOutputStream();
                        while ((len = is.read(bytes)) != -1) {
                            fos.write(bytes,0,len);
                        }

                        ArrayList<Aid> aidList= (ArrayList<Aid>) JSON.parseArray(fos.toString(), Aid.class);
                        App.playList.addAll(aidList,url);
                        //System.out.println(aidList);

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

                loadPlayList();
            }



            private void loadPlayList() {

                PlayList newPlayList = new PlayList();
                String s = getDataFilesDir();
                App.playList = newPlayList;

                File localPlaylist = new File(s+"playlist.json");
                if (s!=null && localPlaylist.exists() && localPlaylist.isFile() && localPlaylist.canRead()) {

                    try {
                        String content = Utils.getStringFromFile(localPlaylist.getAbsolutePath());
                        ArrayList<Aid> aidList= (ArrayList<Aid>) JSON.parseArray(content, Aid.class);
                        App.playList.addAll(aidList,"file://"+localPlaylist.getAbsolutePath());

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                List<Aid> aidList = null;
                                try {
                                    aidList = Aid.scan(s);
                                    App.playList.addAll(aidList,"file://"+localPlaylist.getAbsolutePath());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                String remotePlayHost = App.getInstance().getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE)
                        .getString("remoteplaylisthost", "http://192.168.0.101/bilibili/playlist.json");

                if (!remotePlayHost.trim().equalsIgnoreCase("")) {
                    DowloadPlayList.download(remotePlayHost);
                }
            }
        }).start();
    }

    private static String getDataFilesDir() {
        return "/storage/udisk0/part1/bilibili/";
    }


}
