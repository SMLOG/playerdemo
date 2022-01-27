package com.usbtv.demo.comm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(App.CMD)) {

            String cmd = intent.getExtras().getString("cmd");
            String val = intent.getExtras().getString("val");
            if ("play".equals(cmd)) {
                PlayerController.getInstance().play();

            }
        }
    }
}
