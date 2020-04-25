package com.iw.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WaitAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MarkovService.onNoResult(context);
    }

}