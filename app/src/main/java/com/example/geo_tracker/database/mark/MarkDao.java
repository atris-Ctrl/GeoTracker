package com.example.geo_tracker.database.mark;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import java.util.List;

/**
 * Data access object to access the mark table.
 */
@Dao
public interface MarkDao {

    @Insert
    void insert(Mark mark);

    @Delete
    void delete(Mark mark);

    @Query("DELETE FROM mark_table WHERE id = :id")
    void deleteMark(int id);


    @Query("DELETE FROM mark_table")
    void deleteAll();

    @Query("SELECT * FROM mark_table")
    LiveData<List<Mark>> getAllMarks();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateDescription(Mark mark);

    @Query("SELECT * FROM mark_table WHERE id = :id")
    LiveData<Mark> getMark(int id);

}
