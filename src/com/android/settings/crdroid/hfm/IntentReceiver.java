package com.android.settings.crdroid.hfm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class IntentReceiver extends BroadcastReceiver {

    private static final String TAG = "HFMBootReceiver";

    public static final String ACTION_RUN_BOOTCOMPLETE = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Started Receiver");
        Intent serv = new Intent(context, CheckHosts.class);
        serv.setAction(intent.getAction());
        serv.putExtras(intent);
        context.startService(serv);
    }
}
