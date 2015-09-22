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
        String STOP_TIMER = "com.example.hth.distancecountdown.stop_timer";
    }

    public interface STATUS {
        String ELAPSED_TIME_CHANGED = "com.example.hth.distancecountdown.action.time_changed";
        String LOCATION_CHANGED = "com.example.hth.distancecountdown.action.location_changed";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 1;
    }
}
