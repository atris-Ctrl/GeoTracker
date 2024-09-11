package com.example.geo_tracker.database.mark;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Database for Mark objects.
 */
@Database(entities = {Mark.class}, version = 1, exportSchema = false)
public abstract class MarkDatabase extends RoomDatabase {

    private static volatile MarkDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static MarkDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (MarkDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    MarkDatabase.class, "mark_database")
                            .fallbackToDestructiveMigration().build();
                }
            }
        }
        return instance;
    }


    private static RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d("comp3018", "onCreate Mark");
            databaseWriteExecutor.execute(() -> {
                MarkDao markDao = instance.markDao();
                markDao.deleteAll();

            });
        }
    };


    public abstract MarkDao markDao();



}
