package com.example.asmt4;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ImageListAdapter extends ArrayAdapter<PhotoItem> {
    private ArrayList<String> checkedTags = new ArrayList<>(); // List to store checked tags
    private TextView selectedTagsTextView;
    private ListView listView;
    private HashMap<String, Integer> tagCountMap = new HashMap<>(); // HashMap to store tag counts
    private TextToSpeech tts;
    private static final int MAX_CHECKBOXES = 3;
    private int checkedCount = 0;


    ImageListAdapter(Context context, int resource, ArrayList<PhotoItem> objects, TextView selectedTagsTextView, ListView listView) {
        super(context, resource, objects);
        this.selectedTagsTextView = selectedTagsTextView;
        this.listView = listView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        PhotoItem currentItem = getItem(position);
        ImageView imageView = convertView.findViewById(R.id.Image);
        TextView tagsView = convertView.findViewById(R.id.Name);
        TextView dateView = convertView.findViewById(R.id.Date);
        CheckBox checkBox = convertView.findViewById(R.id.checkbox);

        dateView.setText(currentItem.getDate());
        imageView.setImageBitmap(currentItem.getImage());
        tagsView.setText(currentItem.getTags());

        checkBox.setTag(position);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked && checkedCount >= MAX_CHECKBOXES) {
                    // If trying to check more than the allowed checkboxes, prevent checking
                    checkBox.setChecked(false);
                    return;
                }

                int pos = (int) compoundButton.getTag(); // Get the position from the tag
                PhotoItem item = getItem(pos); // Get the matching PhotoItem
                String[] tagsArray = item.getTags().split(", "); // Split tags into individual strings

                for (String tag : tagsArray){
                    Log.i("tag", tag);
                }



                if (isChecked) {
                    for (String tag : tagsArray) {
                        if(checkedTags.contains(tag)){
                            int count = tagCountMap.get(tag);
                            tagCountMap.put(tag, count + 1);
                        }
                        else if (!checkedTags.contains(tag)) {
                            tagCountMap.put(tag, 1);
                            checkedTags.add(tag); // Add tag if not already added
                        }
                    }
                } else {
                    for (String tag : tagsArray) {
                        if (tagCountMap.containsKey(tag)) {
                            int count = tagCountMap.get(tag);
                            if (count > 1) {
                                tagCountMap.put(tag, count - 1); // Decrement count
                            } else {
                                tagCountMap.remove(tag); // Remove the tag from the HashMap
                                checkedTags.remove(tag); // Remove the tag from checkedTags
                            }
                        }
                    }
                }
                if (isChecked){
                    checkedCount += 1;
                }
                else{
                    checkedCount -= 1;
                }
                updateSelectedTagsTextView();
            }
        });

        return convertView;
    }

    private void updateSelectedTagsTextView() {
        // Update the TextView with the selected tags
        selectedTagsTextView.setText(TextUtils.join(", ", checkedTags));
    }

    public ArrayList<String> getCheckedTags() {
        return checkedTags;
    }

    public void clearAllCheckboxes() {
        checkedTags.clear();
        tagCountMap.clear();
        checkedCount = 0;
        notifyDataSetChanged();
    }

}
