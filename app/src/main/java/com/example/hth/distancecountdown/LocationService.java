package com.example.hth.distancecountdown;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hth on 9/16/15.
 */
public class LocationService extends Service {

    private static final String TAG = "DCD_LOCATIONSERVICE";

    private LocationManager mLocationManager = null;
    private Timer mTimer = null;
    private TimerTask mTimerTask;
    private final Handler mHandler = new Handler();
    private boolean mTimerRunning = false;
    private long mElapsedTime = 0;
    private int mDistance = 0;
    private int mTargetDistance = 0;

    private LocationListener mLocationListener = new MyLocationListener(LocationManager.GPS_PROVIDER, this);

    private Intent locationIntent = new Intent(Constants.ACTION.BROADCAST);
    private NotificationManager mNotificationManager;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        /**
         * Starts Location updates
         */
        if (intent.getAction().equals(Constants.ACTION.START_LOCATION_UPDATES)) {
            Log.d(TAG, "Start Location Updates");

            if (mLocationManager == null) {
                mLocationManager = (LocationManager) getApplicationContext()
                        .getSystemService(Context.LOCATION_SERVICE);
            }

            mLocationManager.addGpsStatusListener(MyGPSListener);

            requestLocationUpdates(1000);
        }
        /**
         * Stops Location updates
         */
        else if (intent.getAction().equals(Constants.ACTION.STOP_LOCATION_UPDATES)) {
            Log.d(TAG, "Stop Location Updates");
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            mTimerRunning = false;
            stopForeground(true);
            stopLocationManager();
            stopSelf();
        }
        /**
         * Starts timer
         */
        else if (intent.getAction().equals(Constants.ACTION.START_TIMER)) {
            Log.d(TAG, "Start Timer");

            mTargetDistance = intent.getIntExtra(Constants.STATUS.DISTANCE, 0);

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, createNotification());

            if (mTimer == null) {
                mElapsedTime = 0;
                mTimer = new Timer();
                initializeTimerTask();
                mTimer.schedule(mTimerTask, 1000, 1000);
            }

            mTimerRunning = true;
        }
        /**
         * Pauses/resumes timer
         */
        else if (intent.getAction().equals(Constants.ACTION.PAUSE_TIMER)) {
            Log.d(TAG, "Pause Timer " + mTimerRunning);
            mTimerRunning = !mTimerRunning;
            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    createNotification());
        }
        /**
         * Stops timer
         */
        else if (intent.getAction().equals(Constants.ACTION.PAUSE_TIMER)) {
            Log.d(TAG, "Stop Timer");
            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    createNotification());
            mTimerRunning = false;
        }
        /**
         * Reset
         */
        else if (intent.getAction().equals(Constants.ACTION.RESET)) {
            Log.d(TAG, "Reset");
            mTimerRunning = false;
            mDistance = 0;
            mElapsedTime = 0;
            stopForeground(true);
        }

        return START_STICKY;
    }

    /**
     * Set location updates to specified interval.
     * @param time in milliseconds
     */
    private void requestLocationUpdates(int time) {

        mLocationManager.removeUpdates(mLocationListener);

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, time, 2, mLocationListener);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "Failed to request location update. ", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "GPS provider doesn't exist. " + ex.getMessage());
        }
    }

    private void initializeTimerTask() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mTimerRunning) {
                            mElapsedTime++;
                            Intent intent = new Intent(Constants.ACTION.BROADCAST);
                            intent.putExtra(Constants.STATUS.ELAPSED_TIME_CHANGED, mElapsedTime);
                            sendBroadcast(intent);
                            mNotificationManager.notify(
                                    Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                                    createNotification());
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");

        stopLocationManager();
        stopSelf();
    }

    private void stopLocationManager() {
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                Log.d(TAG, "Failed to remove location listener. ", ex);
            }
            mLocationManager = null;
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        long secs = mElapsedTime;
        long hours = secs / 3600;
        long minutes = (secs % 3600) / 60;
        long seconds = secs % 60;

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("DistanceCountdown")
                .setContentText((mTimerRunning ? "" : "(" + getString(R.string.paused) + ") ")
                        + String.format("%02d:%02d:%02d, ", hours, minutes, seconds)
                        + ((mTargetDistance - mDistance <= 0) ? 0 : (mTargetDistance - mDistance))
                        + "/" + mTargetDistance + " m")
                .setContentIntent(pendingIntent).build();

        return notification;
    }

    /**
     * LocationListener
     */
    private class MyLocationListener implements LocationListener {

        private Location mLastLocation;
        Context mContext;

        public MyLocationListener(String provider, Context context) {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
            mContext = context;
        }

        /**
         * Handle location updates
         *
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged(" + location + ")");

            if (mTimerRunning) {
                int distance = mDistance + (int) location.distanceTo(mLastLocation);
                //TODO: check distance accuracy

                // Send intent for location change.
                if(distance != mDistance) {
                    mDistance = distance;
                    Intent intent = locationIntent.putExtra(
                            Constants.STATUS.LOCATION_CHANGED, mDistance);
                    mContext.sendBroadcast(intent);
                    mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                            createNotification());
                }
            }

            mLastLocation = location;
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled(" + provider + ")");
            Intent intent = locationIntent.putExtra(Constants.STATUS.GPS_NOT_OK, mDistance);
            mContext.sendBroadcast(intent);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled(" + provider + ")");
            Intent intent = locationIntent.putExtra(Constants.STATUS.GPS_OK, mDistance);
            mContext.sendBroadcast(intent);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged(" + provider + ", " + status + ")");
            if (status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                Intent intent = locationIntent.putExtra(Constants.STATUS.GPS_NOT_OK, mDistance);
                mContext.sendBroadcast(intent);
            } else {
                Intent intent = locationIntent.putExtra(Constants.STATUS.GPS_OK, mDistance);
                mContext.sendBroadcast(intent);
            }
        }
    }

    /**
     * Listener for GPS Status
     */
    private final GpsStatus.Listener MyGPSListener = new GpsStatus.Listener() {

        int lastCount = 0;

        public void onGpsStatusChanged(int event) {

            Intent intent;

            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.d("MyGPSListener", "gps event started");
                    break;

                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.d("MyGPSListener", "gps event first fix");
                    intent = locationIntent.putExtra(Constants.STATUS.GPS_OK, -1);
                    sendBroadcast(intent);
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.d("MyGPSListener", "gps event stopped");
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    //Log.d("MyGPSListener", "gps event satellite status");
                    int count = 0;
                    GpsStatus status = mLocationManager.getGpsStatus(null);
                    for (GpsSatellite sat : status.getSatellites()) {
                        if (sat.usedInFix()) {
                            count++;
                        }
                    }
                    if(count != lastCount) {
                        intent = locationIntent.putExtra(Constants.STATUS.GPS_OK, count);
                        sendBroadcast(intent);
                        lastCount = count;
                    }
                    break;
            }
        }
    };
}
