package com.iw.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationScheduledAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MarkovService.onAlarm(context);
    }

}