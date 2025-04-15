package com.example.cameragalleryapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final Context context;
    private final List<File> imageFiles;
    private final OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(File imageFile);
    }

    public ImageAdapter(Context context, List<File> imageFiles, OnImageClickListener listener) {
        this.context = context;
        this.imageFiles = imageFiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File imageFile = imageFiles.get(position);

        // Use Glide for efficient image loading (add this dependency)
        try {
            Glide.with(context)
                    .load(imageFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder) // Create this drawable
                    .error(R.drawable.ic_image_error) // Create this drawable
                    .into(holder.imageView);
        } catch (Exception e) {
            // Fallback to direct loading if Glide fails or is not available
            holder.imageView.setImageURI(Uri.fromFile(imageFile));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageFile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_image);
        }
    }
}
