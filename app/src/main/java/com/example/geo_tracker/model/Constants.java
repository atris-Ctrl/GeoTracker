package com.example.geo_tracker.model;

import android.graphics.Color;

import com.google.android.gms.location.Geofence;

public class Constants {
    public static final int ZOOM_LEVEL = 15;
    public static final int NOTIFICATION_ID = 111;
    public static final int LOCATION_UPDATE_INTERVAL = 5000;
    public static final int FASTEST_LOCATION_INTERVAL = 3000;
    public static final String NOTIFICATION_CHANNEL_ID = "Location_tracking";
    public static final String NOTIFICATION_CHANNEL_NAME = "Location tracking";
    public static final int DEFAULT_POLYLINE_COLOR = Color.RED;
    public static final int RUN_POLYLINE_COLOR = Color.BLUE;
    public static final int CYCLE_POLYLINE_COLOR = Color.GREEN;
    public static final int DEFAULT_POLYLINE_WIDTH = 5;
    public static final int geoFenceRadius = 100;
    public static final int STATE_RECORDING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_FINISHED = 3;
    public static final int STATE_CANCELLED = 4;
    public static final int transitnionType = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;

}
