package com.example.hth.distancecountdown;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends Activity implements LocationListener {

    protected static final String TAG = "distance countdown";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String COUNTDOWN_RUNNING_KEY = "countdown-running-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    
    protected LocationRequest mLocationRequest;

    protected Location mCurrentLocation;

    // UI Widgets.
    protected Button mStartButton;
    protected Button mResetButton;
    protected TextView mTimeView;
    protected TextView mSpeedView;
    protected EditText mDistanceView;

    protected Boolean mCountdownRunning;
    protected String mLastUpdateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Locate the UI widgets.
        mStartButton = (Button) findViewById(R.id.startButton);
        mResetButton = (Button) findViewById(R.id.resetButton);
        mTimeView = (TextView) findViewById(R.id.timeView);
        mSpeedView = (TextView) findViewById(R.id.speedView);
        mDistanceView = (EditText) findViewById(R.id.distanceView);

        mCountdownRunning = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mCountdownRunning from the Bundle, and make sure that
            // buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(COUNTDOWN_RUNNING_KEY)) {
                mCountdownRunning = savedInstanceState.getBoolean(
                        COUNTDOWN_RUNNING_KEY);
                setButtons();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Set the desired interval for active location updates
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Set the fastest rate for active location updates.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Handles the Start button
     */
    public void startButtonHandler(View view) {
        if (!mCountdownRunning) {
            mCountdownRunning = true;
            mDistanceView.setEnabled(false);
            setButtons();
        }
        else{
            mCountdownRunning = false;
            setButtons();
        }
    }

    /**
     * Handles the Reset button
     */
    public void resetButtonHandler(View view) {
        mResetButton.setEnabled(false);
        mDistanceView.setEnabled(true);
        mStartButton.setText(R.string.start);
        mStartButton.setEnabled(true);
    }

    private void setButtons() {
        if (mCountdownRunning) {
            mStartButton.setText(R.string.stop);
            mResetButton.setEnabled(false);
        } else {
            if(mDistanceView.getText().toString().equals("0")) {
                Log.i(TAG, "StartButton pressed, distance left == 0");
                mStartButton.setText(R.string.start);
                mStartButton.setEnabled(false);
            }
            else {
                Log.i(TAG, "StartButton pressed");
                mStartButton.setText(R.string.resume);
            }
            mResetButton.setEnabled(true);
        }
    }

    /**
     * Updates the distance, average speed and elapsed time in the UI.
     */
    private void updateUI() {
        if (mCurrentLocation != null) {
            //mDistanceView.setText();
            //mSpeedView.setText();
            //mTimeView.setText();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(COUNTDOWN_RUNNING_KEY, mCountdownRunning);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
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
}
