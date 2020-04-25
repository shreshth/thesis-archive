package com.iw.pred;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.iw.consts.Consts;
import com.iw.db.AppDataPoint;

public class K9DelayToleranceEstimator {
    // preferences file
    private static final String KEY_PREFS = "K9_dt_estimate";

    // number of bytes sent by k-9 so far, and time stamp of last measurement
    private static final String KEY_TOTAL_BYTES = "total_bytes";
    private static final String KEY_TOTAL_BYTES_TIMESTAMP = "total_bytes_timestamp";

    // delay tolerance estimate so far
    private static final String KEY_DELAY_TOLERANCE = "delay_tolerance";

    // number of rounds run
    private static final String KEY_TOTAL_ROUNDS = "num_rounds";

    // app name
    private static final String APP_NAME = "K-9 Mail (DroiDTN)";

    public static void addDataPoint(AppDataPoint point, Context context) {
        if (!point.getAppName().equals(APP_NAME)) {
            return;
        }
        Log.d("DEBUG", point.getAppName());

        SharedPreferences sharedPref = context.getSharedPreferences(KEY_PREFS,
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // if the app has not sent more data, then return immediately
        long curTotal = point.getTcpTxBytes() + point.getUdpTxBytes();
        long pastTotal = sharedPref.getLong(KEY_TOTAL_BYTES, 0);
        if (curTotal <= pastTotal) {
            return;
        }

        // otherwise...
        long curTimestamp = point.getTimestamp();
        long pastTimestamp = sharedPref.getLong(KEY_TOTAL_BYTES_TIMESTAMP,
                        curTimestamp);

        // init
        if (!sharedPref.contains(KEY_DELAY_TOLERANCE)) {
            editor.putFloat(KEY_DELAY_TOLERANCE, 0.0F);
            editor.putLong(KEY_TOTAL_BYTES, curTotal);
            editor.putLong(KEY_TOTAL_BYTES_TIMESTAMP, curTimestamp);
            editor.putInt(KEY_TOTAL_ROUNDS, 0);
            editor.commit();
        }

        // calculate new delay tolerance as weighted average
        double pastDelayTolerance = sharedPref.getFloat(KEY_DELAY_TOLERANCE,
                        0.0F);
        double newDelayTolerance = (curTimestamp - pastTimestamp) / 1000.0;
        double curDelayTolerance = (Consts.DELAY_TOLERANCE_FACTOR * newDelayTolerance)
                        + ((1 - Consts.DELAY_TOLERANCE_FACTOR) * pastDelayTolerance);
        Log.d("DEBUGSOS", (newDelayTolerance / pastDelayTolerance) + " : "
                        + newDelayTolerance + " : " + pastDelayTolerance);

        // don't consider changes that are too close together
        if (Consts.DELAY_TOLERANCE_TIME_CUTOFF) {
            if (newDelayTolerance < pastDelayTolerance
                            && newDelayTolerance / pastDelayTolerance < Consts.DELAY_TOLERANCE_TIME_MIN_FACTOR) {
                return;
            }
        }

        // if not in the first few rounds
        int num_rounds = sharedPref.getInt(KEY_TOTAL_ROUNDS, Integer.MAX_VALUE);
        if (Consts.DELAY_TOLERANCE_CHANGE_CUTOFF
                        && num_rounds > Consts.PREDICTION_CLAMP_MIN_ROUNDS) {
            // clamp changes in delay tolerance to at most some factor
            if (pastDelayTolerance != 0) {
                double changeFactor = ((double) curDelayTolerance - (double) pastDelayTolerance)
                                / (double) pastDelayTolerance;
                if (Math.abs(changeFactor) > Consts.MAX_CHANGE_FACTOR
                                && changeFactor < 0) {
                    // double factor = (changeFactor < 0) ? 1.0 -
                    // Consts.MAX_CHANGE_FACTOR
                    // : 1.0 + Consts.MAX_CHANGE_FACTOR;
                    // curDelayTolerance = factor * pastDelayTolerance;
                    curDelayTolerance = (1.0 - Consts.MAX_CHANGE_FACTOR)
                                    * pastDelayTolerance;
                }
            } else {
                // if no delay tolerance recorded, then just clamp it to some
                // max value
                curDelayTolerance = Math.min(curDelayTolerance,
                                Consts.MAX_INIT_CHANGE);
            }
        }

        // clamp to max value
        if (curDelayTolerance >= Consts.MAX_DELAY_TOLERANCE_VALUE) {
            curDelayTolerance = Consts.MAX_DELAY_TOLERANCE_VALUE;
        }

        editor.putFloat(KEY_DELAY_TOLERANCE, (float) curDelayTolerance);
        editor.putLong(KEY_TOTAL_BYTES, curTotal);
        editor.putLong(KEY_TOTAL_BYTES_TIMESTAMP, curTimestamp);
        editor.putInt(KEY_TOTAL_ROUNDS, num_rounds + 1);
        editor.commit();

        Log.d("DEBUGLOL", num_rounds + " : " + curTimestamp + " : "
                        + curDelayTolerance + " : " + curTotal);
        Log.d("DEBUGSAVE1", curTimestamp + "");
        Log.d("DEBUGSAVE2", curDelayTolerance + "");
    }

    public static double predict(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(KEY_PREFS,
                        Context.MODE_PRIVATE);
        return sharedPref.getFloat(KEY_DELAY_TOLERANCE, 0.0F);
    }
}
