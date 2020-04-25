/************************************
 * TO DOS:
 * 1. Popup window for turning GPS on
 * 2. If user doesn't turn GPS on, then quit?
 * 3. If GPS not returning a location (e.g. indoors), then? In this case, it just ignores all this data.
 * 4. So far, only checking if Wifi is *connected*. Should check if any open Wifi is available?
 * 5. If there is movement in 60 seconds, but no movement in 5-second span, we can't detect direction. Currently, it uses the lat/long
 *    from the previous 60 second span (less accurate since more motion in 60 seconds than 5). Is this okay,
 *    or should we just discard that data?
 * 6. What if we go out of range of GPS and then get WiFi (e.g. enter a building and then get WiFi). In this
 * 	  case, we wouldn't get any location updates, and will not be capture the fact that WiFi was found.
 */

package com.iw.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.iw.R;
import com.iw.consts.Consts;
import com.iw.db.LocationDatabaseHelper;
import com.iw.utils.NetworkUtils;
import com.iw.utils.Utils;

public class LocationActivity extends Activity {
    // Dialog IDs
    private final int DIALOG_GPS_OFF = 0; // dialog for when GPS is off
    private final int DIALOG_PROGRESS_SEND = 1; // dialog for progress bar, when

    // sending data

    /**
     * thread to send points to backend
     */
    private class SendPointsThread extends Thread {
        @Override
        public void run() {
            int total_send = LocationDatabaseHelper
                            .getNumRows(LocationActivity.this);
            int sent = 0;
            while (LocationDatabaseHelper.getNumRows(LocationActivity.this) > 0) {
                Map<String, String> data = LocationDatabaseHelper
                                .popFew(LocationActivity.this);
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(
                                Consts.SEND_POINTS_URL_LOCATION);
                try {
                    // Add data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                                    2 * LocationDatabaseHelper.TABLE_POINTS_NUM_COLUMNS);
                    nameValuePairs.add(new BasicNameValuePair("lat", data
                                    .get(LocationDatabaseHelper.KEY_LAT)));
                    nameValuePairs.add(new BasicNameValuePair("lng", data
                                    .get(LocationDatabaseHelper.KEY_LNG)));
                    nameValuePairs.add(new BasicNameValuePair("bearing", data
                                    .get(LocationDatabaseHelper.KEY_BEARING)));
                    nameValuePairs.add(new BasicNameValuePair("timestamp", data
                                    .get(LocationDatabaseHelper.KEY_TIMESTAMP)));
                    nameValuePairs.add(new BasicNameValuePair(
                                    "wifi_power_levels",
                                    data.get(LocationDatabaseHelper.KEY_WIFI_POWER_LEVELS)));
                    nameValuePairs.add(new BasicNameValuePair("speed", data
                                    .get(LocationDatabaseHelper.KEY_SPEED)));
                    nameValuePairs.add(new BasicNameValuePair("accuracy", data
                                    .get(LocationDatabaseHelper.KEY_ACCURACY)));
                    nameValuePairs.add(new BasicNameValuePair(
                                    "sigstrength",
                                    data.get(LocationDatabaseHelper.KEY_SIGNAL_STRENGTH)));

                    nameValuePairs.add(new BasicNameValuePair("user_id", Utils
                                    .getDeviceID(LocationActivity.this)));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // make attempts
                    int attempt = 0;
                    while (attempt < Consts.HTTP_MAX_ATTEMPTS) {
                        HttpResponse response = httpclient.execute(httppost);
                        if (response.getStatusLine().getStatusCode() == 200) {
                            break;
                        } else {
                            attempt = attempt + 1;
                        }
                    }
                    Message msg = progressHandler.obtainMessage();
                    sent += data.get(LocationDatabaseHelper.KEY_LAT).split(",").length;
                    msg.arg1 = (int) (sent * 100.0 / total_send);
                    progressHandler.sendMessage(msg);
                } catch (ClientProtocolException e) {
                    Log.d("Network error", e.toString());
                } catch (IOException e) {
                    Log.d("Network error", e.toString());
                }
            }
        }
    }

    SendPointsThread sendPointsThread;
    ProgressDialog progressDialog;

    final Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int total = msg.arg1;
            progressDialog.setProgress(total);
            if (total >= 100) {
                dismissDialog(DIALOG_PROGRESS_SEND);
            }
        }
    };

    // send data points to back-end
    private void sendPoints() {
        if (LocationDatabaseHelper.getNumRows(LocationActivity.this) <= 0) {
            return;
        }
        SendPointsThread thread = new SendPointsThread();
        showDialog(DIALOG_PROGRESS_SEND);
        thread.start();
    }

    // fetch predictions from back-end
    private void getPredictions() {
        Log.d("DEBUG", "Receiving pred");
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(Consts.PREDICTION_MODEL_URL);
        // make attempts
        int attempt = 0;
        while (attempt < Consts.HTTP_MAX_ATTEMPTS) {
            try {
                HttpResponse response = httpclient.execute(httpget);
                if (response.getStatusLine().getStatusCode() == 200) {
                    InputStream instream = response.getEntity().getContent();
                    BufferedReader br = new BufferedReader(
                                    new InputStreamReader(instream, "UTF-8"));
                    String line;
                    List<Double> lat_list = new ArrayList<Double>();
                    List<Double> lng_list = new ArrayList<Double>();
                    List<Double> bearing_list = new ArrayList<Double>();
                    List<Integer> time_list = new ArrayList<Integer>();
                    while ((line = br.readLine()) != null) {
                        String[] parameters = line.split(" ");
                        lat_list.add(Double.parseDouble(parameters[0]));
                        lng_list.add(Double.parseDouble(parameters[1]));
                        bearing_list.add(Double.parseDouble(parameters[2]));
                        time_list.add((int) Double.parseDouble(parameters[3]));
                    }
                    LocationDatabaseHelper.addPredictions(this, lat_list,
                                    lng_list, bearing_list, time_list);
                    break;
                } else {
                    attempt++;
                }
            } catch (ClientProtocolException e) {
                attempt++;
            } catch (IOException e) {
                attempt++;
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.location);
        findViewById(R.id.send_button).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                if (NetworkUtils.isConnectedWiFi(LocationActivity.this)
                                                || NetworkUtils.isConnectedMobile(LocationActivity.this)) {
                                    sendPoints();
                                } else {
                                    Utils.toast(LocationActivity.this,
                                                    "DroiDTN: Internet Connection is unavailable. Please try again later.");
                                }
                            }
                        });
        findViewById(R.id.get_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (NetworkUtils.isConnectedWiFi(LocationActivity.this)
                                || NetworkUtils.isConnectedMobile(LocationActivity.this)) {
                    getPredictions();
                    Utils.toast(LocationActivity.this,
                                    "Prediction Model Updated");
                } else {
                    Utils.toast(LocationActivity.this,
                                    "DroiDTN: Internet Connection is unavailable. Please try again later.");
                }
            }
        });
        findViewById(R.id.service_button).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                if (MarkovService.isServiceRunning()) {
                                    ((Button) findViewById(R.id.service_button))
                                                    .setText(R.string.service_not_running);
                                    MarkovService.stopService(LocationActivity.this);
                                } else {
                                    ((Button) findViewById(R.id.service_button))
                                                    .setText(R.string.service_running);
                                    MarkovService.startService(LocationActivity.this);
                                }
                            }
                        });

        // if GPS is disabled, ask user to turn it on
        // Runs only once, when activity is created
        // XXX: Alternately could put it in onResume() to constantly remind user
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showDialog(DIALOG_GPS_OFF);
        }
    }

    // handler and runnable to update number of points
    private static Handler updateHandler = new Handler();
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            refreshNumPoints();
        }
    };

    /**
     * Called every time activity gets focus
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (MarkovService.isServiceRunning()) {
            ((Button) findViewById(R.id.service_button))
                            .setText(R.string.service_running);
        } else {
            ((Button) findViewById(R.id.service_button))
                            .setText(R.string.service_not_running);
        }

        refreshNumPoints();
    }

    /**
     * Called whenever activity loses focus Stops the refreshing of number of
     * data points
     */
    @Override
    protected void onPause() {
        super.onPause();
        updateHandler.removeCallbacks(updateRunnable);
        Log.d("Refresh", "Stop refreshing");
    }

    /**
     * refresh the number of points collected sets timer to refresh points again
     * according to REFRESH_RATE
     */
    private void refreshNumPoints() {
        int num_points = LocationDatabaseHelper.getNumRows(this);
        TextView tv = (TextView) findViewById(R.id.num_rows);
        tv.setText(Integer.toString(num_points));
        updateHandler.postDelayed(updateRunnable, Consts.REFRESH_RATE * 1000); // update
        // at
        // twice
        // the
        // rate
        // of
        // points
        // being
        // found
        Log.d("Refresh", "Refreshed number of datapoints");
    }

    /**
     * Dialog to ask user to turn on GPS
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        // if GPS is off
        case DIALOG_GPS_OFF:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(
                            "GPS is turned off. Would you like to turn it on?")
                            .setCancelable(false)
                            .setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                                DialogInterface dialog,
                                                                int id) {
                                                    Intent gpsIntent = new Intent(
                                                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                    gpsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(gpsIntent);
                                                }
                                            })
                            .setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                                DialogInterface dialog,
                                                                int id) {
                                                    dialog.cancel();
                                                }
                                            });
            AlertDialog alert = builder.create();
            return alert;

            // progress bar for sending data
        case DIALOG_PROGRESS_SEND:
            progressDialog = new ProgressDialog(LocationActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(this.getText(R.string.sending));
            progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
                            this.getText(R.string.hide),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                int which) {
                                    dismissDialog(DIALOG_PROGRESS_SEND);
                                }
                            });
            progressDialog.setCancelable(true);
            return progressDialog;
        }
        return null;
    }
}