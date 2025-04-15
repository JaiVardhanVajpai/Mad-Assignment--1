package com.example.cameragalleryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderPickerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private List<FolderItem> folderItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_picker);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        prepareFolderList();
        adapter = new FolderAdapter(folderItems);
        recyclerView.setAdapter(adapter);
    }

    private void prepareFolderList() {
        File appPicturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (appPicturesDir != null) {
            folderItems.add(new FolderItem("App Photos", appPicturesDir.getAbsolutePath()));
        }

        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File cameraDir = new File(dcimDir, "Camera");

        if (picturesDir.exists()) {
            folderItems.add(new FolderItem("Pictures", picturesDir.getAbsolutePath()));
        }

        if (dcimDir.exists()) {
            folderItems.add(new FolderItem("DCIM", dcimDir.getAbsolutePath()));
        }

        if (cameraDir.exists()) {
            folderItems.add(new FolderItem("Camera", cameraDir.getAbsolutePath()));
        }
    }

    private void openGallery(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg") ||
                        name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("No Images")
                    .setMessage("No images found in this folder")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putExtra("folderPath", folderPath);
        startActivity(intent);
    }

    class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
        private List<FolderItem> folders;

        FolderAdapter(List<FolderItem> folders) {
            this.folders = folders;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FolderItem item = folders.get(position);
            holder.folderName.setText(item.getName());
            holder.itemView.setOnClickListener(v -> openGallery(item.getPath()));
        }

        @Override
        public int getItemCount() {
            return folders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView folderName;

            ViewHolder(View itemView) {
                super(itemView);
                folderName = itemView.findViewById(R.id.folder_name);
            }
        }
    }

    class FolderItem {
        private final String name;
        private final String path;

        FolderItem(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }
}
