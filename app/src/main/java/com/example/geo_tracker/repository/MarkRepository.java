package com.example.geo_tracker.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.geo_tracker.database.mark.Mark;
import com.example.geo_tracker.database.mark.MarkDao;
import com.example.geo_tracker.database.mark.MarkDatabase;

import java.util.List;

/**
 * Repository class to handle the data operations.
 */
public class MarkRepository {
    private MarkDao markDao;
    private LiveData<List<Mark>> allMarks;

    public MarkRepository(Application application) {
        MarkDatabase db = MarkDatabase.getDatabase(application);
        markDao = db.markDao();
        allMarks = markDao.getAllMarks();
    }

    public LiveData<List<Mark>> getAllMarks() {
        Log.d("comp3018", "getAllMarks");
        return allMarks;
    }

    public void insert(Mark mark) {
        MarkDatabase.databaseWriteExecutor.execute(() -> {
            markDao.insert(mark);
        });
    }

    public void delete(Mark mark) {
        MarkDatabase.databaseWriteExecutor.execute(() -> {
            markDao.delete(mark);
        });
    }

    public void deleteAll() {
        MarkDatabase.databaseWriteExecutor.execute(() -> {
            markDao.deleteAll();
        });
    }

    public void updateDescription(Mark mark) {
        MarkDatabase.databaseWriteExecutor.execute(() -> {
            markDao.updateDescription(mark);
        });
    }

    public void deleteMark(int id) {
        MarkDatabase.databaseWriteExecutor.execute(() -> {
            markDao.deleteMark(id);
        });
    }

    public LiveData<Mark> getMark(int id) {
        return markDao.getMark(id);
    }
}
