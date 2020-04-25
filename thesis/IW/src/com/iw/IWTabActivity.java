package com.iw;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.iw.app.AppActivity;
import com.iw.location.LocationActivity;

/**
 * 
 * Parent activity of the three tabs
 * 
 */
@SuppressWarnings("deprecation")
public class IWTabActivity extends TabActivity implements OnGestureListener {
    public static IWTabActivity mIWTabActivity;
    public static boolean mIsRunning = false;

    private static final String TAB_ID_APP = "map";
    private static final String TAB_ID_LOCATION = "location";
    public static final int TAB_APP = 0;
    public static final int TAB_LOCATION = 1;
    private static final int NUM_TABS = 2;

    // swipey stuff
    private GestureDetector gestureScanner;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int ANIMATION_DURATION = 200;
    // maximum slope for swipe gesture
    private static final double MAX_SLOPE = Math.tan(Math.toRadians(30));

    public static int mCurrentChild;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gestureScanner = new GestureDetector(this);
        mIWTabActivity = this;
        setContentView(R.layout.main);

        Temp.PredictWrapper(this);

        // Set up Tabs
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;

        // ARActivity
        intent = new Intent().setClass(this, AppActivity.class);
        spec = tabHost.newTabSpec(TAB_ID_APP)
                        .setIndicator(getString(R.string.tab_app))
                        .setContent(intent);
        tabHost.addTab(spec);

        // MapActivity
        intent = new Intent().setClass(this, LocationActivity.class);
        spec = tabHost.newTabSpec(TAB_ID_LOCATION)
                        .setIndicator(getString(R.string.tab_location))
                        .setContent(intent);
        tabHost.addTab(spec);

        // Initial tab selection
        mCurrentChild = TAB_APP;
        tabHost.setCurrentTab(TAB_APP);
        tabHost.setOnTabChangedListener(tabChangeListener);

        // style tabs
        Typeface tf = Typeface.createFromAsset(getAssets(),
                        "fonts/Roboto-Bold.ttf");
        for (int i = 0; i < getTabWidget().getChildCount(); i++) {
            View tab = getTabWidget().getChildAt(i);
            tab.getLayoutParams().height = (int) (50f * getResources()
                            .getDisplayMetrics().density);
            TextView tv = (TextView) tab.findViewById(android.R.id.title);
            tv.setTypeface(tf);
            tv.setGravity(Gravity.CENTER);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            tabHost.getTabWidget().setDividerDrawable(R.color.gray_divider);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentChild = TAB_APP;
        TabHost tabHost = getTabHost();
        tabHost.setCurrentTab(TAB_APP);
    }

    /*
     * Swipey stuff
     */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureScanner != null) {
            if (gestureScanner.onTouchEvent(ev)) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gestureScanner.onTouchEvent(me);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                    float velocityY) {
        // Check movement along the Y-axis. If too much, then ignore
        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH
                        || Math.abs(e1.getY() - e2.getY()) > MAX_SLOPE
                                        * Math.abs(e1.getX() - e2.getX())) {
            return false;
        }

        // Swipe from right to left (if certain distance and velocity)
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            if (mCurrentChild != NUM_TABS - 1) {
                getTabHost().setCurrentTab(mCurrentChild + 1);
                getTabHost().getTabContentView().setAnimation(
                                inFromRightAnimation());
            }
            return true;
        }

        // Swipe from left to right (if certain distance and velocity)
        if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            if (mCurrentChild != 0) {
                getTabHost().setCurrentTab(mCurrentChild - 1);
                getTabHost().getTabContentView().setAnimation(
                                inFromLeftAnimation());
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                    float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @SuppressWarnings("unused")
    private static Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, +1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    @SuppressWarnings("unused")
    private static Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, -1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(ANIMATION_DURATION);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    @SuppressWarnings("unused")
    private static Animation inFromLeftAnimation() {
        Animation inFromRight = new TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, -1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    @SuppressWarnings("unused")
    private static Animation outToRightAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, +1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(ANIMATION_DURATION);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    public OnTabChangeListener tabChangeListener = new OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabID) {
            // update current view
            if (tabID == TAB_ID_APP) {
                mCurrentChild = TAB_APP;
            } else if (tabID == TAB_ID_LOCATION) {
                mCurrentChild = TAB_LOCATION;
            }
        }
    };
}