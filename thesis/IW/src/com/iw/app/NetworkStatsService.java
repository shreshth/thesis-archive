package com.iw.app;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;
import android.os.IBinder;

import com.iw.consts.Consts;
import com.iw.db.AppDataPoint;
import com.iw.db.AppDatabaseHelper;

public class NetworkStatsService extends Service {

    private static final int SCHEDULED_ALARM_CODE = 103;

    private static boolean mServiceRunning;

    /**
     * on starting the service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mServiceRunning == false) {
            // set up an alarm for every data point
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent newintent = new Intent(this, AppScheduledAlarmReceiver.class);
            PendingIntent operation = PendingIntent.getBroadcast(this,
                            SCHEDULED_ALARM_CODE, newintent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis(),
                            Consts.TIME_GRANULARITY_MILLIS, operation);

            mServiceRunning = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * on destroying the service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mServiceRunning = false;
    }

    /**
     * Is the data collection service running
     */
    public static boolean isServiceRunning() {
        return mServiceRunning;
    }

    /**
     * Stop collecting data
     */
    public static void stopService(Context context) {
        context.stopService(new Intent(context, NetworkStatsService.class));
        AlarmManager am = (AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);
        Intent newintent = new Intent(context, AppScheduledAlarmReceiver.class);
        PendingIntent operation = PendingIntent.getBroadcast(context,
                        SCHEDULED_ALARM_CODE, newintent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(operation);
    }

    /**
     * Start collecting data
     */
    public static void startService(Context context) {
        context.startService(new Intent(context, NetworkStatsService.class));
    }

    /**
     * Scheduled alarm, so start collecting network data
     */
    public synchronized static void onAlarm(Context context) {
        ActivityManager mgr = (ActivityManager) context
                        .getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processes = mgr.getRunningAppProcesses();

        PackageManager pm = context.getPackageManager();

        List<AppDataPoint> points = new ArrayList<AppDataPoint>(
                        2 * processes.size());

        // find information for each process
        for (int i = 0; i < processes.size(); i++) {
            RunningAppProcessInfo process = processes.get(i);

            String procName = process.processName;
            int uid = process.uid;

            long rxAll = TrafficStats.getUidRxBytes(uid);
            long txAll = TrafficStats.getUidTxBytes(uid);

            // if this app never uses network, then ignore
            if (rxAll == -1 && txAll == -1) {
                continue;
            }

            // get name
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo(procName, 0);
            } catch (final NameNotFoundException e) {
                ai = null;
            }
            String applicationName = (String) (ai != null ? pm
                            .getApplicationLabel(ai) : procName);

            // get traffic stats
            long tcpRxBytes = TrafficStats.getUidTcpRxBytes(uid);
            long tcpTxBytes = TrafficStats.getUidTcpTxBytes(uid);
            long udpRxBytes = TrafficStats.getUidUdpRxBytes(uid);
            long udpTxBytes = TrafficStats.getUidUdpTxBytes(uid);

            // add to list of points
            AppDataPoint point = new AppDataPoint(applicationName, uid,
                            tcpRxBytes, tcpTxBytes, udpRxBytes, udpTxBytes);
            points.add(point);
        }

        if (!points.isEmpty()) {
            AppDatabaseHelper.addPoints(context, points);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
