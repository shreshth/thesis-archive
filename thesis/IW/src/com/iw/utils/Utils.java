package com.iw.utils;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

import com.iw.consts.Consts;

public class Utils {

    /**
     * Show a toast notification only in test mode
     */
    public static void toast_test(Context context, String text) {
        if (isTestMode(context)) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
        Log.d("Toast Message", text);
    }

    /**
     * Show a toast notification, even if we are not in test_mode
     */
    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        Log.d("Toast Message", text);
    }

    /**
     * Test mode or not?
     */
    public static boolean isTestMode(Context context) {
        for (String allowed_device : Consts.TEST_DEVICE_WHITELIST) {
            if (getDeviceID(context).equals(allowed_device)) {
                return true;
            }
        }
        return false;
    }

    /**
     * return device ID (unique for each android device)
     */
    public static String getDeviceID(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

}
