package com.usbtv.demo.r;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RunOnInstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().endsWith("INITIALIZE_PROGRAMS")) {
            new InitChannel();
        }
    }
}
