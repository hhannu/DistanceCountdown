package com.example.hth.distancecountdown;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
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

    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 1f;

    private LocationManager mLocationManager = null;
    private boolean mLocating = false;
    private Timer mTimer = null;
    private TimerTask mTimerTask;
    private final Handler mHandler = new Handler();
    private boolean mTimerRunning = false;
    private long mElapsedTime = 0;

    LocationListener mLocationListener = new MyLocationListener(LocationManager.GPS_PROVIDER, this);

    Intent locationIntent = new Intent(Constants.ACTION.BROADCAST);

    @Override
    public IBinder onBind(Intent arg0)
    {
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

            try {
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListener);
            } catch (java.lang.SecurityException ex) {
                Log.d(TAG, "Failed to request location update. ", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "GPS provider doesn't exist. " + ex.getMessage());
            }

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("DistanceCountdown")
                .setContentText("")
                .setContentIntent(pendingIntent).build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

            if (mTimer == null) {
                mElapsedTime = 0;
                mTimer = new Timer();
                initializeTimerTask();
                mTimer.schedule(mTimerTask, 1000, 1000);
            }
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
            mTimerRunning = true;
        }
        /**
         * Stops timer
         */
        else if (intent.getAction().equals(Constants.ACTION.STOP_TIMER)) {
            Log.d(TAG, "Stop Timer");
            mTimerRunning = false;
        }
        return START_STICKY;
    }

    private void initializeTimerTask() {
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mTimerRunning) {
                            mElapsedTime++;
                            Intent intent = new Intent(Constants.ACTION.BROADCAST);
                            intent.putExtra(Constants.STATUS.ELAPSED_TIME_CHANGED, mElapsedTime);
                            sendBroadcast(intent);
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
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

    /**
     * LocationListener
     */
    private class MyLocationListener implements LocationListener {

        private Location mLastLocation;
        Context context;

        public MyLocationListener(String provider, Context context) {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
            this.context = context;
        }

        /**
         * Handle location updates
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged(" + location + ")");
            mLastLocation.set(location);

            Intent intent = locationIntent.putExtra(Constants.STATUS.LOCATION_CHANGED, 0);

            context.sendBroadcast(intent);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled(" + provider + ")");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled(" + provider + ")");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged(" + provider + ", " + status + ")");
        }
    }
}
