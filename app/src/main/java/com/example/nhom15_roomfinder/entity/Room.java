package com.example.nhom15_roomfinder.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Room Entity - Đại diện cho thông tin phòng trọ
 */
public class Room implements Serializable {
    private String id;
    private String title;
    private String description;
    private double price;
    private String priceDisplay;     // Hiển thị giá (vd: "3.500.000 VNĐ/tháng")
    private String address;
    private String district;
    private String city;
    private double area;             // Diện tích (m2)
    private List<String> imageUrls;  // Danh sách URL hình ảnh
    private String thumbnailUrl;     // Hình đại diện
    
    // Tiện ích
    private boolean hasWifi;
    private boolean hasAC;           // Máy lạnh
    private boolean hasParking;      // Chỗ để xe
    private boolean hasPrivateBathroom;
    private boolean hasKitchen;
    private boolean hasSecurity;     // An ninh
    
    // Thông tin chủ trọ
    private String ownerId;
    private String ownerName;
    private String ownerPhone;
    private String ownerAvatar;
    
    // Trạng thái
    private boolean isAvailable;     // Còn trống
    private boolean isFavorite;      // Yêu thích (cho user hiện tại)
    private int viewCount;           // Lượt xem
    private long createdAt;
    private long updatedAt;

    // Constructor mặc định (cần cho Firebase)
    public Room() {
        this.imageUrls = new ArrayList<>();
        this.isAvailable = true;
    }

    public Room(String id, String title, double price, String address) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.address = address;
        this.imageUrls = new ArrayList<>();
        this.isAvailable = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor đầy đủ
    public Room(String id, String title, String description, double price, 
                String address, String district, String city, double area) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.address = address;
        this.district = district;
        this.city = city;
        this.area = area;
        this.imageUrls = new ArrayList<>();
        this.isAvailable = true;
        this.createdAt = System.currentTimeMillis();
        this.updatePriceDisplay();
    }

    // Helper method để format giá
    public void updatePriceDisplay() {
        this.priceDisplay = String.format("%,.0f VNĐ/tháng", price);
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (district != null) sb.append(", ").append(district);
        if (city != null) sb.append(", ").append(city);
        return sb.toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { 
        this.price = price; 
        this.updatePriceDisplay();
    }

    public String getPriceDisplay() { 
        if (priceDisplay == null || priceDisplay.isEmpty()) {
            return String.format("%,.0f VNĐ/tháng", price);
        }
        return priceDisplay; 
    }
    public void setPriceDisplay(String priceDisplay) { this.priceDisplay = priceDisplay; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public boolean isHasWifi() { return hasWifi; }
    public void setHasWifi(boolean hasWifi) { this.hasWifi = hasWifi; }

    public boolean isHasAC() { return hasAC; }
    public void setHasAC(boolean hasAC) { this.hasAC = hasAC; }

    public boolean isHasParking() { return hasParking; }
    public void setHasParking(boolean hasParking) { this.hasParking = hasParking; }

    public boolean isHasPrivateBathroom() { return hasPrivateBathroom; }
    public void setHasPrivateBathroom(boolean hasPrivateBathroom) { this.hasPrivateBathroom = hasPrivateBathroom; }

    public boolean isHasKitchen() { return hasKitchen; }
    public void setHasKitchen(boolean hasKitchen) { this.hasKitchen = hasKitchen; }

    public boolean isHasSecurity() { return hasSecurity; }
    public void setHasSecurity(boolean hasSecurity) { this.hasSecurity = hasSecurity; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getOwnerAvatar() { return ownerAvatar; }
    public void setOwnerAvatar(String ownerAvatar) { this.ownerAvatar = ownerAvatar; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}