package com.llw.androidtvdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class MyUsbReceiver extends BroadcastReceiver {
    private String TAG="MyUsbReceiver";
    ArrayList<String> as = new ArrayList<String>();
    private  boolean isMovieSuffix(Context context, String fileName) {
        //判断是否是视频文件
        String name=fileName.toLowerCase();
        String[] suffixs = context.getResources().getStringArray(
                R.array.video_type_suffix);
        for (String string : suffixs) {
            if (name.endsWith(string)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context mContext = context;
        String action = intent.getAction();
        Uri uri = intent.getData();
        final String path = uri.getPath();
        final String featureFilePath=path+"/English";
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            new Thread() {
                public void run() {
                    File file = new File(featureFilePath);
                    if (file.exists()&&file.isDirectory()) {
                        String[] files = file.list();
                        for(int i = 0;i<files.length;i++){
                            String s = files[i];
                            if(isMovieSuffix(mContext,s)){
                                String path = featureFilePath+"/"+s;
                                as.add(path);
                                MyApplication.urls.add(path);
                            }
                        }
                        Intent intent=new Intent(mContext, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putStringArrayListExtra("ff",as);
                        mContext.startActivity(intent);
                    }else{
                        Log.d(TAG, featureFilePath+" is not exist.");
                    }
                }
            }.start();

        }

    }

}
