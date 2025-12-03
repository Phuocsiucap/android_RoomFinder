package com.example.nhom15_roomfinder.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Listing implements Parcelable {
    private String id;
    private String title;
    private String description;
    private String price;
    private String address;
    private List<String> amenities;
    private List<String> imageUrls;
    private long createdAt;
    private int viewCount;
    private boolean isActive;
    private String userId;

    public Listing() {
        this.amenities = new ArrayList<>();
        this.imageUrls = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // Constructor mới để tạo mock data
    public Listing(String id, String title, String description, String price,
                   String address, long createdAt, int viewCount, boolean isActive) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.address = address;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
        this.isActive = isActive;
    }

    protected Listing(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        price = in.readString();
        address = in.readString();
        amenities = in.createStringArrayList();
        imageUrls = in.createStringArrayList();
        createdAt = in.readLong();
        viewCount = in.readInt();
        isActive = in.readByte() != 0;
        userId = in.readString();
    }

    public static final Creator<Listing> CREATOR = new Creator<Listing>() {
        @Override
        public Listing createFromParcel(Parcel in) {
            return new Listing(in);
        }

        @Override
        public Listing[] newArray(int size) {
            return new Listing[size];
        }
    };

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // Helper methods
    public String getFormattedPrice() {
        return price + " đ";
    }

    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - createdAt;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = months / 12;

        if (years > 0) return years + " năm trước";
        if (months > 0) return months + " tháng trước";
        if (days > 0) return days + " ngày trước";
        if (hours > 0) return hours + " giờ trước";
        if (minutes > 0) return minutes + " phút trước";
        return "Vừa xong";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(price);
        dest.writeString(address);
        dest.writeStringList(amenities);
        dest.writeStringList(imageUrls);
        dest.writeLong(createdAt);
        dest.writeInt(viewCount);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeString(userId);
    }
}