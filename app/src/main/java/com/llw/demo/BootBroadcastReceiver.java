package com.llw.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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


            //Utils.simulateKeystroke(4);
            Utils.exec("input keyevent 4");
            new Thread() {
                public void run() {
                    // Utils.exec("input keyevent 4");
                    //Utils.exec("input touchscreen tap 300 300");

                   /* Instrumentation mInst = new Instrumentation();
                    mInst.sendKeyDownUpSync(KeyEvent.KEYCODE_4);

                    DUtils.reloadPlayList();
                    //loopDisk(file);*/

                    //Toast.makeText(context, "loading", Toast.LENGTH_SHORT).show();

                    DowloadPlayList.reloadPlayList();
                    Intent intent=new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                    App.sendPlayBroadCast(-1,-1);


                }
            }.start();
        }
    }

}