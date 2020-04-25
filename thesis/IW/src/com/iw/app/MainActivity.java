package com.iw.app;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.iw.R;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActivityManager mgr = (ActivityManager) this
                        .getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processes = mgr.getRunningAppProcesses();

        PackageManager pm = getApplicationContext().getPackageManager();

        String out = "";
        for (int i = 0; i < processes.size(); i++) {
            RunningAppProcessInfo process = processes.get(i);
            String name = process.processName;
            int uid = process.uid;

            long rxAll = TrafficStats.getUidRxBytes(uid);
            long txAll = TrafficStats.getUidTxBytes(uid);

            if (rxAll != -1 && txAll != -1) {
                Log.d("DEBUG", name + " (UID: " + uid + ") r:" + rxAll + "/w:"
                                + txAll);

                ApplicationInfo ai;
                try {
                    ai = pm.getApplicationInfo(name, 0);
                } catch (final NameNotFoundException e) {
                    ai = null;
                }
                String applicationName = (String) (ai != null ? pm
                                .getApplicationLabel(ai) : name);
                out += applicationName + " [ r:" + rxAll + "/t:" + txAll
                                + " ]\n";

                // try {
                // Process proc = Runtime.getRuntime().exec(new String[] {"cat",
                // "/proc/uid_stat/" + uid + "/tcp_rcv"});
                // BufferedReader reader = new BufferedReader(new
                // InputStreamReader(proc.getInputStream()));
                // String line = null;
                //
                // while ((line = reader.readLine()) != null)
                // {
                // System.out.println(line);
                // }
                // } catch(Exception e) {
                // }
                // break;
                //
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
