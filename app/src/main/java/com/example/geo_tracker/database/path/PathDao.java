package com.example.geo_tracker.database.path;
import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// Data access object
@Dao
public interface PathDao {
    @Query("SELECT * FROM path_table")
    LiveData<List<Path>> getAllPaths();
    @Query("SELECT * FROM path_table WHERE name = :activityName")
    LiveData<List<Path>> getAllPathsWithActivityName(String activityName);


    @Insert
    void insert(Path path);

    @Delete
    void delete(Path path);

    @Query("DELETE FROM path_table WHERE id = :id")
    void deletePath(int id);

   @Query("DELETE FROM path_table")
    void deleteAll();

   @Query("SELECT distance from path_table")
   LiveData<List<Float>> getAllDistance();

    @Query("SELECT * FROM path_table WHERE id = :id")
    LiveData<Path> getPath(int id);

    @Query("SELECT SUM(distance) FROM path_table")
    LiveData<Float> getTotalDistance();

    @Query("SELECT SUM(distance) FROM path_table WHERE date = :date")
    LiveData<Float> getTotalDistanceToday(String date);

    @Query("SELECT SUM(time) FROM path_table")
    LiveData<Float> getTotalTime();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateImageData(Path path);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateDescription(Path path);

}

