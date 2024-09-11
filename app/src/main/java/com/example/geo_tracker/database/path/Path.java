package com.example.geo_tracker.database.path;
//entity for run database

import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
//    https://developer.android.com/training/data-storage/room/defining-data

@Entity(tableName = "path_table")
public class Path extends BaseObservable {


    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "name")
    public String name;
    @Nullable
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] image;

    @ColumnInfo(name = "distance")
    public float distance;
    @ColumnInfo(name = "time")
    public float time;
    @ColumnInfo(name = "avg_speed")
    public float avg_speed;

    @Nullable
    @ColumnInfo(name = "description")
    public String description;
    @Nullable
    @ColumnInfo(name = "weather")
    public String weather;
    @ColumnInfo(name = "date")
    public String date;

    public Path(String name, float distance, byte[] image,float time, float avg_speed,String date,String description, String weather){
        this.name = name;
        this.distance = distance;
        this.time = time;
        this.image = image;
        this.date = date;
        this.avg_speed = avg_speed;
        this.weather = weather;
        this.description = description;
    }

    public String getDate() {
        return date;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public byte[] getImage() {
        return image;
    }

    public void setImage(@Nullable byte[] image) {
        this.image = image;
    }

    public float getDistance() {
        return distance;
    }

    @Bindable
    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public float getAvg_speed() {
        return avg_speed;
    }

    public void setAvg_speed(float avg_speed) {
        this.avg_speed = avg_speed;
    }



    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public String getWeather() {
        return weather;
    }

    public void setWeather(@Nullable String weather) {
        this.weather = weather;
    }

}
