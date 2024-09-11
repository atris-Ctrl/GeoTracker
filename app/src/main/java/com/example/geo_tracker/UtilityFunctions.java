package com.example.geo_tracker;

import static com.example.geo_tracker.model.Constants.DEFAULT_POLYLINE_COLOR;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UtilityFunctions {

    public static String formatTime(float timeInMillis){
        int hours = (int) (timeInMillis / (1000 * 60 * 60));
        int minutes = (int) ((timeInMillis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) ((timeInMillis % (1000 * 60)) / 1000);
        return String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, seconds);
    }
    public static String formatTimeInSecond(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    public static String formatDistance(float distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format(Locale.getDefault(), "%.0f m", distanceInMeters);
        } else {
            float distanceInKm = distanceInMeters / 1000;
            return String.format(Locale.getDefault(), "%.2f km", distanceInKm);
        }
    }


    public static int getColorPolyline(String activity) {
        switch (activity) {
            case "Running":
                return DEFAULT_POLYLINE_COLOR;
            case "Walking":
                return Color.parseColor("#4CAF50");
            case "Cycling":
                return Color.parseColor("#2196F3");
            default:
                return Color.BLACK;
        }
    }


    public static String getTodayDate(){
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(today);
    }

    public static byte[] imageToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        return stream.toByteArray();
    }
    public static Bitmap convertByteArrayToBitmap(byte[] byteArr) {
        Bitmap bitMapImage = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
        return bitMapImage;
    }

    public static double calculateAvgSpeed(double distance, double duration) {
        return distance / (duration / 1000);
    }




}
