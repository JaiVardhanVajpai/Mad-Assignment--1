package com.example.cameragalleryapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";
    private ImageView previewImageView;
    private TextView folderPathText;
    private String currentPhotoPath;
    private File storageDir;
    private Uri photoUri;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        // Update system gallery
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isExternalStorageDirectory()) {
                            // For Android 10+ using MediaStore
                            Toast.makeText(this, "Photo saved to: " + storageDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        } else {
                            // For older Android using broadcast
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            File f = new File(currentPhotoPath);
                            Uri contentUri = Uri.fromFile(f);
                            mediaScanIntent.setData(contentUri);
                            sendBroadcast(mediaScanIntent);
                            Toast.makeText(this, "Photo saved to: " + storageDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        }

                        // Display the image preview
                        previewImageView.setImageURI(Uri.parse(currentPhotoPath));
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing camera result: " + e.getMessage(), e);
                        Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewImageView = findViewById(R.id.preview_image);
        folderPathText = findViewById(R.id.folder_path_text);
        Button takePictureButton = findViewById(R.id.take_picture_button);
        Button chooseFolderButton = findViewById(R.id.choose_folder_button);

        // Set default storage directory to app-specific directory
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        updateFolderPathText();

        takePictureButton.setOnClickListener(view -> takePicture());
        chooseFolderButton.setOnClickListener(view -> chooseFolder());
    }

    private void updateFolderPathText() {
        folderPathText.setText("Folder: " + storageDir.getAbsolutePath());
    }

    private boolean isExternalStorageDirectory() {
        return !storageDir.getAbsolutePath().contains(getPackageName());
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            Uri photoUri = null;

            try {
                // For Android 10+ and public directories, use MediaStore
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isExternalStorageDirectory()) {
                    ContentValues values = new ContentValues();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + ".jpg";

                    values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                    // Set the correct relative path based on folder choice
                    String relativePath = getRelativePathFromStorageDir();
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);

                    photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    currentPhotoPath = getPathFromUri(photoUri);
                } else {
                    // For app-specific directories or older Android, use file creation
                    photoFile = createImageFile();
                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(this,
                                "com.example.cameragalleryapp.fileprovider",
                                photoFile);
                        currentPhotoPath = photoFile.getAbsolutePath();
                    }
                }

                if (photoUri != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    cameraLauncher.launch(takePictureIntent);
                    this.photoUri = photoUri;
                } else {
                    Toast.makeText(this, "Could not create photo file", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error taking picture: " + e.getMessage(), e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private String getRelativePathFromStorageDir() {
        String path = storageDir.getAbsolutePath();
        if (path.contains("DCIM")) {
            return "DCIM/CameraApp";
        } else if (path.contains("Pictures")) {
            return "Pictures/CameraApp";
        } else {
            return "Pictures/CameraApp"; // Default
        }
    }

    private String getPathFromUri(Uri uri) {
        // This is a placeholder - actual path can't be reliably determined from content URI
        // For our purposes, we'll just use the URI string
        return uri.toString();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        if (!storageDir.exists()) {
            boolean success = storageDir.mkdirs();
            if (!success) {
                Log.e(TAG, "Failed to create directory: " + storageDir.getAbsolutePath());
                Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
            }
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        return image;
    }

    private void chooseFolder() {
        final String[] folders = new String[] {
                "App Photos (Private)",
                "Pictures (Public)",
                "DCIM (Public)",
                "Camera (Public)"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a folder");
        builder.setItems(folders, (dialog, which) -> {
            String selectedFolder = folders[which];

            switch (selectedFolder) {
                case "App Photos (Private)":
                    storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    break;
                case "Pictures (Public)":
                    storageDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "CameraApp");
                    break;
                case "DCIM (Public)":
                    storageDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM), "CameraApp");
                    break;
                case "Camera (Public)":
                    storageDir = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM), "Camera");
                    break;
            }

            if (!storageDir.exists()) {
                boolean success = storageDir.mkdirs();
                if (!success && !isExternalStorageDirectory()) {
                    // Only show error for app-specific directories, as we handle public dirs differently
                    Log.e(TAG, "Failed to create directory: " + storageDir.getAbsolutePath());
                    Toast.makeText(CameraActivity.this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                }
            }

            updateFolderPathText();

            if (isExternalStorageDirectory() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Toast.makeText(CameraActivity.this,
                        "Note: On Android 10+, photos will be saved to system gallery folders",
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.show();
    }
}
