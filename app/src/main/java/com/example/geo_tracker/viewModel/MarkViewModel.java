package com.example.geo_tracker.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.geo_tracker.database.mark.Mark;
import com.example.geo_tracker.repository.MarkRepository;

import java.util.List;

/**
 * ViewModel for Mark objects.
 *
 */
public class MarkViewModel extends AndroidViewModel {

    private MarkRepository markRepository;
    private LiveData<List<Mark>> allMarks;

    public MarkViewModel(Application application) {
        super(application);
        markRepository = new MarkRepository(application);
        allMarks = markRepository.getAllMarks();
    }

    public LiveData<List<Mark>> getAllMarks() {
        return allMarks;
    }

    public void insert(Mark mark) {
        markRepository.insert(mark);
    }

    public void delete(Mark mark) {
        markRepository.delete(mark);
    }

    public void deleteAll() {
        markRepository.deleteAll();
    }

    public void updateDescription(Mark mark) {
        markRepository.updateDescription(mark);
    }

    public void deleteMark(int id) {
        markRepository.deleteMark(id);
    }
    public LiveData<Mark> getMark(int id) {
        return markRepository.getMark(id);
    }
}
