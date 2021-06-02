package com.usbtv.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.usbtv.demo.comm.NetUtils;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);

        }else if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED))
        {

           App.setMediaMounted(true);
            //Utils.simulateKeystroke(4);
            //Utils.execLocalCmdByAdb("input keyevent 4");
           // execLocalCmdByAdb

           // App.sendExit();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new Thread() {
                public void run() {
                    // Utils.exec("input keyevent 4");
                    //Utils.exec("input touchscreen tap 300 300");

                   /* Instrumentation mInst = new Instrumentation();
                    mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_4);

                    DUtils.reloadPlayList();
                    //loopDisk(file);*/

                    //Toast.makeText(context, "loading", Toast.LENGTH_SHORT).show();
                    Log.d(App.TAG,"load usb.........");

                    int i = 20;
                    for(;i>0&&App.isMediaMounted();i++){
                        DowloadPlayList.loadPlayList(true);
                        try {
                            if(App.playList.getAidList().size()>0)break;
                            Thread.sleep(5000);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                   // if(!isForeground(context)){
                        Intent intent=new Intent(context, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    //};


                    //App.sendPlayBroadCast(-1,-1);


                }
            }.start();
        }else if(intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)){
            App.setMediaMounted(false);

        } else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            final int netWorkState = NetUtils.getNetWorkState(context);
            if (netWorkState == 0) {
                DowloadPlayList.loadPlayList(true);
            } else if (netWorkState == 1) {
            } else {
            }
        }

    }

}