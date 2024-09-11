package com.example.geo_tracker.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.geo_tracker.database.path.Path;
import com.example.geo_tracker.repository.PathRepository;

import java.util.List;

public class PathViewModel extends AndroidViewModel {

    private PathRepository pathRepository;
    private LiveData<List<Path>> allPaths;
    private LiveData<Float> totalDistance;
    private LiveData<Float> totalTime;
    private LiveData<Float> totalCalories;

    private LiveData<List<Float>> allDistance;
    private LiveData<List<Path>> pathsWithActivityName;



    private MutableLiveData<Integer> currentState = new MutableLiveData<>();
    public PathViewModel(Application application) {
        super(application);
        pathRepository = new PathRepository(application);
        allPaths = pathRepository.getAllPaths();
        totalDistance = pathRepository.getTotalDistance();
        totalTime = pathRepository.getTotalTime();
        allDistance = pathRepository.getAllDistance();

    }
    public LiveData<List<Float>> getAllDistance(){
        return allDistance;
    }
    public LiveData<List<Path>> getAllPathsWithActivity(String activityName) {
        pathsWithActivityName = pathRepository.getAllPathsWithActivityName(activityName);
        return pathsWithActivityName;
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

    public LiveData<Float> getTotalCalories() {
        return totalCalories;
    }

    public void insert(Path path) {
        pathRepository.insert(path);
    }

    public void delete(Path path) {
        pathRepository.delete(path);
    }

    public void deleteAll() {
        pathRepository.deleteAll();
    }
    public void deletePath(int id) {
        pathRepository.deletePath(id);
    }

    public LiveData<Path> getPath(int id) {
        return pathRepository.getPath(id);
    }

    public void updateImageData(Path path) {
        pathRepository.updateImageData(path);
    }
    public void updateDescription(Path path) {pathRepository.updateDescriptionData(path);}

    public LiveData<Float> getTotalDistanceToday(String date) {
        return pathRepository.getTotalDistanceToday(date);
    }

}
