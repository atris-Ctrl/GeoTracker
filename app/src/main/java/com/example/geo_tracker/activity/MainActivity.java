package com.example.geo_tracker.activity;

import static com.example.geo_tracker.model.Constants.DEFAULT_POLYLINE_COLOR;
import static com.example.geo_tracker.model.Constants.DEFAULT_POLYLINE_WIDTH;
import static com.example.geo_tracker.model.Constants.STATE_CANCELLED;
import static com.example.geo_tracker.model.Constants.STATE_FINISHED;
import static com.example.geo_tracker.model.Constants.STATE_PAUSED;
import static com.example.geo_tracker.model.Constants.STATE_RECORDING;
import static com.example.geo_tracker.model.Constants.ZOOM_LEVEL;
import static com.example.geo_tracker.UtilityFunctions.formatTime;
import static com.example.geo_tracker.UtilityFunctions.getTodayDate;
import static com.example.geo_tracker.model.Constants.transitnionType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geo_tracker.UtilityFunctions;
import com.example.geo_tracker.databinding.ActivityMainBinding;
import com.example.geo_tracker.service.GeoFenceBroadcastReceiver;
import com.example.geo_tracker.service.GeoFenceHelper;
import com.example.geo_tracker.database.mark.Mark;
import com.example.geo_tracker.database.path.Path;
import com.example.geo_tracker.service.LocationService;
import com.example.geo_tracker.R;
import com.example.geo_tracker.viewModel.MarkViewModel;
import com.example.geo_tracker.viewModel.PathViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;


/**
 * MainActivity class represents the main activity of the application,
 * responsible for displaying and interacting with the map, user location, markers,
 * and managing the recording of paths.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    GeoFenceHelper geoFenceHelper;
    public PolylineOptions polylineOptions;
    public ServiceConnection serviceConnection;
    private boolean addBthClick = false;
    private boolean startTrackingPath = false;
    SupportMapFragment mapFragment;
    TextView distanceText, modeText, timeText;
    BottomNavigationView navBar;
    LocationService locationService;
    PathViewModel pathViewModel;
    MarkViewModel markViewModel;
    Polyline polyline;
    LatLng currentLocation;
    String selectedActivity = "Running";
    Button recordButton, addButton, finishCancelButton, resumePauseButton;
    private RadioGroup radioGroupActivities;
    private List<LatLng> pathList = new ArrayList<>();
    private int currentState;
    private PendingIntent geofencePendingIntent;
    private boolean firstRun;
    private boolean isFirstRun = true;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        pathViewModel = new PathViewModel(getApplication());
        markViewModel = new MarkViewModel(getApplication());
        geofencingClient = LocationServices.getGeofencingClient(this);
        geoFenceHelper = new GeoFenceHelper(this);

        distanceText = findViewById(R.id.distanceText);
        modeText = findViewById(R.id.modeText);
        timeText = findViewById(R.id.timeText);
        navBar = findViewById(R.id.bottom_navigation_view);
        radioGroupActivities = findViewById(R.id.radioGroupActivities);
        addButton = findViewById(R.id.addButton);
        firstRun = true;

        addButton.setOnClickListener(v -> onClickAddMarker());

        recordButton = findViewById(R.id.recordButton);
        finishCancelButton = findViewById(R.id.stopButton);
        resumePauseButton = findViewById(R.id.resumeButton);
        finishCancelButton.setVisibility(View.GONE);
        resumePauseButton.setVisibility(View.GONE);

        setUpRecordResumeStopButtons();
        setRadioButtons();
        setNavBar();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(MainActivity.this);
    }

    /**
     * Sets up click listeners for the record, resume/pause, and finish/cancel buttons.
     * Defines actions based on the current state of the path recording.
     */
    public void setUpRecordResumeStopButtons() {
        recordButton.setOnClickListener(v -> {
            currentState = locationService.getCurrentState().getValue();
            Boolean isTracking = locationService.getIsTrackingPath().getValue();
            if (currentState == STATE_FINISHED || currentState == STATE_CANCELLED) {
                sendActionToService("START_OR_RESUME_SERVICE");
            }
        });
        resumePauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentState = locationService.getCurrentState().getValue();
                if (currentState == STATE_PAUSED) {
                    sendActionToService("START_OR_RESUME_SERVICE");
                } else if (currentState == STATE_RECORDING) {
                    sendActionToService("PAUSE_SERVICE");
                }

            }
        });
        finishCancelButton.setOnClickListener(v -> {
            currentState = locationService.getCurrentState().getValue();
            if (currentState == STATE_PAUSED) {
                Log.d("timer", "onClick: cancel");
                sendActionToService("CANCEL_SERVICE");
                cancelPathOperation();
            } else if (currentState == STATE_RECORDING) {
                Log.d("timer", "onClick: finsih");
                sendActionToService("STOP_SERVICE");
                finishPathOperation();
            }

        });
    }

    public void finishPathOperation() {
        String date = getTodayDate();
        List<LatLng> locationPoints = locationService.getPathPoints().getValue();
        Long milliseconds = locationService.getTimeInMilliseconds().getValue();
        float distance = locationService.getDistance().getValue();
        float avg_speed = distance / (milliseconds / 1000);
        Path path = new Path(selectedActivity, distance, null,
                milliseconds, avg_speed, date, null, null);
        locationService.startOrResetPathInformation();
        removePolyline();
        pathViewModel.insert(path);
        Toast.makeText(this, "Path saved", Toast.LENGTH_SHORT).show();
    }

    /**
     * Finalizes the path recording, saves the path information, and resets recording data.
     * Displays a toast message indicating the successful path saving.
     */
    public void cancelPathOperation() {
        locationService.startOrResetPathInformation();
        removePolyline();
    }


    /**
     * Updates the ui based on the current state of the path recording by setting visibility
     * and text of buttons to reflect the appropriate actions for each state.
     *
     * @param currentState The current state of the path recording (e.g., STATE_RECORDING, STATE_PAUSED).
     */
    private void updateUI(int currentState) {
        switch (currentState) {
            case STATE_RECORDING:
                resumePauseButton.setText("Pause");
                finishCancelButton.setText("Finish");
                resumePauseButton.setVisibility(View.VISIBLE);
                finishCancelButton.setVisibility(View.VISIBLE);
                recordButton.setVisibility(View.GONE);
                break;
            case STATE_PAUSED:
                resumePauseButton.setText("Resume");
                finishCancelButton.setText("Cancel");
                resumePauseButton.setVisibility(View.VISIBLE);
                finishCancelButton.setVisibility(View.VISIBLE);
                recordButton.setVisibility(View.GONE);
                break;
            case STATE_FINISHED:
                resumePauseButton.setVisibility(View.GONE);
                finishCancelButton.setVisibility(View.GONE);
                recordButton.setVisibility(View.VISIBLE);
                break;
            case STATE_CANCELLED:
                resumePauseButton.setVisibility(View.GONE);
                finishCancelButton.setVisibility(View.GONE);
                recordButton.setVisibility(View.VISIBLE);
                break;
        }
    }


    /**
     * Removes the polylines from the map, clears all markers, and updates the Geofences based on stored marks.
     * This method is called when finishing or canceling recording path.
     */
    public void removePolyline() {
        mMap.clear();
        List<Mark> marks = markViewModel.getAllMarks().getValue();
        removeGeoFence();
        for (Mark mark : marks) {
            LatLng latLng = new LatLng(mark.latitude, mark.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(mark.id + ". " + mark.title)
                    .snippet(mark.description));
            addCircleMarker(latLng, 200);
            addGeofenceOnMap(geoFenceHelper.createGeofence(mark.title + "===" + mark.description, latLng, Geofence.NEVER_EXPIRE, transitnionType), mark.id, mark.description);
        }
    }


    /***
     * Sets the navigation bar to the home page.
     * <ref>https://www.youtube.com/watch?v=y4arA4hsok8&t=18s</ref>
     */
    public void setNavBar() {
        navBar.setSelectedItemId(R.id.home);
        navBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.home) {
                    navBar.setSelectedItemId(R.id.home);
                    return true;
                }
                if (id == R.id.navigation_paths) {
                    Intent i = new Intent(MainActivity.this, PathsActivity.class);
                    startActivity(i);
                    return true;
                }

                if (id == R.id.navigation_stat) {
                    Intent i = new Intent(MainActivity.this, StatActivity.class);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });
    }


    /***
     * Draws all the polylines on the map based on the path points stored in the database.
     */
    private void drawAllPolyline() {

        int count = 0;
        polylineOptions = new PolylineOptions()
                .color(DEFAULT_POLYLINE_COLOR)
                .width(DEFAULT_POLYLINE_WIDTH);

        for (int i = 0; i < pathList.size() - 1; i++) {
//            Log.d("comp3018", "drawAllPolyline: " +count + " size: " + pathList.size());
            LatLng lastLocation = pathList.get(i);
            LatLng currentLocation = pathList.get(i + 1);
            polylineOptions.add(lastLocation, currentLocation);
            count++;
            mMap.addPolyline(polylineOptions);
        }

    }

    /**
     * Sends an action to the LocationService based on the action string.
     *
     * @param action The action to be sent to the LocationService.
     */
    private void sendActionToService(String action) {
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(action);
        startService(intent);
    }

    private void startLocationService() {
        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    /**
     * Called when the map is ready to be used. Initializes the map settings, permissions, and UI elements.
     * Starts the location service, focuses on the user's location, and sets up the info window adapter.
     *
     * @param googleMap The GoogleMap instance
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        } else {
            polylineOptions = new PolylineOptions()
                    .color(DEFAULT_POLYLINE_COLOR)
                    .width(DEFAULT_POLYLINE_WIDTH);
            polyline = mMap.addPolyline(polylineOptions);
            mMap.setMyLocationEnabled(true);
            isFirstRun = true;
            drawAllPolyline();
            bindService();
            startLocationService();

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Nullable
                @Override
                public View getInfoContents(@NonNull Marker marker) {
                    View googleMarkView = getLayoutInflater().inflate(R.layout.google_marker_layout, null);
                    TextView titleTextView = googleMarkView.findViewById(R.id.descriptionTitle);
                    TextView descriptionTextView = googleMarkView.findViewById(R.id.descriptionText);
                    TextView removeTextView = googleMarkView.findViewById(R.id.reminderText);
                    titleTextView.setText(marker.getTitle());
                    descriptionTextView.setText(marker.getSnippet());
                    removeTextView.setText("Click to remove");
                    return googleMarkView;
                }

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }
            });
            focusUserLocation();

        }

        mMap.setOnInfoWindowClickListener(marker -> {
            LatLng pos = marker.getPosition();
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(UtilityFunctions.getColorPolyline(selectedActivity));
            polylineOptions.width(DEFAULT_POLYLINE_WIDTH);
            int id = Integer.parseInt(marker.getTitle().split("\\.")[0]);
            markViewModel.deleteMark(id);
        });
    }

    private void focusUserLocation() {
        if (currentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, ZOOM_LEVEL));
        }
    }


    /**
     * Uses the GeofencingClient to remove geofences by providing the geofence pending intent.
     */
    public void removeGeoFence() {
        geofencingClient.removeGeofences(geoFenceHelper.getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("GeoFenceReceiver", "onSuccess: geoFence remove");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geoFenceHelper.getErrorString(e);
                        Log.d("GeoFenceReceiver", "onFailure: " + errorMessage);
                    }
                });
    }

    /**
     * Draws a path polyline on the map if the path list contains at least two points.
     * The polyline color and width are set to default values.
     */
    private void drawPath() {
        if (pathList != null && pathList.size() > 0 && pathList.size() > 1) {
            polylineOptions = new PolylineOptions()
                    .color(DEFAULT_POLYLINE_COLOR)
                    .width(DEFAULT_POLYLINE_WIDTH);
//            Log.d("comp3018", "drawPath: " + pathList.size());
            polylineOptions.add(pathList.get(pathList.size() - 1),
                    pathList.get(pathList.size() - 2));
            mMap.addPolyline(polylineOptions);
        }
    }


    /**
     * Displays a dialog for adding a marker at the given LatLng.
     * The dialog prompts the user to enter a title and description for the marker.
     * If the title and description are provided, a new Mark is created and inserted into the MarkViewModel.
     *
     * @param latLng The LatLng where the marker will be added.
     */
    private void showAddMarkerDialog(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.marker_layout, null);
        builder.setView(dialogView);

        EditText editTextMarkerTitle = dialogView.findViewById(R.id.editTextMarkerTitle);
        EditText editTextMarkerDescription = dialogView.findViewById(R.id.editTextMarkerDescription);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = editTextMarkerTitle.getText().toString();
            String description = editTextMarkerDescription.getText().toString();

            if (!title.isEmpty() || !description.isEmpty()) {
                Mark newMark = new Mark(latLng.latitude, latLng.longitude, title, description);
                markViewModel.insert(newMark);

            } else {
                Toast.makeText(MainActivity.this, "Please enter a title/description for the marker title", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    /**
     * Observes various LiveData from the LocationService to update UI elements and map markers.
     * The observed LiveData includes current location, tracking state, path points, time, distance,
     * and the list of markers.
     */
    private void serviceObserve() {
        locationService.getCurrentLocation().observe(MainActivity.this, location -> {
            if (location != null) {
                currentLocation = location;
                if (isFirstRun || locationService.getIsTrackingPath().getValue())
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, ZOOM_LEVEL));
                    isFirstRun = false;
            }
        });

        locationService.getCurrentState().observe(MainActivity.this, state -> {
            if (state != null) {
                updateUI(state);
            }
        });
        locationService.getPathPoints().observe(this, locations -> {
            if (locations != null && locations.size() > 0) {
                pathList = locations;
                Log.d("PathViewModel", "onChanged: " + locations.size());
                drawPath();
                LatLng lastLocation = locations.get(locations.size() - 1);
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, ZOOM_LEVEL));
//                zoomToUserLocation();
            }
        });


        locationService.getTimeInMilliseconds().observe(this, time -> {
//                Log.d("timer", "** Time : " + time);
            timeText.setText("Time: \n" + formatTime(time));
        });

        locationService.getDistance().observe(this, distance -> {
            if (distance != null) {
                distanceText.setText("Distance: \n" + UtilityFunctions.formatDistance(distance));
            }
        });

        markViewModel.getAllMarks().observe(MainActivity.this, marks -> {
            if (marks != null) {
                mMap.clear();
                drawAllPolyline();
                removeGeoFence();
                for (Mark mark : marks) {
                    LatLng latLng = new LatLng(mark.latitude, mark.longitude);
                    mMap.addMarker(new MarkerOptions().position(latLng).title(mark.id + ". " + mark.title).snippet(mark.description));
                    addCircleMarker(latLng, 200);
                    addGeofenceOnMap(geoFenceHelper.createGeofence(mark.title + "===" + mark.description, latLng, Geofence.NEVER_EXPIRE, transitnionType), mark.id, mark.description);
                }
            }

        });

    }


    public void onClickAddMarker() {
        addBthClick = !addBthClick;
        if (addBthClick) {
            addButton.setText("Finish Add");
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    showAddMarkerDialog(latLng);
                }
            });
        } else {
            addButton.setText("Add Marker");
            mMap.setOnMapClickListener(null);
        }
    }


    /**
     * Adds a geofence to the map based on the given geofence, id, and description.
     * The geofence is added to the geofencing client using the geofence helper.
     *
     * @param geofence    The geofence to be added to the map.
     * @param id          The id of the geofence.
     * @param description The description of the geofence.
     */

    private void addGeofenceOnMap(Geofence geofence, int id, String description) {
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);

        geofencingClient.addGeofences(geoFenceHelper.createGeofencingRequest(geofence), geoFenceHelper.getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("GeoFenceReceiver", "onSuccess: geoFence "
                                + geofence.getRequestId() + " added");
                    }
                })
                .addOnFailureListener(this, e -> {
                    String errorMessage = geoFenceHelper.getErrorString(e);
                    Log.d("GeoFenceReceiver", "onFailure: " + errorMessage);
                });
    }


    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return super.bindService(service, conn, flags);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void bindService() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                LocationService.MyBinder binder = (LocationService.MyBinder) iBinder;
                locationService = binder.getService();
                serviceObserve();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        Log.d("Bind service", "start");
        bindService(new Intent(this, LocationService.class), serviceConnection, BIND_AUTO_CREATE);

    }


    /**
     * Add circle marker on the map based on the given LatLng and radius.
     */
    private void addCircleMarker(LatLng latLng, int radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(R.color.black);
        circleOptions.fillColor(R.color.black);
        mMap.addCircle(circleOptions);

    }

    /**
     * Sets the radio buttons for the activity selection.
     */

    public void setRadioButtons() {
        radioGroupActivities.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton radioButton = findViewById(i);
            selectedActivity = radioButton.getText().toString();
            polylineOptions.color(UtilityFunctions.getColorPolyline(selectedActivity));
            modeText.setText("Mode: \n" + selectedActivity);
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapFragment.onStart();
        drawAllPolyline();
        Log.d("comp3018", "onStart: ");
        bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("comp3018", "onStop: ");
        mapFragment.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapFragment.onLowMemory();
        Log.d("comp3018", "onLowMemory: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("comp3018", "onDestroy: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("comp3018", "onResume: ");
        drawAllPolyline();

        mapFragment.onResume();
    }


}