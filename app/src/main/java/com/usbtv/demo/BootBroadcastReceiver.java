package com.usbtv.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            /*
            Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);*/

        }else if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED))
        {

           App.setMediaMounted(true);
            //Utils.simulateKeystroke(4);
            //Utils.execLocalCmdByAdb("input keyevent 4");
           // execLocalCmdByAdb
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
                        DowloadPlayList.reloadPlayList();
                        try {
                            if(App.playList.getAidList().size()>0)break;
                            Thread.sleep(5000);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Intent intent=new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                    App.sendPlayBroadCast(-1,-1);


                }
            }.start();
        }else if(intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)){
            App.setMediaMounted(false);

        }
    }

}