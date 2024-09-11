package com.example.geo_tracker.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceHelper extends ContextWrapper{

    PendingIntent pendingIntent;

    public GeoFenceHelper(Context base) {
        super(base);
    }


    public PendingIntent getGeofencePendingIntent() {
        if(pendingIntent != null){
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeoFenceBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 7878, intent, PendingIntent.FLAG_MUTABLE);
        return pendingIntent;
    }

    public Geofence createGeofence(String id, LatLng latLng, long expirationDuration, int transitionType){
        return new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(latLng.latitude, latLng.longitude, 200)
                .setExpirationDuration(expirationDuration)
                .setTransitionTypes(transitionType)
                .build();
    }

    public GeofencingRequest createGeofencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }



    public String getErrorString(Exception e){
        if (e instanceof ApiException){
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode()){
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_NOT_AVAILABLE";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
            }
        }
        return e.getLocalizedMessage();
    }
}
