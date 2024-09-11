package com.example.geo_tracker.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geo_tracker.R;
import com.example.geo_tracker.UtilityFunctions;
import com.example.geo_tracker.viewModel.PathViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class StatActivity extends AppCompatActivity {
//    https://www.youtube.com/watch?v=nOtlFl1aUCw
    NavigationBarView navBar;

    PathViewModel pathViewModel;

    LineChart lineChart;
    TextView totalDistanceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);
        navBar = findViewById(R.id.bottom_navigation_view);
        setNavBar();
        lineChart = findViewById(R.id.line_chart);
        totalDistanceText = findViewById(R.id.textView2);
        setupLineChart();
        observeDistanceData();
        String todayDate = UtilityFunctions.getTodayDate();
        pathViewModel.getTotalDistanceToday(todayDate).observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float totalDistance) {
                if(totalDistance != null){
                    totalDistanceText.setText("You have travelled " + UtilityFunctions.formatDistance(totalDistance)+" today!");
                }
                else{
                    totalDistanceText.setText("You haven't travelled today");}
            }
        });

    }


    private void setupLineChart() {
        lineChart.setVisibility(View.VISIBLE);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        Description description = new Description();
        description.setText("Distance Trend Over Time");
        lineChart.setDescription(description);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
    }

    private void observeDistanceData() {
        pathViewModel = new PathViewModel(getApplication());
        pathViewModel.getAllDistance().observe(this, distanceList -> {
            if (distanceList != null && distanceList.size() > 0) {
                List<Entry> entries = new ArrayList<>();
                for (int i = 0; i < distanceList.size(); i++) {
                    entries.add(new Entry(i, distanceList.get(i)));
                }

                LineDataSet dataSet = new LineDataSet(entries, "Distance Trend");
                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                dataSet.setValueTextColor(Color.BLACK);
                dataSet.setValueTextSize(13f);
                dataSet.setLineWidth(3f);
                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                lineChart.invalidate();
            }
        });
    }


    /**
     * <ref>https://www.youtube.com/watch?v=y4arA4hsok8&t=18s</ref>
     */
    public void setNavBar(){
        navBar.setSelectedItemId(R.id.navigation_stat);

        navBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.home){
                    Intent i = new Intent(StatActivity.this, MainActivity.class);
                    startActivity(i);
                    return true;
                }
                if(id == R.id.navigation_paths) {
                    Intent i = new Intent(StatActivity.this, PathsActivity.class);
                    startActivity(i);
                    return true;
                }

                if(id == R.id.navigation_stat){
                    navBar.setSelectedItemId(R.id.navigation_stat);
                    return true;
                }
                return false;
            }
        });



    }

}