package com.usbtv.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.usbtv.demo.comm.App;

public class BootupActivity extends BroadcastReceiver {
    private static final String TAG = "BootupActivity";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootupActivity initiated");

        boolean startAtBoot = PlayerController.getInstance().configStore.startAtBoot;

        if (startAtBoot && intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().toUpperCase().indexOf("QUICKBOOT_POWERON")>-1) {
            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             context.startActivity(mainActivityIntent);
        }
    }


}