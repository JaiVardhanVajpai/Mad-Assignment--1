package com.example.cameragalleryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        } else { // Android 10 and below
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }

    private Button takePhotoButton;
    private Button viewGalleryButton;
    private TextView permissionMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePhotoButton = findViewById(R.id.take_photo_button);
        viewGalleryButton = findViewById(R.id.view_gallery_button);
        permissionMessage = findViewById(R.id.permission_message);

        // Check and request permissions
        if (!hasPermissions()) {
            requestPermissions();
        } else {
            updatePermissionStatus(true);
        }

        takePhotoButton.setOnClickListener(view -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            } else {
                showPermissionExplanationDialog();
            }
        });

        viewGalleryButton.setOnClickListener(view -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, FolderPickerActivity.class));
            } else {
                showPermissionExplanationDialog();
            }
        });
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("This app requires camera and storage permissions to function properly. Please grant all permissions.")
                .setPositiveButton("Grant Permissions", (dialog, which) -> requestPermissions())
                .setNegativeButton("App Settings", (dialog, which) -> {
                    // Open app settings so user can grant permissions manually
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus(hasPermissions());
    }

    private void updatePermissionStatus(boolean hasPermissions) {
        if (hasPermissions) {
            permissionMessage.setVisibility(View.GONE);
            takePhotoButton.setEnabled(true);
            viewGalleryButton.setEnabled(true);
        } else {
            permissionMessage.setVisibility(View.VISIBLE);
            permissionMessage.setText("Please grant all permissions to use the app");
            takePhotoButton.setEnabled(false);
            viewGalleryButton.setEnabled(false);
        }
    }

    private boolean hasPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;

            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            } else {
                allGranted = false;
            }

            updatePermissionStatus(allGranted);

            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                showPermissionExplanationDialog();
            }
        }
    }
}
