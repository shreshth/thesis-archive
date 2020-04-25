package com.iw.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.iw.R;
import com.iw.consts.Consts;
import com.iw.db.AppDatabaseHelper;
import com.iw.utils.NetworkUtils;
import com.iw.utils.Utils;

public class AppActivity extends Activity {
    private final int DIALOG_PROGRESS_SEND = 1; // dialog for progress bar, when

    /**
     * thread to send points to backend
     */
    private class SendPointsThread extends Thread {
        @Override
        public void run() {
            int total_send = AppDatabaseHelper.getNumRows(AppActivity.this);
            int sent = 0;
            while (AppDatabaseHelper.getNumRows(AppActivity.this) > 0) {
                Map<String, String> data = AppDatabaseHelper
                                .popFew(AppActivity.this);

                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(Consts.SEND_POINTS_URL_APP);
                try {
                    // Add data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                                    2 * AppDatabaseHelper.NUM_COLUMNS);

                    nameValuePairs.add(new BasicNameValuePair("appName", data
                                    .get(AppDatabaseHelper.KEY_APP_NAME)));
                    nameValuePairs.add(new BasicNameValuePair("uid", data
                                    .get(AppDatabaseHelper.KEY_APP_UID)));
                    nameValuePairs.add(new BasicNameValuePair(
                                    "tcpRxBytes",
                                    data.get(AppDatabaseHelper.KEY_TCP_RX_BYTES)));
                    nameValuePairs.add(new BasicNameValuePair(
                                    "tcpTxBytes",
                                    data.get(AppDatabaseHelper.KEY_TCP_TX_BYTES)));
                    nameValuePairs.add(new BasicNameValuePair(
                                    "udpRxBytes",
                                    data.get(AppDatabaseHelper.KEY_UDP_RX_BYTES)));
                    nameValuePairs.add(new BasicNameValuePair(
                                    "udpTxBytes",
                                    data.get(AppDatabaseHelper.KEY_UDP_TX_BYTES)));
                    nameValuePairs.add(new BasicNameValuePair("timestamp", data
                                    .get(AppDatabaseHelper.KEY_TIMESTAMP)));

                    nameValuePairs.add(new BasicNameValuePair("user_id", Utils
                                    .getDeviceID(AppActivity.this)));
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
                    sent += data.get(AppDatabaseHelper.KEY_APP_UID).split(",").length;
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
        if (AppDatabaseHelper.getNumRows(AppActivity.this) <= 0) {
            return;
        }
        SendPointsThread thread = new SendPointsThread();
        showDialog(DIALOG_PROGRESS_SEND);
        thread.start();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.apps);

        findViewById(R.id.networkstats_send_button).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                if (NetworkUtils.isConnectedWiFi(AppActivity.this)
                                                || NetworkUtils.isConnectedMobile(AppActivity.this)) {
                                    sendPoints();
                                } else {
                                    Utils.toast(AppActivity.this,
                                                    "DroiDTN: Internet Connection is unavailable. Please try again later.");
                                }
                            }
                        });

        findViewById(R.id.networkstats_service_button).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                if (NetworkStatsService.isServiceRunning()) {
                                    ((Button) findViewById(R.id.networkstats_service_button))
                                                    .setText(R.string.service_not_running);
                                    NetworkStatsService
                                                    .stopService(AppActivity.this);
                                } else {
                                    ((Button) findViewById(R.id.networkstats_service_button))
                                                    .setText(R.string.service_running);
                                    NetworkStatsService
                                                    .startService(AppActivity.this);
                                }
                            }
                        });
    }

    /**
     * Dialog to show progress in sending points
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        // progress bar for sending data
        case DIALOG_PROGRESS_SEND:
            progressDialog = new ProgressDialog(AppActivity.this);
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

    // handler and runnable to update number of points
    private static Handler updateHandler = new Handler();
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            refreshNumPoints();
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if (NetworkStatsService.isServiceRunning()) {
            ((Button) findViewById(R.id.networkstats_service_button))
                            .setText(R.string.service_running);
        } else {
            ((Button) findViewById(R.id.networkstats_service_button))
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
        int num_points = AppDatabaseHelper.getNumRows(this);
        TextView tv = (TextView) findViewById(R.id.networkstats_num_rows);
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

}
