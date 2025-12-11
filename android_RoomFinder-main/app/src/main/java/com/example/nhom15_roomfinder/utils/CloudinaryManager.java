package com.example.nhom15_roomfinder.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

/**
 * CloudinaryManager - Quản lý upload ảnh lên Cloudinary
 * 
 * Thông tin tài khoản:
 * - Cloud Name: dawxdwbjk
 * - API Key: 583579923368498
 * - API Secret: kbkcq1G4JigsxcEc1i_Ifg-Omhk
 */
public class CloudinaryManager {
    
    private static final String TAG = "CloudinaryManager";
    
    // Cloudinary credentials
    private static final String CLOUD_NAME = "dawxdwbjk";
    private static final String API_KEY = "583579923368498";
    private static final String API_SECRET = "kbkcq1G4JigsxcEc1i_Ifg-Omhk";
    
    // Upload URL
    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
    
    // Upload preset (unsigned upload) - cần tạo trên Cloudinary dashboard
    private static final String UPLOAD_PRESET = "roomfinder_unsigned";
    
    private static CloudinaryManager instance;
    private OkHttpClient client;
    
    private CloudinaryManager() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }
    
    public static synchronized CloudinaryManager getInstance() {
        if (instance == null) {
            instance = new CloudinaryManager();
        }
        return instance;
    }
    
    /**
     * Interface callback khi upload hoàn thành
     */
    public interface UploadCallback {
        void onSuccess(String imageUrl, String publicId);
        void onError(String errorMessage);
        void onProgress(int progress);
    }
    
    /**
     * Upload ảnh từ Uri lên Cloudinary
     * @param context Context
     * @param imageUri Uri của ảnh cần upload
     * @param folder Thư mục trên Cloudinary (vd: "avatars", "rooms", "chat")
     * @param callback Callback khi upload hoàn thành
     */
    public void uploadImage(Context context, Uri imageUri, String folder, UploadCallback callback) {
        // Sử dụng signed upload thay vì unsigned
        uploadImageSigned(context, imageUri, folder, callback);
    }
    
    /**
     * Upload ảnh với unsigned upload (cần tạo upload preset trên Cloudinary dashboard)
     */
    public void uploadImageUnsigned(Context context, Uri imageUri, String folder, UploadCallback callback) {
        try {
            // Đọc ảnh thành byte array
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onError("Không thể đọc file ảnh");
                return;
            }
            
            byte[] imageBytes = getBytes(inputStream);
            inputStream.close();
            
            // Convert to Base64
            String base64Image = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.DEFAULT);
            
            // Tạo request body
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", base64Image)
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("folder", folder)
                    .addFormDataPart("api_key", API_KEY)
                    .build();
            
            // Tạo request
            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build();
            
            // Thực hiện request async
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Upload failed: " + e.getMessage());
                    callback.onError("Lỗi kết nối: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject json = new JSONObject(responseBody);
                            
                            String secureUrl = json.getString("secure_url");
                            String publicId = json.getString("public_id");
                            
                            Log.d(TAG, "Upload success: " + secureUrl);
                            callback.onSuccess(secureUrl, publicId);
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Parse response error: " + e.getMessage());
                            callback.onError("Lỗi xử lý phản hồi: " + e.getMessage());
                        }
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        Log.e(TAG, "Upload failed: " + errorBody);
                        callback.onError("Upload thất bại: " + response.code());
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Upload error: " + e.getMessage());
            callback.onError("Lỗi: " + e.getMessage());
        }
    }
    
    /**
     * Upload ảnh với signed upload (bảo mật hơn)
     */
    public void uploadImageSigned(Context context, Uri imageUri, String folder, UploadCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onError("Không thể đọc file ảnh");
                return;
            }
            
            byte[] imageBytes = getBytes(inputStream);
            inputStream.close();
            
            String base64Image = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            
            // Tạo timestamp và signature
            // Signature phải được tạo từ các params theo thứ tự alphabet
            long timestamp = System.currentTimeMillis() / 1000;
            String toSign = "folder=" + folder + "&timestamp=" + timestamp + API_SECRET;
            String signature = sha1(toSign);
            
            Log.d(TAG, "Upload to folder: " + folder);
            Log.d(TAG, "Timestamp: " + timestamp);
            Log.d(TAG, "Signature string: folder=" + folder + "&timestamp=" + timestamp);
            
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", base64Image)
                    .addFormDataPart("folder", folder)
                    .addFormDataPart("timestamp", String.valueOf(timestamp))
                    .addFormDataPart("api_key", API_KEY)
                    .addFormDataPart("signature", signature)
                    .build();
            
            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Upload network error: " + e.getMessage());
                    callback.onError("Lỗi kết nối: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Upload response code: " + response.code());
                    Log.d(TAG, "Upload response body: " + responseBody);
                    
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String secureUrl = json.getString("secure_url");
                            String publicId = json.getString("public_id");
                            Log.d(TAG, "Upload success: " + secureUrl);
                            callback.onSuccess(secureUrl, publicId);
                        } catch (Exception e) {
                            Log.e(TAG, "Parse error: " + e.getMessage());
                            callback.onError("Lỗi xử lý phản hồi");
                        }
                    } else {
                        Log.e(TAG, "Upload failed: " + responseBody);
                        callback.onError("Upload thất bại: " + response.code() + " - " + responseBody);
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Upload exception: " + e.getMessage());
            callback.onError("Lỗi: " + e.getMessage());
        }
    }
    
    /**
     * Lấy URL thumbnail của ảnh
     * @param originalUrl URL gốc từ Cloudinary
     * @param width Chiều rộng mong muốn
     * @param height Chiều cao mong muốn
     * @return URL thumbnail
     */
    public static String getThumbnailUrl(String originalUrl, int width, int height) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }
        
        // Cloudinary transformation URL
        // Original: https://res.cloudinary.com/dawxdwbjk/image/upload/v123/folder/image.jpg
        // Thumbnail: https://res.cloudinary.com/dawxdwbjk/image/upload/w_200,h_200,c_fill/v123/folder/image.jpg
        
        String transformation = "w_" + width + ",h_" + height + ",c_fill";
        return originalUrl.replace("/upload/", "/upload/" + transformation + "/");
    }
    
    /**
     * Lấy URL ảnh với chất lượng tùy chỉnh
     */
    public static String getOptimizedUrl(String originalUrl, int quality) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }
        String transformation = "q_" + quality;
        return originalUrl.replace("/upload/", "/upload/" + transformation + "/");
    }
    
    /**
     * Convert InputStream to byte array
     */
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
    
    /**
     * SHA1 hash for signature
     */
    private String sha1(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] result = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
