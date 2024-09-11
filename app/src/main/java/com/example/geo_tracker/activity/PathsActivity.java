package com.example.geo_tracker.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.geo_tracker.R;
import com.example.geo_tracker.adaptor.PathAdapter;
import com.example.geo_tracker.database.path.Path;
import com.example.geo_tracker.viewModel.PathViewModel;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

public class PathsActivity extends AppCompatActivity {
    PathAdapter adapter;
    PathViewModel pathViewModel;
    NavigationBarView navBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        pathViewModel = new PathViewModel(getApplication());
        navBar = findViewById(R.id.bottom_navigation);

        setNavBar();

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        pathViewModel.getAllPaths().observe(this, new Observer<List<Path>>() {
            @Override
            public void onChanged(@Nullable final List<Path> paths) {
                adapter = new PathAdapter(paths);
                recyclerView.setAdapter(adapter);

            }
        });
    }

    /**
     *  <ref>https://www.youtube.com/watch?v=y4arA4hsok8&t=18s</ref>
     */
    private void setNavBar(){
        navBar.setSelectedItemId(R.id.navigation_paths);
        navBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.home){
                    Intent i = new Intent(PathsActivity.this, MainActivity.class);
                    startActivity(i);
                    return true;
                }
                if(id == R.id.navigation_paths) {
                    navBar.setSelectedItemId(R.id.navigation_paths);
                    return true;
                }
                if(id == R.id.navigation_stat){
                    Intent i = new Intent(PathsActivity.this, StatActivity.class);
                    startActivity(i);
                    return true;
                }

                return false;
            }
        });
    }


}