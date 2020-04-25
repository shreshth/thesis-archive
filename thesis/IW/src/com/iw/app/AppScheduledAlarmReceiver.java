package com.iw.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppScheduledAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkStatsService.onAlarm(context);
    }
}
