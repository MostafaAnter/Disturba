package com.locname.distribution;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;

/**
 * Created by Mostafa on 9/6/2015.
 */
public class TrackerSettings {
    /**
     * Basic tracker settings, with all the default parameters
     * <ul>
     * <li>5min between updates</li>
     * <li>100m between updates</li>
     * <li>100m between updates</li>
     * <li>1m timeout</li>
     * <li>Uses Network</li>
     * <li>Uses Passive</li>
     * </ul>
     */
    public static final TrackerSettings DEFAULT = new TrackerSettings();
    /**
     * The default time interval between location updates
     * Its value is 5 minutes
     */
    public static final long DEFAULT_MIN_TIME_BETWEEN_UPDATES = 5 * 60 * 1000;
    /**
     * The default distance between location updates
     * Its value is 100m
     */
    public static final float DEFAULT_MIN_METERS_BETWEEN_UPDATES = 100;
    /**
     * The default value of timeout that helps to stop the listener if the listener is taking too much time
     * Its value is 1 minutes
     */
    public static final int DEFAULT_TIMEOUT = 60 * 1000;

    /**
     * The minimum time interval between location updates, in milliseconds by default its value is {@link #DEFAULT_MIN_TIME_BETWEEN_UPDATES}
     */
    private long mTimeBetweenUpdates = -1;
    /**
     * The minimum distance between location updates in meters, by default its value is {@link #DEFAULT_MIN_METERS_BETWEEN_UPDATES}
     */
    private float mMetersBetweenUpdates = -1;
    /**
     * The value of mTimeout to stop the listener after a specified time in case the listener is unable to get the location for a specified time
     */
    private int mTimeout = -1;

    /**
     * Specifies if tracker should use the GPS (default is true)
     */
    private boolean mUseGPS = true;
    /**
     * Specifies if tracker should use the Network (default is true)
     */
    private boolean mUseNetwork = true;
    /**
     * Specifies if tracker should use the Passive provider (default is true)
     */
    private boolean mUsePassive = true;

    /**
     * Set the delay between updates of the location
     *
     * @param timeBetweenUpdates the delay between the updates
     * @return the instance of TrackerSettings
     */
    public TrackerSettings setTimeBetweenUpdates(@FloatRange(from = 1) long timeBetweenUpdates) {
        if (timeBetweenUpdates > 0) {
            mTimeBetweenUpdates = timeBetweenUpdates;
        }
        return this;
    }

    public long getTimeBetweenUpdates() {
        return mTimeBetweenUpdates <= 0 ? DEFAULT_MIN_TIME_BETWEEN_UPDATES : mTimeBetweenUpdates;
    }

    /**
     * Set the distance between updates of the location
     *
     * @param metersBetweenUpdates the distance between the updates
     * @return the instance of TrackerSettings
     */
    public TrackerSettings setMetersBetweenUpdates(@FloatRange(from = 1) float metersBetweenUpdates) {
        if (metersBetweenUpdates > 0) {
            mMetersBetweenUpdates = metersBetweenUpdates;
        }
        return this;
    }

    public float getMetersBetweenUpdates() {
        return mMetersBetweenUpdates <= 0 ? DEFAULT_MIN_METERS_BETWEEN_UPDATES : mMetersBetweenUpdates;
    }

    /**
     * Set the timeout before giving up if no updates
     *
     * @param timeout the timeout before giving up
     * @return the instance of TrackerSettings
     */
    public TrackerSettings setTimeout(@IntRange(from = 1) int timeout) {
        if (timeout > 0) {
            mTimeout = timeout;
        }
        return this;
    }

    public int getTimeout() {
        return this.mTimeout <= -1 ? DEFAULT_TIMEOUT : mTimeout;
    }

    /**
     * Set the usage of the GPS for the tracking
     *
     * @param useGPS true if the tracker should use the GPS, false if not
     * @return the instance of TrackerSettings
     */
    public TrackerSettings setUseGPS(boolean useGPS) {
        mUseGPS = useGPS;
        return this;
    }

    public boolean shouldUseGPS() {
        return mUseGPS;
    }

    /**
     * Set the usage of network for the tracking
     *
     * @param useNetwork true if the tracker should use the network, false if not
     * @return the instance of TrackerSettings
     */
    public TrackerSettings setUseNetwork(boolean useNetwork) {
        mUseNetwork = useNetwork;
        return this;
    }

    public boolean shouldUseNetwork() {
        return mUseNetwork;
    }

    /**
     * Set the usage of the passive sensor for the tracking
     *
     * @param usePassive true if the tracker should listen to passive updates, false if not
     * @return the instance of TrackerSettings
     */
    public TrackerSettings setUsePassive(boolean usePassive) {
        mUsePassive = usePassive;
        return this;
    }

    public boolean shouldUsePassive() {
        return mUsePassive;
    }
}