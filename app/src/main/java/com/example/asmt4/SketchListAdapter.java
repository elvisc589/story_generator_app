package com.example.asmt4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SketchListAdapter extends ArrayAdapter<SketchItem> {
    // Constructor
    SketchListAdapter(Context context, int resource, ArrayList<SketchItem> objects) {
        super(context, resource, objects);
    }

    // getView method to customize the appearance of each list item
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate a new view if an existing view is not reused
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_no_checkbox, parent, false);
        }

        // Get the SketchItem for the current position
        SketchItem currentItem = getItem(position);

        // Find the views within the list item layout
        ImageView imageView = convertView.findViewById(R.id.Image); // Change ID as needed
        TextView tagsView = convertView.findViewById(R.id.Name); // Change ID as needed
        TextView dateView = convertView.findViewById(R.id.Date); // Or any other metadata you might have

        // Set the image and text on the views from the currentItem properties
        imageView.setImageBitmap(currentItem.getSketchImage()); // Assuming you have a getSketchImage method
        tagsView.setText(currentItem.getTags()); // Assuming you have a getTags method
        dateView.setText(currentItem.getDate()); // Assuming you have a getDate method

        return convertView;
    }
}
