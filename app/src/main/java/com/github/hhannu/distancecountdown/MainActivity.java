package com.github.hhannu.distancecountdown;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;


import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    protected static final String TAG = "DistanceCountdown";

    // Keys for storing activity state in the Bundle.
    protected final static String COUNTDOWN_RUNNING_KEY = "countdown-running-key";
    protected final static String RESUME_COUNTDOWN_KEY = "resume-countdown-key";
    protected final static String CLEAR_TIMER_KEY = "clear-timer-key";
    protected final static String START_TIME_STRING_KEY = "start-time-string-key";
    protected final static String REMAINING_DISTANCE_KEY = "remaining-distance-key";
    protected final static String ELAPSED_TIME_KEY = "elapsed-time-key";
    protected final static String AVEGARE_SPEED_KEY = "average-speed-key";

    // UI Widgets.
    private Button mStartButton;
    private Button mResetButton;
    private Chronometer mChronometer;
    private TextView mSpeedView;
    private TextView mGpsStatus;
    private EditText mDistanceView;

    private Boolean mCountdownRunning;
    private Boolean mResumeCountdown;
    private String mStartTime;

    private int mDistance;
    private long mElapsedTime;
    private float mAverageSpeed;
    private boolean mClearTimer;

    private Intent mServiceIntent;

    private SharedPreferences mPrefs;
    private MyBroadcastReceiver mMyBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(" + (savedInstanceState != null) + ")");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Locate the UI widgets.
        mStartButton = (Button) findViewById(R.id.startButton);
        mResetButton = (Button) findViewById(R.id.resetButton);
        mSpeedView = (TextView) findViewById(R.id.speedView);
        mGpsStatus = (TextView) findViewById(R.id.gpsStatus);
        mGpsStatus.setText(R.string.gps_not_ok);
        mDistanceView = (EditText) findViewById(R.id.distanceView);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setText("00:00:00");
        mElapsedTime = 0;
        mCountdownRunning = false;
        mResumeCountdown = false;
        mPrefs = getSharedPreferences(this.getLocalClassName(), 0);

        // Update values using data stored in the Bundle,
        if (savedInstanceState != null) {
            updateValuesFromBundle(savedInstanceState);
        }
        // or update values using data stored in SharedPreferences file.
        else {
            updateValuesFromSharedPreferences();
        }

        mServiceIntent = new Intent(this, LocationService.class);
        mServiceIntent.setAction(Constants.ACTION.START_LOCATION_UPDATES);
        startService(mServiceIntent);

        mMyBroadcastReceiver = new MyBroadcastReceiver();
        this.registerReceiver(mMyBroadcastReceiver, new IntentFilter(Constants.ACTION.BROADCAST));
    }

    /**
     * Handles the Start button
     */
    public void startButtonHandler(View view) {
        // Do nothing is distance == 0
        if (mDistanceView.getText().toString().equals("0")) {
            return;
        }
        // Start/resume countdown
        if (!mCountdownRunning) {
            // Resume
            if (mResumeCountdown) {
                mServiceIntent.setAction(Constants.ACTION.PAUSE_TIMER);
            }
            // Start
            else {
                mDistance = Integer.parseInt(mDistanceView.getText().toString());
                mStartTime = DateFormat.getTimeInstance().format(new Date());
                mServiceIntent.setAction(Constants.ACTION.START_TIMER);
                mServiceIntent.putExtra(Constants.STATUS.DISTANCE, mDistance);
            }

            mCountdownRunning = true;
            mDistanceView.setEnabled(false);
            mStartButton.setText(R.string.stop);
            mResetButton.setEnabled(false);

            startService(mServiceIntent);
        } else {
            // Pause countdown
            mCountdownRunning = false;
            mStartButton.setText(R.string.resume);
            mResumeCountdown = true;

            mServiceIntent.setAction(Constants.ACTION.PAUSE_TIMER);
            startService(mServiceIntent);

            mResetButton.setEnabled(true);
        }
    }

    /**
     * Handles the Reset button
     */
    public void resetButtonHandler(View view) {
        mServiceIntent.setAction(Constants.ACTION.RESET);
        startService(mServiceIntent);

        mResetButton.setEnabled(false);
        mDistanceView.setEnabled(true);
        mDistanceView.setText(String.valueOf(mDistance));
        mStartButton.setText(R.string.start);
        mStartButton.setEnabled(true);
        mChronometer.setText("00:00:00");
        mSpeedView.setText("0.0 km/h");
        mResumeCountdown = false;
        mClearTimer = true;
        mElapsedTime = 0;
    }

    private void setButtons() {
        Log.i(TAG, "setButtons(" + mCountdownRunning + ")");
        if (mCountdownRunning) {
            mStartButton.setText(R.string.stop);
            mResetButton.setEnabled(false);
            mDistanceView.setEnabled(false);
        } else {
            if (mResumeCountdown) {
                mStartButton.setText(R.string.resume);
                mDistanceView.setEnabled(false);
                mResetButton.setEnabled(true);
            } else {
                mStartButton.setText(R.string.start);
                mDistanceView.setEnabled(true);
            }
            mStartButton.setEnabled(true);
        }
    }

    /**
     * Updates the distance, average speed and elapsed time in the UI.
     */
    private void updateUI() {
        //Log.d(TAG, "updateUI()");
        int distance = mDistance - Integer.parseInt(mDistanceView.getText().toString());

        if (mElapsedTime > 0 && distance > 0) {
            double speed = distance / mElapsedTime;
            mSpeedView.setText(Math.round(36d * speed) / 10d + " km/h");
        }

        long secs = mElapsedTime;
        long hours = secs / 3600;
        long minutes = (secs % 3600) / 60;
        long seconds = secs % 60;
        mChronometer.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");

        // Update the values from the Bundle and make sure that
        // buttons are correctly enabled or disabled.
        if (savedInstanceState.keySet().contains(RESUME_COUNTDOWN_KEY)) {
            mResumeCountdown = savedInstanceState.getBoolean(RESUME_COUNTDOWN_KEY, false);
            //Log.i(TAG, "mResumeCountdown == " + mResumeCountdown);
        }

        if (savedInstanceState.keySet().contains(COUNTDOWN_RUNNING_KEY)) {
            mCountdownRunning = savedInstanceState.getBoolean(COUNTDOWN_RUNNING_KEY, false);
            //Log.i(TAG, "mCountdownRunning == " + mCountdownRunning);
            setButtons();
        }

        if (savedInstanceState.keySet().contains(CLEAR_TIMER_KEY)) {
            mClearTimer = savedInstanceState.getBoolean(CLEAR_TIMER_KEY, false);
            //Log.i(TAG, "mClearTimer == " + mClearTimer);
        }

        if (savedInstanceState.keySet().contains(START_TIME_STRING_KEY)) {
            mStartTime = savedInstanceState.getString(START_TIME_STRING_KEY, null);
            //Log.i(TAG, "mStartTime == " + mStartTime);
        }

        if (savedInstanceState.keySet().contains(REMAINING_DISTANCE_KEY)) {
            mDistance = savedInstanceState.getInt(REMAINING_DISTANCE_KEY, 1000);
            mDistanceView.setText(String.valueOf(mDistance));
            //Log.i(TAG, "mDistance == " + mDistance);
        }

        if (savedInstanceState.keySet().contains(ELAPSED_TIME_KEY)) {
            mElapsedTime = savedInstanceState.getLong(ELAPSED_TIME_KEY, 0);
            //Log.i(TAG, "mElapsedTime == " + mElapsedTime);
        }

        if (savedInstanceState.keySet().contains(AVEGARE_SPEED_KEY)) {
            mAverageSpeed = savedInstanceState.getFloat(AVEGARE_SPEED_KEY, 0);
            //Log.i(TAG, "mAverageSpeed == " + mAverageSpeed);
        }

        updateUI();
    }

    private void updateValuesFromSharedPreferences() {
        Log.i(TAG, "updateValuesFromSharedPreferences()");

        if (mPrefs.contains(RESUME_COUNTDOWN_KEY)) {
            mResumeCountdown = mPrefs.getBoolean(RESUME_COUNTDOWN_KEY, false);
            //Log.i(TAG, "mResumeCountdown == " + mResumeCountdown);
        }

        if (mPrefs.contains(COUNTDOWN_RUNNING_KEY)) {
            mCountdownRunning = mPrefs.getBoolean(COUNTDOWN_RUNNING_KEY, false);
            //Log.i(TAG, "mCountdownRunning == " + mCountdownRunning);
            setButtons();
        }

        if (mPrefs.contains(CLEAR_TIMER_KEY)) {
            mClearTimer = mPrefs.getBoolean(CLEAR_TIMER_KEY, false);
            //Log.i(TAG, "mClearTimer == " + mClearTimer);
        }

        if (mPrefs.contains(START_TIME_STRING_KEY)) {
            mStartTime = mPrefs.getString(START_TIME_STRING_KEY, null);
            //Log.i(TAG, "mStartTime == " + mStartTime);
        }

        if (mPrefs.contains(REMAINING_DISTANCE_KEY)) {
            mDistance = mPrefs.getInt(REMAINING_DISTANCE_KEY, 1000);
            mDistanceView.setText(String.valueOf(mDistance));
            //Log.i(TAG, "mDistance == " + mDistance);
        }

        if (mPrefs.contains(ELAPSED_TIME_KEY)) {
            mElapsedTime = mPrefs.getLong(ELAPSED_TIME_KEY, 0);
            //Log.i(TAG, "mElapsedTime == " + mElapsedTime);
        }

        if (mPrefs.contains(AVEGARE_SPEED_KEY)) {
            mAverageSpeed = mPrefs.getFloat(AVEGARE_SPEED_KEY, 0);
            //Log.i(TAG, "mAverageSpeed == " + mAverageSpeed);
        }

        updateUI();
    }

    /**
     * Stores activity data in the Bundle.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onSaveInstanceState");

        savedInstanceState.putBoolean(COUNTDOWN_RUNNING_KEY, mCountdownRunning);
        //Log.i(TAG, "mCountdownRunning == " + mCountdownRunning);

        savedInstanceState.putBoolean(RESUME_COUNTDOWN_KEY, mResumeCountdown);
        //Log.i(TAG, "mResumeCountdown == " + mResumeCountdown);

        savedInstanceState.putBoolean(CLEAR_TIMER_KEY, mClearTimer);
        //Log.i(TAG, "mClearTimer == " + mClearTimer);

        savedInstanceState.putString(START_TIME_STRING_KEY, mStartTime);
        //Log.i(TAG, "mStartTime == " + mStartTime);

        savedInstanceState.putInt(REMAINING_DISTANCE_KEY, mDistance);
        //Log.i(TAG, "mDistance == " + mDistance);

        savedInstanceState.putLong(ELAPSED_TIME_KEY, mElapsedTime);
        //Log.i(TAG, "mElapsedTime == " + mElapsedTime);

        savedInstanceState.putFloat(AVEGARE_SPEED_KEY, mAverageSpeed);
        //Log.i(TAG, "mAverageSpeed == " + mAverageSpeed);

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();

        SharedPreferences.Editor ed = mPrefs.edit();

        ed.putBoolean(COUNTDOWN_RUNNING_KEY, mCountdownRunning);
        //Log.i(TAG, "mCountdownRunning == " + mCountdownRunning);

        ed.putBoolean(RESUME_COUNTDOWN_KEY, mResumeCountdown);
        //Log.i(TAG, "mResumeCountdown == " + mResumeCountdown);

        ed.putBoolean(CLEAR_TIMER_KEY, mClearTimer);
        //Log.i(TAG, "mClearTimer == " + mClearTimer);

        ed.putString(START_TIME_STRING_KEY, mStartTime);
        //Log.i(TAG, "mStartTime == " + mStartTime);

        ed.putLong(ELAPSED_TIME_KEY, mElapsedTime);
        //Log.i(TAG, "mElapsedTime == " + mElapsedTime);

        ed.putInt(REMAINING_DISTANCE_KEY, mDistance);
        //Log.i(TAG, "mDistance == " + mDistance);

        ed.putFloat(AVEGARE_SPEED_KEY, mAverageSpeed);
        //Log.i(TAG, "mAverageSpeed == " + mAverageSpeed);

        ed.commit();

        if (!mCountdownRunning && !mResumeCountdown) {
            mServiceIntent.setAction(Constants.ACTION.STOP_LOCATION_UPDATES);
            startService(mServiceIntent);
        }

        unregisterReceiver(mMyBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        private MyBroadcastReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {

            //Log.i(TAG, "onReceive");

            // Location changed
            if (intent.hasExtra(Constants.STATUS.LOCATION_CHANGED)) {
                int distance = intent.getIntExtra(Constants.STATUS.LOCATION_CHANGED, 0);
                Log.i(TAG, "Location changed: " + distance);
                if (mCountdownRunning && distance > 0) {
                    if (mDistance - distance <= 0) {
                        // Distance left == 0
                        mDistanceView.setText("0");
                        mStartButton.setText(R.string.start);
                        mStartButton.setEnabled(false);
                        mResumeCountdown = false;
                        mResetButton.setEnabled(true);
                        mServiceIntent.setAction(Constants.ACTION.STOP_TIMER);
                        startService(mServiceIntent);
                    } else {
                        mDistanceView.setText(String.valueOf(mDistance - distance));
                    }
                }
            // Elapsed time changed
            } else if (intent.hasExtra(Constants.STATUS.ELAPSED_TIME_CHANGED)) {
                Log.i(TAG, "Elapsed time changed: " + mElapsedTime);

                if (mCountdownRunning) {
                    mElapsedTime = intent.getLongExtra(Constants.STATUS.ELAPSED_TIME_CHANGED, 0);
                }
            // GPS status changed
            } else if (intent.hasExtra(Constants.STATUS.GPS_OK)) {
                int satellites = intent.getIntExtra(Constants.STATUS.GPS_OK, 0);

                if (!mCountdownRunning) {
                    mStartButton.setEnabled(true);
                }

                if (satellites != 0) {
                    Log.i(TAG, "GPS Ok: " + satellites);
                    mGpsStatus.setText(R.string.gps_ok);
                } else {
                    mGpsStatus.setText(R.string.gps_not_ok);
                }
            // GPS status changed to not OK
            } else if (intent.hasExtra(Constants.STATUS.GPS_NOT_OK)) {
                Log.i(TAG, "GPS Not Ok");

                if (!mCountdownRunning) {
                    mStartButton.setEnabled(false);
                }
                mGpsStatus.setText(R.string.gps_not_ok);
            }
            updateUI();
        }
    }
}
