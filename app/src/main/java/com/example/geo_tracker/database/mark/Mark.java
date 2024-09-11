package com.example.geo_tracker.database.mark;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class to represent a mark.
 */
@Entity(tableName = "mark_table")
public class Mark {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public double latitude;
    public double longitude;
    public String description;
    public String title;
    public Mark(double latitude, double longitude, String title, String description){
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }
}
