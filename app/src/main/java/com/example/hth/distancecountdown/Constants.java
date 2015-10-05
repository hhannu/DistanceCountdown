package com.example.hth.distancecountdown;

/**
 * Created by hth on 9/16/15.
 */
public class Constants {

    public interface ACTION {
        String BROADCAST = "com.example.hth.distancecountdown.BROADCAST";
        String START_LOCATION_UPDATES = "com.example.hth.distancecountdown.start_locationupdates";
        String STOP_LOCATION_UPDATES = "com.example.hth.distancecountdown.stop_locationupdates";
        String START_TIMER = "com.example.hth.distancecountdown.start_timer";
        String PAUSE_TIMER = "com.example.hth.distancecountdown.pause_timer";
        String STOP_TIMER = "com.example.hth.distancecountdown.stop_timer";
        String RESET = "com.example.hth.distancecountdown.reset";
    }

    public interface STATUS {
        String ELAPSED_TIME_CHANGED = "com.example.hth.distancecountdown.action.time_changed";
        String LOCATION_CHANGED = "com.example.hth.distancecountdown.action.location_changed";
        String GPS_OK = "com.example.hth.distancecountdown.gps_ok";
        String GPS_NOT_OK = "com.example.hth.distancecountdown.gps_not_ok";
        String DISTANCE = "com.example.hth.distancecountdown.distance";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 1;
    }
}
