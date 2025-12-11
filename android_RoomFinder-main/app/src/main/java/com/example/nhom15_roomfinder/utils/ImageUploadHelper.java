package com.example.nhom15_roomfinder.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * ImageUploadHelper - Helper class để upload ảnh với các use case cụ thể
 * 
 * Các use case:
 * 1. Upload ảnh profile (avatar)
 * 2. Upload ảnh phòng trọ (tối đa 5 ảnh)
 * 3. Upload ảnh tin nhắn (chat)
 */
public class ImageUploadHelper {
    
    private static final String TAG = "ImageUploadHelper";
    
    // Folder names trên Cloudinary
    public static final String FOLDER_AVATARS = "roomfinder/avatars";
    public static final String FOLDER_ROOMS = "roomfinder/rooms";
    public static final String FOLDER_CHAT = "roomfinder/chat";
    
    // Giới hạn số ảnh
    public static final int MAX_ROOM_IMAGES = 5;
    
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    /**
     * Callback cho upload đơn ảnh
     */
    public interface SingleUploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
    
    /**
     * Callback cho upload nhiều ảnh
     */
    public interface MultipleUploadCallback {
        void onAllSuccess(List<String> imageUrls);
        void onProgress(int current, int total, String currentUrl);
        void onError(String error, int failedIndex);
    }
    
    // ==================== PROFILE AVATAR ====================
    
    /**
     * Upload ảnh đại diện (avatar)
     * @param context Context
     * @param imageUri Uri của ảnh
     * @param userId ID người dùng
     * @param callback Callback kết quả
     */
    public static void uploadAvatar(Context context, Uri imageUri, String userId, SingleUploadCallback callback) {
        String folder = FOLDER_AVATARS + "/" + userId;
        
        CloudinaryManager.getInstance().uploadImage(context, imageUri, folder, 
            new CloudinaryManager.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl, String publicId) {
                    mainHandler.post(() -> callback.onSuccess(imageUrl));
                }

                @Override
                public void onError(String errorMessage) {
                    mainHandler.post(() -> callback.onError(errorMessage));
                }

                @Override
                public void onProgress(int progress) {
                    // Optional: handle progress
                }
            });
    }
    
    // ==================== ROOM IMAGES ====================
    
    /**
     * Upload nhiều ảnh phòng trọ (tối đa 5 ảnh)
     * @param context Context
     * @param imageUris Danh sách Uri ảnh
     * @param roomId ID phòng (có thể null nếu tạo mới)
     * @param callback Callback kết quả
     */
    public static void uploadRoomImages(Context context, List<Uri> imageUris, String roomId, MultipleUploadCallback callback) {
        if (imageUris == null || imageUris.isEmpty()) {
            callback.onError("Không có ảnh để upload", -1);
            return;
        }
        
        // Giới hạn số ảnh
        int count = Math.min(imageUris.size(), MAX_ROOM_IMAGES);
        List<Uri> limitedUris = imageUris.subList(0, count);
        
        String folder = FOLDER_ROOMS + "/" + (roomId != null ? roomId : "temp_" + System.currentTimeMillis());
        
        uploadMultipleImages(context, limitedUris, folder, callback);
    }
    
    /**
     * Upload đơn ảnh phòng trọ
     */
    public static void uploadSingleRoomImage(Context context, Uri imageUri, String roomId, SingleUploadCallback callback) {
        String folder = FOLDER_ROOMS + "/" + (roomId != null ? roomId : "temp_" + System.currentTimeMillis());
        
        CloudinaryManager.getInstance().uploadImage(context, imageUri, folder,
            new CloudinaryManager.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl, String publicId) {
                    mainHandler.post(() -> callback.onSuccess(imageUrl));
                }

                @Override
                public void onError(String errorMessage) {
                    mainHandler.post(() -> callback.onError(errorMessage));
                }

                @Override
                public void onProgress(int progress) {}
            });
    }
    
    // ==================== CHAT IMAGES ====================
    
    /**
     * Upload ảnh tin nhắn
     * @param context Context
     * @param imageUri Uri của ảnh
     * @param chatId ID cuộc chat
     * @param callback Callback kết quả
     */
    public static void uploadChatImage(Context context, Uri imageUri, String chatId, SingleUploadCallback callback) {
        String folder = FOLDER_CHAT + "/" + chatId;
        
        CloudinaryManager.getInstance().uploadImage(context, imageUri, folder,
            new CloudinaryManager.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl, String publicId) {
                    mainHandler.post(() -> callback.onSuccess(imageUrl));
                }

                @Override
                public void onError(String errorMessage) {
                    mainHandler.post(() -> callback.onError(errorMessage));
                }

                @Override
                public void onProgress(int progress) {}
            });
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Upload nhiều ảnh (tuần tự)
     */
    private static void uploadMultipleImages(Context context, List<Uri> imageUris, String folder, MultipleUploadCallback callback) {
        List<String> uploadedUrls = new ArrayList<>();
        final int total = imageUris.size();
        
        uploadNextImage(context, imageUris, folder, 0, uploadedUrls, total, callback);
    }
    
    /**
     * Upload ảnh tiếp theo trong danh sách (đệ quy)
     */
    private static void uploadNextImage(Context context, List<Uri> imageUris, String folder, 
                                        int currentIndex, List<String> uploadedUrls, 
                                        int total, MultipleUploadCallback callback) {
        if (currentIndex >= imageUris.size()) {
            // Đã upload xong tất cả
            mainHandler.post(() -> callback.onAllSuccess(uploadedUrls));
            return;
        }
        
        Uri currentUri = imageUris.get(currentIndex);
        
        CloudinaryManager.getInstance().uploadImage(context, currentUri, folder,
            new CloudinaryManager.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl, String publicId) {
                    uploadedUrls.add(imageUrl);
                    
                    final int idx = currentIndex;
                    mainHandler.post(() -> callback.onProgress(idx + 1, total, imageUrl));
                    
                    // Upload ảnh tiếp theo
                    uploadNextImage(context, imageUris, folder, currentIndex + 1, uploadedUrls, total, callback);
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "Upload failed at index " + currentIndex + ": " + errorMessage);
                    mainHandler.post(() -> callback.onError(errorMessage, currentIndex));
                }

                @Override
                public void onProgress(int progress) {}
            });
    }
    
    /**
     * Lấy URL thumbnail
     */
    public static String getThumbnailUrl(String originalUrl, int size) {
        return CloudinaryManager.getThumbnailUrl(originalUrl, size, size);
    }
    
    /**
     * Lấy URL thumbnail với kích thước tùy chỉnh
     */
    public static String getThumbnailUrl(String originalUrl, int width, int height) {
        return CloudinaryManager.getThumbnailUrl(originalUrl, width, height);
    }
    
    /**
     * Kiểm tra số lượng ảnh hợp lệ cho phòng trọ
     */
    public static boolean isValidRoomImageCount(int count) {
        return count > 0 && count <= MAX_ROOM_IMAGES;
    }
    
    /**
     * Lấy số ảnh tối đa cho phòng trọ
     */
    public static int getMaxRoomImages() {
        return MAX_ROOM_IMAGES;
    }
}
