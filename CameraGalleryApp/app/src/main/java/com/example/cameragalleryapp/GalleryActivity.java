package com.example.cameragalleryapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GalleryActivity extends AppCompatActivity implements ImageAdapter.OnImageClickListener {

    private String folderPath;
    private List<File> imageFiles = new ArrayList<>();
    private ImageAdapter adapter;

    // Result launcher for image details
    private final ActivityResultLauncher<Intent> imageDetailsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Always refresh when returning from details, regardless of result
                refreshGallery();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        folderPath = getIntent().getStringExtra("folderPath");
        if (folderPath == null) {
            Toast.makeText(this, "No folder path provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView folderPathText = findViewById(R.id.gallery_folder_path);
        folderPathText.setText("Folder: " + new File(folderPath).getName());

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.gallery_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Initialize adapter with empty list
        adapter = new ImageAdapter(this, imageFiles, this);
        recyclerView.setAdapter(adapter);

        // Load images
        loadImagesFromFolder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGallery();
    }

    private void refreshGallery() {
        loadImagesFromFolder();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadImagesFromFolder() {
        imageFiles.clear();
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            Toast.makeText(this, "Folder does not exist or is not accessible", Toast.LENGTH_SHORT).show();
            return;
        }

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".png"));

        if (files != null && files.length > 0) {
            // Sort by last modified (newest first)
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            // Add valid files to the list
            for (File file : files) {
                if (file.exists() && file.isFile() && file.length() > 0) {
                    imageFiles.add(file);
                }
            }
        }
    }

    @Override
    public void onImageClick(File imageFile) {
        // Launch image details using the result launcher
        Intent intent = new Intent(this, ImageDetailsActivity.class);
        intent.putExtra("imagePath", imageFile.getAbsolutePath());
        imageDetailsLauncher.launch(intent);
    }
}
