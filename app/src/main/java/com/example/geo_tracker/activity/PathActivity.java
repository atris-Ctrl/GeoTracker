package com.example.geo_tracker.activity;

import static com.example.geo_tracker.UtilityFunctions.convertByteArrayToBitmap;
import static com.example.geo_tracker.UtilityFunctions.imageToByteArray;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geo_tracker.R;
import com.example.geo_tracker.database.mark.Mark;
import com.example.geo_tracker.database.path.Path;
import com.example.geo_tracker.databinding.ActivityMainBinding;
import com.example.geo_tracker.databinding.ActivityPathBinding;
import com.example.geo_tracker.viewModel.PathViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.material.navigation.NavigationBarView;

public class PathActivity extends AppCompatActivity {
    PathViewModel pathViewModel;
    ActivityResultLauncher<Intent> resultLauncher;
    private int path_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPathBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_path);

        initResultLauncher();
        Intent i = getIntent();
        pathViewModel = new PathViewModel(getApplication());
        Button editButton = findViewById(R.id.editButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            pathViewModel.deletePath(path_id);
            finish();
        });
        editButton.setOnClickListener(v -> {openEditDialog();});
        ImageView imageView = findViewById(R.id.imageView);
        path_id = i.getIntExtra("path_id", -1);
        if (path_id == -1) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            TextView textView = findViewById(R.id.text_title);
            textView.setText((CharSequence) pathViewModel.getAllPaths().getValue());
            pathViewModel.getPath(path_id).observe(this, path -> {
                if (path != null) {
                    if(path.image != null) {
                        imageView.setImageBitmap(convertByteArrayToBitmap(path.image));
                    }
                    binding.setPathXML(path);

                }
            });
        }
    }

    public void onClickPath(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        resultLauncher.launch(intent);
    }
    private void insertPath(byte[] image){
        pathViewModel.getPath(path_id).observe(this, path -> {
            path.image = image;
            pathViewModel.updateImageData(path);
        });
    }


    /**
     * Open a dialog to edit the description of the path.
     */
    private void openEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_path_dialog, null);
        builder.setView(dialogView);

        EditText descriptionEditText = dialogView.findViewById(R.id.edit_text_description);

        Button editImageButton = dialogView.findViewById(R.id.editImageButton);
        editImageButton.setOnClickListener(v -> {onClickPath();});
        builder.setPositiveButton("Add", (dialog, which) -> {
            String description = descriptionEditText.getText().toString();
            if (!description.isEmpty()) {
                Toast.makeText(PathActivity.this, "change added", Toast.LENGTH_SHORT).show();
                pathViewModel.getPath(path_id).observe(this, path -> {
                    path.weather = description;
                    pathViewModel.updateDescription(path);
                });
            } else {
                Toast.makeText(PathActivity.this, "Please enter a title for the marker title", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void exitButton(View v){
        finish();
    }


    /**
     * Initialise the result launcher for the image picker.
     */
    private void initResultLauncher(){
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            Uri imageUri = result.getData().getData();
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            byte[] bytes= imageToByteArray(bitmap);
                            insertPath(bytes);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(PathActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }


}