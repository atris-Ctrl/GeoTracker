package com.example.geo_tracker.service;

import static com.example.geo_tracker.model.Constants.FASTEST_LOCATION_INTERVAL;
import static com.example.geo_tracker.model.Constants.LOCATION_UPDATE_INTERVAL;
import static com.example.geo_tracker.model.Constants.NOTIFICATION_CHANNEL_ID;
import static com.example.geo_tracker.model.Constants.NOTIFICATION_CHANNEL_NAME;
import static com.example.geo_tracker.model.Constants.NOTIFICATION_ID;
import static com.example.geo_tracker.model.Constants.STATE_CANCELLED;
import static com.example.geo_tracker.model.Constants.STATE_FINISHED;
import static com.example.geo_tracker.model.Constants.STATE_PAUSED;
import static com.example.geo_tracker.model.Constants.STATE_RECORDING;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.geo_tracker.R;
import com.example.geo_tracker.UtilityFunctions;
import com.example.geo_tracker.activity.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import androidx.lifecycle.LifecycleService;

import java.util.ArrayList;
import java.util.List;


public class LocationService extends LifecycleService {
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    NotificationCompat.Builder builder;
    private IBinder mBinder = new MyBinder();
    public static MutableLiveData<Boolean> isFinishPath = new MutableLiveData<>();
    public static MutableLiveData<Boolean> isTrackingPath = new MutableLiveData<>();
    public static MutableLiveData<List<LatLng>> location = new MutableLiveData<>();
    public static MutableLiveData<Long> timeInMilliseconds = new MutableLiveData<>();
    public static MutableLiveData<Long> timeInSeconds = new MutableLiveData<>();

    public static MutableLiveData<Float> distance = new MutableLiveData<>();
    public static MutableLiveData<LatLng> currentLocation = new MutableLiveData<>();
    public static MutableLiveData<Integer> currentState = new MutableLiveData<>();
    private Handler handler = new Handler();
    private Runnable runnable;
    private long totalTimer = 0;
    private long timeStart = 0;
    private long timeElapsed = 0;
    private long lastSecond = 0;
    private boolean isFirstRun;


    /**
     * Start the stopwatch and increment second and millisecond livedata
     */
    private void startStopWatch() {
        timeStart = System.currentTimeMillis();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isTrackingPath.getValue()) {
                    timeElapsed = System.currentTimeMillis() - timeStart;
                    timeInMilliseconds.postValue(totalTimer + timeElapsed);

                    if (timeInMilliseconds.getValue() >= lastSecond + 1000) {
                        timeInSeconds.postValue(timeInSeconds.getValue() + 1);
                        lastSecond += 1000;
                    }
                    handler.postDelayed(this, 200);
                } else {
                    totalTimer += timeElapsed;
                }
            }

        };
        handler.post(runnable);

    }

    public MutableLiveData<Long> getTimeInMilliseconds() {
        return timeInMilliseconds;
    }

    public MutableLiveData<Boolean> getIsTrackingPath() {
        return isTrackingPath;
    }

    public MutableLiveData<List<LatLng>> getPathPoints() {
        return location;
    }

    public MutableLiveData<Float> getDistance() {
        return distance;
    }

    public MutableLiveData<LatLng> getCurrentLocation() {
        return currentLocation;
    }

    public MutableLiveData<Boolean> getIsFinishPath() {
        return isFinishPath;
    }

    public void stopFinishPath() {
        isFinishPath.postValue(false);
    }

    /**
     * Initializes the LocationService, including building a notification, setting up the
     * FusedLocationProviderClient, and resetting path information.
     * Observes the isTrackingPath LiveData to start the stopwatch when tracking begins.
     * Sets up the location callback to handle location updates and updates path information.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        startOrResetPathInformation();
        isTrackingPath.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isTracking) {
                if (isTracking) {
                    startStopWatch();
                }
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location lastLng = locationResult.getLastLocation();
                LatLng latLng = new LatLng(lastLng.getLatitude(), lastLng.getLongitude());
                currentLocation.postValue(latLng);
                if (isTrackingPath.getValue()) {
                    for (Location location : locationResult.getLocations()) {
                        addPathPoint(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                    calculateDistance();
                }

            }
        };
    }

    public MutableLiveData<Integer> getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState.setValue(currentState);
    }

    /**
     * Starts or resets path information to initial values:
     * Sets the current state to STATE_FINISHED.
     * Resets flags and counters for the stopwatch.
     * Resets LiveData values for tracking and path details.
     */
    public void startOrResetPathInformation() {
        isFirstRun = true;
        totalTimer = 0;
        lastSecond = 0;
        timeElapsed = 0;
        timeInMilliseconds.postValue(0L);
        timeInSeconds.postValue(0L);
        isFinishPath.postValue(false);
        isTrackingPath.postValue(false);
        location.postValue(new ArrayList<>());
        distance.postValue(0f);
        currentState.postValue(STATE_FINISHED);

    }

    public void startTracking() {
        isTrackingPath.postValue(true);
    }

    /**
     * Adds a new path point to the list of recorded locations.
     *
     * @param latLng
     */
    public void addPathPoint(LatLng latLng) {
        List<LatLng> pathPoints = location.getValue();
        pathPoints.add(latLng);
//        Log.d("comp3018", "Location size: " + pathPoints.size());
        location.postValue(pathPoints);
    }


    public PendingIntent createPendingIntent() {
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
                i, PendingIntent.FLAG_MUTABLE);
        return pendingIntent;
    }

    /**
     * Creates a PendingIntent with a specified action for use with buttons in the notification.
     *
     * @param action The action to be performed when the PendingIntent is triggered.
     * @return A PendingIntent with the specified action.
     */
    public PendingIntent createPendingIntentOnButton(String action) {
        Intent pauseIntent = new Intent(this, LocationService.class);
        pauseIntent.setAction(action);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_MUTABLE);
        return pausePendingIntent;
    }

    /**
     * Updates the text in the notification.
     *
     * @param text The text to be displayed in the notification.
     */
    private void updateNotificationText(String text) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            builder.setContentText(text);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }


    /**
     * Updates the buttons in the notification based on the current state of the service.
     *
     * @param currentState The current state of the service.
     */
    @SuppressLint("RestrictedApi")
    private void updateNotificationButtons(int currentState) {
        Log.d("comp3018", "updateNotificationButtons " + currentState);
        NotificationCompat.Action pauseAction = new NotificationCompat.Action(R.drawable.baseline_pause_24, "Pause", createPendingIntentOnButton("PAUSE_SERVICE"));
        NotificationCompat.Action resumeAction = new NotificationCompat.Action(R.drawable.baseline_pause_24, "Resume", createPendingIntentOnButton("START_OR_RESUME_SERVICE"));
        builder.mActions.clear();

        switch (currentState) {
            case STATE_RECORDING:
                builder.addAction(pauseAction);
                break;
            case STATE_PAUSED:
                builder.addAction(resumeAction);
                break;
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void buildNotification() {
        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Path tracking starts")
                .setOngoing(true)
                .setContentText("Time elapsed: 00:00:00 Distance: 0m")
                .setContentIntent(createPendingIntent())
                .setSmallIcon(R.drawable.baseline_person_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
    }

    /**
     * Starts the service as a foreground service.
     * Sets up observers for the timeInSeconds and currentState LiveData.
     * Updates the notification text with the current time and distance.
     */
    private void startForegroundService() {

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
        timeInSeconds.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long time) {
                String currentTime = UtilityFunctions.formatTimeInSecond(time);
                String currentDistance = UtilityFunctions.formatDistance(distance.getValue());
                updateNotificationText("Time elapsed: " + currentTime + " Distance: " + currentDistance);
            }
        });
        currentState.observe(this, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer state) {
                        updateNotificationButtons(state);
                    }
                }
        );
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        createNotificationChannel();
        Log.d("comp3018", "onStartCommand Service");
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_LOCATION_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "START_OR_RESUME_SERVICE":
                        if (isFirstRun) {
                            startTracking();
                            isTrackingPath.postValue(true);
                            startForegroundService();
                            isFirstRun = false;
                        } else {
                            startTracking();
                        }
                        currentState.postValue(STATE_RECORDING);
                        break;
                    case "PAUSE_SERVICE":
                        pauseService();
                        currentState.postValue(STATE_PAUSED);
                        break;
                    case "STOP_SERVICE":
                        finishService();
                        currentState.postValue(STATE_CANCELLED);
                        break;
                    case "CANCEL_SERVICE":
                        cancelService();
                        currentState.postValue(STATE_CANCELLED);
                        break;
                }
            }
        }
        return START_STICKY;
    }

    public void pauseService() {
        isTrackingPath.postValue(false);

    }

    public void cancelService() {
        updateNotificationText("Your path is canceled");
        startOrResetPathInformation();

    }

    public void finishService() {
        isTrackingPath.postValue(false);
        updateNotificationText("Your path is saved ! :)");
        isFinishPath.postValue(true);

    }

    /**
     * Calculates the distance between the last two points in the path.
     */
    public void calculateDistance() {
        float[] results = new float[1];
        if (location.getValue().size() > 2) {
            LatLng endPoint = location.getValue().get(location.getValue().size() - 1);
            LatLng prevPoint = location.getValue().get(location.getValue().size() - 2);
            double startPointLat = prevPoint.latitude;
            double startPointLong = prevPoint.longitude;
            double endPointLong = endPoint.longitude;
            double endPointPointLat = endPoint.latitude;
            Location.distanceBetween(startPointLat, startPointLong, endPointPointLat, endPointLong, results);
            distance.postValue(distance.getValue() + results[0]);

        }
    }

    public void removeNotification() {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("comp3018", "onDestroy Service");
    }

    public class MyBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }

    }


}