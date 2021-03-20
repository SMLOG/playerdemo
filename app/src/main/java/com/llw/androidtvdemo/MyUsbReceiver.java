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
    ArrayList<String> as = new ArrayList<String>();
    private  boolean isMovieSuffix( String fileName) {
        String name=fileName.toLowerCase();

        for (String string : this.suffixs) {
            if (name.endsWith(string)) {
                return true;
            }
        }
        return false;
    }

    private void loopDisk(File file){

        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                String s = files[i];
                String path = file.getAbsolutePath() + "/" + s;
                File f =new File(path);

                if (f.isFile()) {
                    if(isMovieSuffix(s)){
                        as.add(path);
                        VideoItem item = new VideoItem();
                        item.url = path;
                        item.seq = 0;
                        item.times = 0;
                        item.createDate = System.currentTimeMillis();
                        item.lastUpdate = System.currentTimeMillis();
                        item.cat = "";
                        App.playList.add(item);
                    }

                }else {
                    loopDisk(f);
                }
            }
        }
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
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            new Thread() {
                public void run() {
                    File file = new File(featureFilePath);
                    loopDisk(file);
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
