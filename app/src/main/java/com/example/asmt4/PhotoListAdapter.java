package com.example.asmt4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class PhotoListAdapter extends ArrayAdapter<PhotoItem> {
    PhotoListAdapter(Context context, int resource, ArrayList<PhotoItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_no_checkbox, parent, false);
        }
        PhotoItem currentItem = getItem(position);
        ImageView imageView = convertView.findViewById(R.id.Image); // Change ID as needed
        TextView tagsView = convertView.findViewById(R.id.Name); // Change ID as needed
        TextView dateView = convertView.findViewById(R.id.Date);

        dateView.setText(currentItem.getDate());
        imageView.setImageBitmap(currentItem.getImage());
        tagsView.setText(currentItem.getTags());

        return convertView;
    }


}

