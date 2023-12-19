package com.example.asmt4;

import android.graphics.Bitmap;

public class PhotoItem {
    private Bitmap image; // Use Bitmap for images
    private String tags;
    private String date;

    public PhotoItem(Bitmap image, String tags, String date) {
        this.image = image;
        this.tags = tags;
        this.date = date;
    }

    // Getters for image and tags
    public Bitmap getImage() {
        return image;
    }

    public String getTags() {
        return tags;
    }

    public String getDate() {
        return date;
    }
}

