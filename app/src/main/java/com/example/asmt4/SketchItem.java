package com.example.asmt4;

import android.graphics.Bitmap;

public class SketchItem {
    private Bitmap image; // Use Bitmap for images
    private String tags;

    private String date;

    public SketchItem(Bitmap image, String tags, String date) {
        this.image = image;
        this.tags = tags;
        this.date = date;
    }

    // Getters for image and tags
    public Bitmap getSketchImage() {
        return image;
    }

    public String getTags() {
        return tags;
    }


    public String getDate() {
        return date;
    }
}

