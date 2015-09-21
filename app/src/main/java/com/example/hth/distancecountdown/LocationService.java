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
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by hth on 9/16/15.
 */
public class LocationService extends Service {

    private static final String TAG = "DCD_LOCATIONSERVICE";

    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 1f;

    private LocationManager mLocationManager = null;
    private boolean mLocating = false;

    LocationListener mLocationListener = new MyLocationListener(LocationManager.GPS_PROVIDER);

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand()");

        if (intent.getAction().equals(Constants.ACTION.START_LOCATION_MANAGER)) {
            Log.i(TAG, "Received Start Location Manager Intent");

            if (mLocationManager == null) {
                mLocationManager = (LocationManager) getApplicationContext()
                        .getSystemService(Context.LOCATION_SERVICE);
            }

            try {
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListener);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "Failed to request location update. ", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "GPS provider doesn't exist. " + ex.getMessage());
            }
        }
        else if (intent.getAction().equals(Constants.ACTION.STOP_LOCATION_MANAGER)) {
            Log.i(TAG, "Received Stop Location Manager Intent");
            stopLocationManager();
        }
        else if (intent.getAction().equals(Constants.ACTION.START_LOCATION_UPDATES)) {
            Log.i(TAG, "Received Start Location Updates Intent");

            Intent notificationIntent = new Intent(this, MainActivity.class);
            //notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
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
        }
        else if (intent.getAction().equals(Constants.ACTION.STOP_LOCATION_UPDATES)) {
            Log.i(TAG, "Received Stop Location Updates Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate()");
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy()");

        stopLocationManager();
    }

    private void stopLocationManager() {
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                Log.i(TAG, "Failed to remove location listener. ", ex);
            }
            mLocationManager = null;
        }
    }

    /**
     * LocationListener
     */
    private class MyLocationListener implements LocationListener {

        Location mLastLocation;

        public MyLocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        /**
         * Handle location updates
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged(" + location + ")");
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled(" + provider + ")");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled(" + provider + ")");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged(" + provider + ", " + status + ")");
        }
    }

}
