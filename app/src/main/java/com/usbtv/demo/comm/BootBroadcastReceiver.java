package com.usbtv.demo.comm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.usbtv.demo.MainActivity;

import java.io.IOException;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Intent mainActivityIntent = new Intent(context, MainActivity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mainActivityIntent);

        }

    }

}