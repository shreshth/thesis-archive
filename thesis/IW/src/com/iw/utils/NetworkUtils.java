package com.iw.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class NetworkUtils {
    /**
     * is WiFi connected
     */
    public static boolean isConnectedWiFi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] ni = cm.getAllNetworkInfo();
        for (NetworkInfo n : ni) {
            if (n.getTypeName().equalsIgnoreCase("wifi") && n.isConnected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * is WiFi enabled
     * 
     * @return
     */
    public static boolean isEnabledWiFi(Context context) {
        WifiManager wm = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
        return wm.isWifiEnabled();
    }

    /**
     * is mobile data connected?
     */
    public static boolean isConnectedMobile(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] ni = cm.getAllNetworkInfo();
        for (NetworkInfo n : ni) {
            if (n.getTypeName().equalsIgnoreCase("mobile") && n.isConnected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * stop WiFi, if enabled
     */
    public static void disableWiFi(Context context) {
        WifiManager wm = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
        if (wm.isWifiEnabled()) {
            wm.setWifiEnabled(false);
        }
    }

    /**
     * start WiFi, if disabled
     */
    public static void enableWiFi(Context context) {
        WifiManager wm = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
        }
    }

    /**
     * Stop 3G
     */
    public static void stop3G(Context context) {
        // impossibru
    }
}
