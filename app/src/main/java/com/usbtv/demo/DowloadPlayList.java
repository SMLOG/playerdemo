package com.usbtv.demo;

import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.usbtv.demo.data.Drive;
import com.usbtv.demo.data.Folder;
import com.usbtv.demo.data.VFile;

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
                }

            }).start();
        } else {
            reLoadPlayList();
        }

    }


    public static void scanToDB() {



            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        Dao<Folder, Integer> folderDao = App.getHelper().getDao(Folder.class);
                        Dao<VFile, Integer> vfDao = App.getHelper().getDao(VFile.class);
                        // if the path changed,clean all data and re-scan again
                        Folder testFolder = folderDao.queryBuilder().where().isNotNull("root_id").queryForFirst();
                        if(! testFolder.exists()){
                            Dao<Drive, Integer> driveDao = App.getHelper().getDao(Drive.class);

                            List<Folder> folders = folderDao.queryBuilder().where().isNotNull("root_id").query();
                            for(Folder f:folders){
                                vfDao.delete(f.getFiles());
                                folderDao.delete(f);
                            }
                            driveDao.deleteBuilder().delete();
                            Aid.scanAllDrive();

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return;
        }


    public static void reLoadPlayList() {

        scanToDB();

    }



}
