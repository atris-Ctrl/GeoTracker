package com.example.geo_tracker.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.geo_tracker.R;
import com.example.geo_tracker.activity.MainActivity;
import com.example.geo_tracker.viewModel.MarkViewModel;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Broadcast receiver for geofence transitions.
 * <ref>https://developer.android.com/develop/sensors-and-location/location/geofencing</ref>
 */
public class GeoFenceBroadcastReceiver extends BroadcastReceiver {

    public String TAG = "GeoFenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Broadcast is recieving");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofence error: " + geofencingEvent.getErrorCode());
            return;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
                || transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for (Geofence geofence : triggeringGeofences) {
                String geofenceId = geofence.getRequestId();
                String[] splitStrings = geofenceId.split("===");
                String title = splitStrings[0];
                String description = splitStrings[1];
                if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    Log.i(TAG, "Entered geofence: " + geofenceId + " " + description);
                    handleNotification(context, "Entered " + title + " Geofence", description);
                } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    Log.i(TAG, "Exited geofence: " + geofenceId);
                    handleNotification(context, "Exit " + title + " Geofence", description);
                }
            }
        }
    }

    private void handleNotification(Context context, String title, String message) {
        sendNotification(context, title, message);
    }


    /**
     * Send a notification.
     */
    public void sendNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null)
            return;
        String channelId = "Geofence Detected";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Geofence Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(createNotificationPendingIntent(context));
        notificationManager.notify(1, builder.build());

    }
    public PendingIntent createNotificationPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

}





