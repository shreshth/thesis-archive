package com.iw.pred;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.iw.consts.Consts;
import com.iw.db.LocationDatabaseHelper;
import com.iw.utils.NetworkUtils;
import com.iw.utils.Utils;

public class PredictionRequestReceiver extends BroadcastReceiver {

    private Context mContext;
    private int tolerance;
    private int timer;
    private boolean callbackPending;

    // handler and runnable to scan for wifi
    private Handler scanHandler = new Handler();
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            doWifiScan();
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        // tolerance = intent.getIntExtra("tolerance", 0);
        tolerance = (int) K9DelayToleranceEstimator.predict(context);
        timer = 0;
        callbackPending = true;

        // DEBUG
        Utils.toast(mContext, "tolerance is " + tolerance);

        // callback immediately if wifi is turned off
        if (!NetworkUtils.isEnabledWiFi(mContext)) {
            callback("wifi not enabled");
            return;
        }

        // scan for wifi
        scanHandler.post(scanRunnable);

        // scan for location
        if (callbackPending) {
            doLocationScan();
        }
    }

    private void callback(String reason) {
        if (callbackPending) {
            Intent callback_intent = new Intent("com.iw.callback");
            callback_intent.putExtra("reason", reason);
            mContext.sendBroadcast(callback_intent);
            callbackPending = false;
        }
    }

    private void doLocationScan() {
        LocationManager lm = (LocationManager) mContext
                        .getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingRequired(true);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedRequired(true);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);

        // get current location
        Location location = lm.getLastKnownLocation(lm.getBestProvider(
                        criteria, true));
        if (location == null
                        || !location.hasAccuracy()
                        || location.getAccuracy() >= Consts.MIN_LOCATION_ACCURACY) {
            // if current location outdated/inaccurate
            callback("inaccurate location");
        } else {
            onLocation(location);
        }

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location arg0) {
                // onLocation(arg0);
            }

            @Override
            public void onProviderDisabled(String arg0) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status,
                            Bundle extras) {
            }
        };
        try {
            // lm.requestSingleUpdate(criteria, locationListener, null);
        } catch (Exception e) {
            callback("location not available");
        }
    }

    private void onLocation(Location location) {
        // get prediction
        int time_to_wifi = LocationDatabaseHelper.predict(mContext,
                        location.getLatitude(), location.getLongitude(),
                        location.getBearing(), location.getSpeed(),
                        location.getAccuracy(), System.currentTimeMillis());

        Log.d("DEBUG", "Wifi: " + time_to_wifi + ", Tolerance: " + tolerance);

        // if prediction is too long, then callback
        if (time_to_wifi > tolerance - timer) {
            callback("wifi will not be available soon");
        } else {
            // display prediction
            if (callbackPending) {
                Utils.toast(mContext, "Wifi unavailable, but expected in "
                                + Integer.toString(time_to_wifi - timer)
                                + " seconds");
            }
        }
    }

    private void doWifiScan() {
        // scan
        WifiManager wm = (WifiManager) mContext
                        .getSystemService(Context.WIFI_SERVICE);
        wm.startScan();

        // if wifi is connected, then callback
        if (NetworkUtils.isConnectedWiFi(mContext)
                        && wm.getConnectionInfo().getRssi() > Consts.MIN_WIFI_RSSI) {
            Log.d("DEBUG", "WIFI FOUND");
            callback("wifi found");
        } else {
            // increment timer
            timer = timer + Consts.WIFI_SCAN_FREQUENCY;

            // callback if timer > tolerance
            if (timer > tolerance) {
                callback("timer expired");
            } else {
                // prepare for next scan
                scanHandler.postDelayed(scanRunnable,
                                Consts.WIFI_SCAN_FREQUENCY * 1000);
            }
        }
    }
}