package com.usbtv.demo;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.usbtv.demo.comm.Utils;

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
                            fos.write(bytes, 0, len);
                        }

                        ArrayList<Aid> aidList = (ArrayList<Aid>) JSON.parseArray(fos.toString(), Aid.class);
                        App.playList.addAll(aidList, url);
                        //System.out.println(aidList);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    public static void loadPlayList(boolean isUsingThread) {

        if (isUsingThread) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    reLoadPlayList();
                    Utils.exec("cd " + getDataFilesDir() + " && wget -O - http://192.168.0.101/bilibili/index.cgi|sh - ");
                    reLoadPlayList();
                    //Utils.exec("cd "+getDataFilesDir()+" && wget -q -O - http://192.168.0.101/bilibili/index.cgi|sh");
                }

            }).start();
        } else {
            Utils.exec("cd " + getDataFilesDir() + " && wget -O - http://192.168.0.101/bilibili/index.cgi|sh - ");
            reLoadPlayList();
        }
        App.schedule(-1, 0);

    }


    public static void reLoadPlayList() {
        String s = getDataFilesDir();

        File localPlaylist = new File(s + "playlist.json");
        if (s != null && localPlaylist.getParentFile().isDirectory()) {
            Log.d(App.TAG, localPlaylist.getAbsolutePath() + " exists");

            if (localPlaylist.exists() && localPlaylist.isFile() && localPlaylist.canRead()) {
                try {
                    String content = Utils.getStringFromFile(localPlaylist.getAbsolutePath());
                    ArrayList<Aid> aidList = (ArrayList<Aid>) JSON.parseArray(content, Aid.class);
                    App.playList.addAll(aidList, "file://" + localPlaylist.getAbsolutePath());


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(App.TAG, e.getMessage());

                }
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Aid> aidList = null;
                    try {
                        aidList = Aid.scan(s);
                        App.playList.addAll(aidList, "file://" + localPlaylist.getAbsolutePath());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } else {
            Log.d(App.TAG, localPlaylist.getAbsolutePath() + " not exists");

        }

    }

    public static String getDataFilesDir() {
        String path = Utils.getExtendedMemoryPath(App.getInstance().getApplicationContext());
        if (path != null && new File(path).exists()) return path + "/";
        else {
            return App.getInstance().getCacheDir() + "/";

        }

    }


}
