package com.llw.androidtvdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class MyUsbReceiver extends BroadcastReceiver {
    private String TAG="MyUsbReceiver";
    private String[] suffixs;
    private  boolean isMovieSuffix( String fileName) {
        String name=fileName.toLowerCase();

        for (String string : this.suffixs) {
            if (name.endsWith(string)) {
                return true;
            }
        }
        return false;
    }

    private  File findCacheFolder(File file, String androidviedocache, int n){

        if(file.isDirectory()){
            if(file.getName().equalsIgnoreCase(androidviedocache)){
                return file;
            }else{

                for(int i=0;i<n;i++){
                    ArrayList<File> list = new ArrayList<File>();
                    for(File c:file.listFiles()){
                        if(c.isDirectory()){
                            if(c.getName().equalsIgnoreCase(androidviedocache))return c;
                            list.add(c);
                        }
                    }
                }
            }

        }


        return  null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context mContext = context;

        this.suffixs = context.getResources().getStringArray(
                R.array.video_type_suffix);

        String action = intent.getAction();
        Uri uri = intent.getData();
        final String path = uri.getPath();
        final String featureFilePath=path+"/English";
        if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
            App.updateCacheFolder(null);

        }else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            //发送返回按键
            Utils.setKeyPress(4);

           //File cacheFolder = findCacheFolder(new File(featureFilePath),"androidviedocache",3);
           App.updateCacheFolder(new File(path+"/part0/androidviedocache"));

            new Thread() {
                public void run() {
                    File file = new File(featureFilePath);
                    //loopDisk(file);
                    Intent intent=new Intent(mContext, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   // intent.putStringArrayListExtra("ff",as);
                    mContext.startActivity(intent);
                        Log.d(TAG, featureFilePath+" is not exist.");

                }
            }.start();

        }

    }

}
