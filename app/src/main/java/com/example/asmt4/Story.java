package com.example.asmt4;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Story extends AppCompatActivity {
    String url = "https://api.textcortex.com/v1/texts/social-media-posts";
    private ImageListAdapter adapter;
    private SQLiteDatabase mydb;
    private TextView selectedTagsTextView;
    private ListView photoListView;
    private TextToSpeech tts;
    private final String API_KEY = "YOUR_TEXTCORTEX_API_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);

        // Find the ListView by its ID and set up the adapter
        photoListView = findViewById(R.id.photoListView);
        adapter = new ImageListAdapter(this, R.layout.list_item, new ArrayList<>(), findViewById(R.id.selectedTags), findViewById(R.id.photoListView));
        photoListView.setAdapter(adapter);

        selectedTagsTextView = findViewById(R.id.selectedTags);

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR){
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.75f);
            }
        });
        CheckBox myCheckbox = findViewById(R.id.sketchCheckbox);
        myCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

    }

    private boolean isSketchCheckboxChecked() {
        CheckBox myCheckbox = findViewById(R.id.sketchCheckbox);
        return myCheckbox.isChecked();
    }


    public void findTaggedImages(View view){
        adapter.clearAllCheckboxes();

        EditText userTags = findViewById(R.id.userTags);
        String searchTags = userTags.getText().toString().trim();

        ArrayList<Bitmap> matchingImages = new ArrayList<>();

        // Search in the photos database
        Cursor photoCursor = mydb.rawQuery("SELECT * FROM PHOTOS WHERE TAGS LIKE ? ORDER BY DATE DESC, CREATED DESC", new String[]{"%" + searchTags + "%"});

        // Convert the cursor into a list of PhotoItems
        ArrayList<PhotoItem> photoItems = new ArrayList<>();
        int imageIndex = photoCursor.getColumnIndex("IMAGE");
        int tagsIndex = photoCursor.getColumnIndex("TAGS");
        int dateIndex = photoCursor.getColumnIndex("DATE");
        int timeIndex = photoCursor.getColumnIndex("TIME");
        while(photoCursor.moveToNext()) {


            byte[] ba = photoCursor.getBlob(imageIndex);
            Bitmap bitmap = BitmapFactory.decodeByteArray(ba, 0, ba.length);
            String tags = photoCursor.getString(tagsIndex);
            String date = photoCursor.getString(dateIndex);
            String time = photoCursor.getString(timeIndex);
            photoItems.add(new PhotoItem(bitmap, tags, date + " - " + time));

        }
        photoCursor.close();

        if (isSketchCheckboxChecked()) {


            // Search in the sketches database
            Cursor sketchCursor = mydb.rawQuery("SELECT * FROM SKETCHES WHERE TAGS LIKE ? ORDER BY DATE DESC, CREATED DESC", new String[]{"%" + searchTags + "%"});
            //}
            int imageIndex2 = sketchCursor.getColumnIndex("IMAGE");
            int tagIndex2 = sketchCursor.getColumnIndex("TAGS");
            int dateIndex2 = sketchCursor.getColumnIndex("DATE");
            int timeIndex2 = sketchCursor.getColumnIndex("TIME");

            while (sketchCursor.moveToNext()) {
                // Retrieve data from the cursor
                byte[] imageBlob = sketchCursor.getBlob(imageIndex2);
                String tags = sketchCursor.getString(tagIndex2);
                String date = sketchCursor.getString(dateIndex2);
                String time = sketchCursor.getString(timeIndex2);

                // Convert the image blob to a Bitmap
                Bitmap image = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);

                // Create a FoodItem with sketch details
                PhotoItem item = new PhotoItem(image, tags, date + " - " + time);
                photoItems.add(item);
            }
            sketchCursor.close();
        }

        adapter.clear();
        adapter.addAll(photoItems);
        adapter.notifyDataSetChanged();

    }

    public void onSubmit(View view) throws JSONException {
        ArrayList<String> checkedTags = adapter.getCheckedTags();
        makeHttpRequest(findViewById(android.R.id.content), checkedTags);
    }

    public void makeHttpRequest(View view, ArrayList<String> checkedTags) throws JSONException{
        //EditText contextID = findViewById(R.id.context);
        //EditText keywordsID = findViewById(R.id.keywords);
        TextView story = findViewById(R.id.story);
        for (String tag : checkedTags){
            Log.i("checked tag", tag);
        }
        //String context = contextID.getText().toString();
        //String keywords = keywordsID.getText().toString();


        JSONObject data = new JSONObject();
        data.put("context", "story");
        data.put("max_tokens", 100);
        data.put("mode", "twitter");
        data.put("model", "chat-sophos-1");
        String[] keywordArray = {"cars", "bmw"};
        //String[] keywordArray = keywords.split("\\s*,\\s*");
        JSONArray jsonArray = new JSONArray();
        for (String tag : checkedTags) {
            jsonArray.put(tag);
        }
        data.put("keywords", jsonArray);
        //data.put("keywords", new JSONArray(Arrays.asList(checkedTags)));


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i("Success ", response.toString());
                    JSONArray outputs = response.getJSONObject("data").getJSONArray("outputs");

                    String storyText = outputs.getJSONObject(0).getString("text");
                    story.setText(storyText);
                    tts.speak(story.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);

                } catch (JSONException e) {
                    Log.e("error", "JSON parsing error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", new String(error.networkResponse.data));

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + API_KEY);
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    public void toMain(View view) {
        finish();
    }
}