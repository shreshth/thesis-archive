package com.iw.consts;

/*
 * TODO:
 * a = Log everytime a 3G packet not able to be delayed (i.e. not tolerant enough to be delayed)
 * b = Log everytime a 3G packet was successfully delayed
 * b/(a+b) is the savings ratio
 */

public class Consts {

    // ------------------------ Location - Data collection
    // -------------------------------
    // //

    // time granularity for location updates (in seconds)

    public static final int TIME_GRANULARITY = 10;

    // maximum wait for a gps location / wifi scan to return (in seconds)
    public static final int MAX_WAIT = 8;

    // total length of markov model in seconds
    public static final int MARKOV_TOTAL_SECONDS = 10 * 60;

    // how many steps of location data to store
    public static final int NUM_MARKOV_STEPS = MARKOV_TOTAL_SECONDS
                    / TIME_GRANULARITY;

    // supported wireless SSID's
    public static final String[] SSID_WHITELIST = { "puwireless" };

    // refresh rate for number of datapoints on main activity (in seconds)
    public static final int REFRESH_RATE = 10;

    // Minimum accuracy for location prediction
    public static final double MIN_LOCATION_ACCURACY = 25.0;

    // ------------------------ Location - prediction
    // ---------------------------
    // //

    // We will scan for wifi this often (seconds)
    public static final int WIFI_SCAN_FREQUENCY = 10;

    // Bearing will be reduced in cluster prediction by this much
    public static final double BEARING_MULTIPLIER = 1 / 18000;

    // Minimum good wifi RSSI to be considered connected
    public static final int MIN_WIFI_RSSI = -70;

    // ----------------------- Apps - data collection
    // -----------------------------
    // //

    // time between reading the files for different apps (in milliseconds)
    public static final int TIME_GRANULARITY_MILLIS = 500;

    // time to wait for results (in milliseconds)
    public static final int MAX_WAIT_MILLIS = 300;

    // ------------------------ Apps - delay tolerance prediction
    // ---------------------------------
    // //

    // Minimum number of rounds below which clamping doesn't occur
    public static final int PREDICTION_CLAMP_MIN_ROUNDS = 5;

    // application prediction weights
    public static final double DELAY_TOLERANCE_FACTOR = 0.5;

    // minimum delay tolerance granularity (milliseconds)
    public static final double MIN_TIME_DIFF_DELAY_TOLERANCE = 2.7 * 1000;
    public static final double DELAY_TOLERANCE_TIME_MIN_FACTOR = 0.25;
    public static final boolean DELAY_TOLERANCE_TIME_CUTOFF = true;

    // maximum allowed change in delay tolerance
    public static final double MAX_CHANGE_FACTOR = 0.1;
    public static final boolean DELAY_TOLERANCE_CHANGE_CUTOFF = true;
    public static final double MAX_INIT_CHANGE = 10;

    // clamp to this maximum value (seconds)
    public static final double MAX_DELAY_TOLERANCE_VALUE = 600.0;

    // ------------------------ HTTP Constants ---------------------------------
    // //

    // Number of data points to send in one http request
    public static final int HTTP_BATCH_LIMIT = 10;

    // URL for sending data to backend
    public static final String SEND_POINTS_URL_APP = "http://droidtndata.appspot.com/add_data_app";
    public static final String SEND_POINTS_URL_LOCATION = "http://droidtndata.appspot.com/add_data_location";

    // Prediction Model
    public static final String PREDICTION_MODEL_URL = "http://droidtndata.appspot.com/pred.txt";

    // number of tries to make for an http request before giving up
    public static final int HTTP_MAX_ATTEMPTS = 3;

    // ------------------------ Test Constants ---------------------------------
    // //

    // testing messages etc will only appear on these phones
    public static final String[] TEST_DEVICE_WHITELIST = { "e8bce1e69b89be6f", // Hamza's
                                                                               // personal
                                                                               // Phone
                    "892ff7ea98149a55", // Galaxy Nexus we got for the project
                    "72890e4ed7e94cae", // Shreshth's personal phone
                    "43f1e0dcff1f1cb5" // IW Galaxy Nexus
    };

    private Consts() {
    }
}
