package com.example.nhom15_roomfinder.activity;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom15_roomfinder.R; // Giả sử có layout item_gallery_image

import java.util.List;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder> {

    private List<String> imageUrls;
    private Context context;

    public ImageGalleryAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                .load(url)
                .fitCenter()
                .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    // ViewHolder với tính năng Zoom
    static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {

        ImageView imageView;
        private ScaleGestureDetector scaleGestureDetector;
        private Matrix matrix = new Matrix();
        private float scale = 1f;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.galleryImageView);
            scaleGestureDetector = new ScaleGestureDetector(itemView.getContext(), this);
            imageView.setOnTouchListener(this);
            imageView.setScaleType(ImageView.ScaleType.MATRIX);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            scale *= scaleFactor;
            scale = Math.max(0.5f, Math.min(scale, 5.0f)); // Giới hạn zoom

            matrix.setScale(scale, scale, detector.getFocusX(), detector.getFocusY());
            imageView.setImageMatrix(matrix);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {}
    }
}
