package com.example.cameragalleryapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailsActivity extends AppCompatActivity {
    private static final String TAG = "ImageDetailsActivity";
    private String imagePath;
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        // Get image path from intent
        imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath == null) {
            Toast.makeText(this, "Error: No image path provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Toast.makeText(this, "Error: Image file not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        ImageView imageView = findViewById(R.id.detail_image_view);
        TextView nameText = findViewById(R.id.image_name_text);
        TextView pathText = findViewById(R.id.image_path_text);
        TextView sizeText = findViewById(R.id.image_size_text);
        TextView dateText = findViewById(R.id.image_date_text);
        Button deleteButton = findViewById(R.id.delete_button);

        try {
            // Load image using direct URI
            Uri imageUri = Uri.fromFile(imageFile);
            imageView.setImageURI(imageUri);

            // Set text fields with image details
            nameText.setText("Name: " + imageFile.getName());
            pathText.setText("Path: " + imagePath);
            sizeText.setText("Size: " + getReadableFileSize(imageFile.length()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String dateModified = sdf.format(new Date(imageFile.lastModified()));
            dateText.setText("Date: " + dateModified);

        } catch (Exception e) {
            Log.e(TAG, "Error loading image details: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading image details", Toast.LENGTH_SHORT).show();
        }

        // Set delete button click listener
        deleteButton.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteImage())
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.1f %s",
                size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private void deleteImage() {
        boolean deleted = false;
        String errorMessage = "";

        try {
            // Check if it's in app's private directory
            if (imagePath.contains(getPackageName())) {
                // For app-private storage, direct deletion works
                deleted = imageFile.delete();
                if (!deleted) {
                    errorMessage = "Cannot delete from app's private storage";
                }
            }
            // For Android 10 and above, use MediaStore for public storage
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();

                // Try to find the image in MediaStore
                String selection = MediaStore.Images.Media.DATA + "=?";
                String[] selectionArgs = new String[]{imagePath};
                Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                deleted = resolver.delete(queryUri, selection, selectionArgs) > 0;

                if (!deleted) {
                    // Fallback: try by display name
                    selection = MediaStore.Images.Media.DISPLAY_NAME + "=?";
                    selectionArgs = new String[]{imageFile.getName()};
                    deleted = resolver.delete(queryUri, selection, selectionArgs) > 0;
                }

                if (!deleted) {
                    // Last resort: direct delete
                    deleted = imageFile.delete();
                }

                if (!deleted) {
                    errorMessage = "Cannot delete from public storage on Android 10+. Check Storage Permissions in Settings.";
                }
            }
            // For Android 9 and below
            else {
                // Direct deletion should work if we have write permission
                deleted = imageFile.delete();

                if (deleted) {
                    // Update media store to remove from gallery
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(imageFile);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                } else {
                    errorMessage = "Cannot delete file. Make sure you've granted Storage permissions.";
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception deleting file: " + e.getMessage(), e);
            errorMessage = "Permission denied. Please grant Storage permissions in App Settings.";
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image: " + e.getMessage(), e);
            errorMessage = "Error: " + e.getMessage();
        }

        if (deleted) {
            Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
            // Return to gallery
            setResult(RESULT_OK);
            finish();
        } else {
            // Show a more helpful error dialog
            new AlertDialog.Builder(this)
                    .setTitle("Deletion Failed")
                    .setMessage(errorMessage + "\n\nTo fix this issue:\n" +
                            "1. Go to App Settings\n" +
                            "2. Open Permissions\n" +
                            "3. Grant Storage/Files permission\n\n" +
                            "For Android 11+, you may need to:\n" +
                            "4. Enable 'Access all files' option")
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Close", null)
                    .show();
        }
    }
}
