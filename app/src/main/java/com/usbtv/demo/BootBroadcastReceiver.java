package com.usbtv.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.usbtv.demo.comm.NetUtils;

import java.io.IOException;
import java.sql.SQLException;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {


            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);

        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {

            App.initDisks();

            new Thread() {
                public void run() {


                    DowloadPlayList.loadPlayList(true);

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }.start();
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
            App.initDisks();
           // DowloadPlayList.loadPlayList(true);

        } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            final int netWorkState = NetUtils.getNetWorkState(context);
            if (netWorkState == 0) {
            } else if (netWorkState == 1) {
            } else {
            }
        }

    }

}