package com.example.geo_tracker.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.geo_tracker.database.path.Path;
import com.example.geo_tracker.database.path.PathDao;
import com.example.geo_tracker.database.path.PathDatabase;

import java.util.List;

public class PathRepository {
    private PathDao pathDao;
    private LiveData<List<Path>> allPaths;
    private LiveData<Float> totalDistance;
    private LiveData<Float> totalTime;
    private LiveData<String> weather;
    private LiveData<String> description;

    public PathRepository(Application application) {
        PathDatabase database = PathDatabase.getDatabase(application);
        pathDao = database.pathDao();
        allPaths = pathDao.getAllPaths();
        totalDistance = pathDao.getTotalDistance();
        totalTime = pathDao.getTotalTime();

    }

    public LiveData<List<Float>> getAllDistance() {
        return pathDao.getAllDistance();
    }

    public LiveData<List<Path>> getAllPaths() {
        return allPaths;
    }

    public LiveData<Float> getTotalDistance() {
        return totalDistance;
    }

    public LiveData<Float> getTotalTime() {
        return totalTime;
    }

    public LiveData<List<Path>> getAllPathsWithActivityName(String activityName) {
        return pathDao.getAllPathsWithActivityName(activityName);
    }
    public void insert(Path path) {
        PathDatabase.databaseWriteExecutor.execute(() -> {
            pathDao.insert(path);
        });
    }

    public void delete(Path path) {
        PathDatabase.databaseWriteExecutor.execute(() -> {
            pathDao.delete(path);
        });
    }

    public void deleteAll() {
        PathDatabase.databaseWriteExecutor.execute(() -> {
            pathDao.deleteAll();
        });
    }

    public LiveData<Path> getPath(int id) {
        return pathDao.getPath(id);
    }

    public void updateImageData(Path path) {
        PathDatabase.databaseWriteExecutor.execute(() -> {
            pathDao.updateImageData(path);
        });
    }

    public void updateDescriptionData(Path path){
        PathDatabase.databaseWriteExecutor.execute(() -> {
            pathDao.updateDescription(path);
        });
    }

    public void deletePath(int id){
        PathDatabase.databaseWriteExecutor.execute(() -> {
            pathDao.deletePath(id);
        });
    }
    public LiveData<Float> getTotalDistanceToday(String date){
        return pathDao.getTotalDistanceToday(date);
    }

}
