package dev.datvt.cloudtracks;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by datvt on 8/10/2016.
 */
public class MyApplication extends Application {
    public static final boolean CAN_DOWNLOAD = false;

    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(getResources().getString(R.string.analytics_tracker));
        }
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
