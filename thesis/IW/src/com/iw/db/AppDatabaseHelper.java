package com.iw.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.iw.consts.Consts;
import com.iw.pred.K9DelayToleranceEstimator;
import com.iw.utils.StringUtils;

public class AppDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 6; // change uid to integer

    // Database Name
    private static final String DATABASE_NAME = "droidtn_app";

    // Table name for network logs
    private static final String TABLE_NETWORK_STATS = "network_stats";

    // Table name for last seen log values
    private static final String TABLE_LAST_SEEN = "last_seen";

    // Column names
    public static final String KEY_ID = "id";
    public static final String KEY_APP_NAME = "app_name";
    public static final String KEY_APP_UID = "app_uid";
    public static final String KEY_TCP_RX_BYTES = "tcp_rx_bytes";
    public static final String KEY_TCP_TX_BYTES = "tcp_tx_bytes";
    public static final String KEY_UDP_RX_BYTES = "udp_rx_bytes";
    public static final String KEY_UDP_TX_BYTES = "udp_tx_bytes";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final int NUM_COLUMNS = 8;

    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_NETWORK_STATS_TABLE = "CREATE TABLE "
                        + TABLE_NETWORK_STATS + "(" + KEY_ID
                        + " INTEGER PRIMARY KEY," + KEY_APP_NAME + " STRING,"
                        + KEY_APP_UID + " INTEGER," + KEY_TCP_RX_BYTES
                        + " INTEGER," + KEY_TCP_TX_BYTES + " INTEGER,"
                        + KEY_UDP_RX_BYTES + " INTEGER," + KEY_UDP_TX_BYTES
                        + " INTEGER," + KEY_TIMESTAMP + " INTEGER" + ")";
        db.execSQL(CREATE_NETWORK_STATS_TABLE);

        String CREATE_LAST_SEEN_TABLE = "CREATE TABLE " + TABLE_LAST_SEEN + "("
                        + KEY_ID + " INTEGER PRIMARY KEY," + KEY_APP_NAME
                        + " STRING," + KEY_APP_UID + " INTEGER,"
                        + KEY_TCP_RX_BYTES + " INTEGER," + KEY_TCP_TX_BYTES
                        + " INTEGER," + KEY_UDP_RX_BYTES + " INTEGER,"
                        + KEY_UDP_TX_BYTES + " INTEGER," + KEY_TIMESTAMP
                        + " INTEGER" + ")";
        db.execSQL(CREATE_LAST_SEEN_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NETWORK_STATS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAST_SEEN);

        // Create tables again
        onCreate(db);
    }

    /**
     * Get the number of rows in the database
     */
    private int getNumRows() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NETWORK_STATS;

        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(sql);
        int count = (int) statement.simpleQueryForLong();
        db.close(); // Closing database connection

        return count;
    }

    // add many data points
    private void addPoints(List<AppDataPoint> points, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (AppDataPoint point : points) {
            // if this data point has already been seen, then skip

            if (exists(point)) {
                continue;
            }

            ContentValues values = new ContentValues();
            values.put(KEY_APP_NAME, point.getAppName());
            values.put(KEY_APP_UID, point.getUID());
            values.put(KEY_TCP_RX_BYTES, point.getTcpRxBytes());
            values.put(KEY_TCP_TX_BYTES, point.getTcpTxBytes());
            values.put(KEY_UDP_RX_BYTES, point.getUdpRxBytes());
            values.put(KEY_UDP_TX_BYTES, point.getUdpTxBytes());
            values.put(KEY_TIMESTAMP, point.getTimestamp());

            // insert row
            db.insert(TABLE_NETWORK_STATS, null, values);

            // update last seen value
            updateLastSeen(point);

            K9DelayToleranceEstimator.addDataPoint(point, context);
        }

        try {
            db.close(); // Closing database connection
        } catch (Exception e) {

        }
    }

    // check if this point already exists in the last seen table
    private boolean exists(AppDataPoint point) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_LAST_SEEN + " WHERE "
                        + KEY_APP_NAME + "=\"" + point.getAppName() + "\" AND "
                        + KEY_APP_UID + "=" + point.getUID() + " AND "
                        + KEY_TCP_RX_BYTES + "=" + point.getTcpRxBytes()
                        + " AND " + KEY_TCP_TX_BYTES + "="
                        + point.getTcpTxBytes() + " AND " + KEY_UDP_RX_BYTES
                        + "=" + point.getUdpRxBytes() + " AND "
                        + KEY_UDP_TX_BYTES + "=" + point.getUdpTxBytes();

        Cursor cursor = db.rawQuery(selectQuery, null);

        // if the cursor is non-empty, then it exists
        return cursor.moveToFirst();

        // no need to call db.close() since it will be closed in the calling
        // function
    }

    // update the last seen log for this app
    private void updateLastSeen(AppDataPoint point) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_APP_NAME, point.getAppName());
        values.put(KEY_APP_UID, point.getUID());
        values.put(KEY_TCP_RX_BYTES, point.getTcpRxBytes());
        values.put(KEY_TCP_TX_BYTES, point.getTcpTxBytes());
        values.put(KEY_UDP_RX_BYTES, point.getUdpRxBytes());
        values.put(KEY_UDP_TX_BYTES, point.getUdpTxBytes());
        values.put(KEY_TIMESTAMP, point.getTimestamp());

        String whereClause = KEY_APP_NAME + "=? AND " + KEY_APP_UID + "=?";
        String[] whereArgs = { point.getAppName(),
                        Long.toString(point.getUID()) };

        // update values
        int rowsAffected = db.update(TABLE_LAST_SEEN, values, whereClause,
                        whereArgs);

        // if nothing affected, add a new row
        if (rowsAffected == 0) {
            db.insert(TABLE_LAST_SEEN, null, values);
        }

        // no need to call db.close() since it will be closed in the calling
        // function
    }

    /**
     * Retrieve a few data points and remove them from the database
     * 
     * @return Returns a comma separated string of fields
     */
    private Map<String, String> popFew() {
        Map<String, String> data = new HashMap<String, String>(2 * NUM_COLUMNS);

        List<String> appNameList = new ArrayList<String>(
                        2 * Consts.HTTP_BATCH_LIMIT);
        List<String> uidList = new ArrayList<String>(
                        2 * Consts.HTTP_BATCH_LIMIT);
        List<String> tcpRxBytesList = new ArrayList<String>(
                        2 * Consts.HTTP_BATCH_LIMIT);
        List<String> tcpTxBytesList = new ArrayList<String>(
                        2 * Consts.HTTP_BATCH_LIMIT);
        List<String> udpRxBytesList = new ArrayList<String>(
                        2 * Consts.HTTP_BATCH_LIMIT);
        List<String> udpTxBytesList = new ArrayList<String>(
                        2 * Consts.HTTP_BATCH_LIMIT);
        List<String> timestampList = new ArrayList<String>(
                        2 * Consts.HTTP_BATCH_LIMIT);

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NETWORK_STATS
                        + " ORDER BY " + KEY_TIMESTAMP + " ASC LIMIT "
                        + Consts.HTTP_BATCH_LIMIT;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        long greatestTimeStamp = 0;
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                long timestamp = Long.parseLong(cursor.getString(7));
                if (timestamp > greatestTimeStamp) {
                    greatestTimeStamp = timestamp;
                }

                appNameList.add(cursor.getString(1));
                uidList.add(cursor.getString(2));
                tcpRxBytesList.add(cursor.getString(3));
                tcpTxBytesList.add(cursor.getString(4));
                udpRxBytesList.add(cursor.getString(5));
                udpTxBytesList.add(cursor.getString(6));
                timestampList.add(cursor.getString(7));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Delete retrieved points
        db.delete(TABLE_NETWORK_STATS, KEY_TIMESTAMP + " <= ?",
                        new String[] { Long.toString(greatestTimeStamp) });
        db.close(); // Closing database connection

        data.put(KEY_APP_NAME, StringUtils.implode(
                        appNameList.toArray(new String[0]), ","));
        data.put(KEY_APP_UID, StringUtils.implode(
                        uidList.toArray(new String[0]), ","));
        data.put(KEY_TCP_RX_BYTES, StringUtils.implode(
                        tcpRxBytesList.toArray(new String[0]), ","));
        data.put(KEY_TCP_TX_BYTES, StringUtils.implode(
                        tcpTxBytesList.toArray(new String[0]), ","));
        data.put(KEY_UDP_RX_BYTES, StringUtils.implode(
                        udpRxBytesList.toArray(new String[0]), ","));
        data.put(KEY_UDP_TX_BYTES, StringUtils.implode(
                        udpTxBytesList.toArray(new String[0]), ","));
        data.put(KEY_TIMESTAMP, StringUtils.implode(
                        timestampList.toArray(new String[0]), ","));

        return data;
    }

    // --------------- Synchronized access to whole class
    // ----------------------------
    public synchronized static int getNumRows(Context context) {
        AppDatabaseHelper db = new AppDatabaseHelper(context);
        return db.getNumRows();
    }

    public synchronized static void addPoints(Context context,
                    List<AppDataPoint> points) {
        AppDatabaseHelper db = new AppDatabaseHelper(context);
        db.addPoints(points, context);
    }

    public synchronized static Map<String, String> popFew(Context context) {
        AppDatabaseHelper db = new AppDatabaseHelper(context);
        return db.popFew();
    }
}