package com.example.nhom15_roomfinder.entity;

import java.io.Serializable;

public class Room implements Serializable {
    private String id;
    private String title;
    private String price;
    private String location;
    private String distance;
    private String imageUrl;
    private boolean isFavorite;

    public Room(String id, String title, String price, String location,
                String distance, String imageUrl, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.location = location;
        this.distance = distance;
        this.imageUrl = imageUrl;
        this.isFavorite = isFavorite;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}