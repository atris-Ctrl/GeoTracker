package com.example.geo_tracker.database.path;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Path.class}, version = 1,exportSchema = false)
public abstract class PathDatabase extends RoomDatabase {
// From Lab 6
    private static volatile PathDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static PathDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PathDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PathDatabase.class, "path_database")
                            .fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }


    private static RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d("comp3018", "onCreate");
            databaseWriteExecutor.execute(() -> {
                PathDao pathDao = INSTANCE.pathDao();
                pathDao.deleteAll();

            });
        }
    };


    public abstract PathDao pathDao();
}
