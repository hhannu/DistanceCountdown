package com.example.hth.distancecountdown;

/**
 * Created by hth on 9/16/15.
 */
public class Constants {

    public interface ACTION {

        String BROADCAST_ACTION = "com.example.hth.distancecountdown.BROADCAST";
        String MAIN_ACTION = "com.example.hth.distancecountdown.action.main";
        String START_LOCATION_MANAGER = "com.example.hth.distancecountdown.start_locationmanager";
        String STOP_LOCATION_MANAGER = "com.example.hth.distancecountdown.stop_locationmanager";
        String START_LOCATION_UPDATES = "com.example.hth.distancecountdown.start_locationupdates";
        String STOP_LOCATION_UPDATES = "com.example.hth.distancecountdown.stop_locationupdates";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 1;
    }
}
