package com.example.asmt4;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Sketch extends AppCompatActivity {
    private MyDrawingArea drawingArea;

    private SQLiteDatabase mydb;

    private ListView sketchListView;
    private SketchListAdapter adapter;
    private final String API_KEY = "YOUR_VISION_API_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);
        drawingArea = findViewById(R.id.big_image_view);

        mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);
        databaseWork();

        // Find the ListView by its ID and set up the adapter
        sketchListView = findViewById(R.id.sketchListView);
        adapter = new SketchListAdapter(this, R.layout.list_item_no_checkbox, new ArrayList<>());
        sketchListView.setAdapter(adapter);

        // Load saved sketches on app startup
        showLatestImage();
    }

    void databaseWork(){
        mydb.execSQL("CREATE TABLE IF NOT EXISTS SKETCHES (SKETCH_ID INTEGER PRIMARY KEY AUTOINCREMENT, IMAGE BLOB, TAGS TEXT, TIME TEXT, DATE TEXT, CREATED TEXT)");

    }
    public void onClear(View view) {
        EditText tagsEditText = findViewById(R.id.userTags);
        tagsEditText.setText("");

        drawingArea.clear();
    }

    public void saveToDataBase(View view) {
        MyDrawingArea myDrawingArea = findViewById(R.id.big_image_view);
        Bitmap bmp = myDrawingArea.getBitmap();
        EditText tagsEditText = findViewById(R.id.userTags);
        String userTags = tagsEditText.getText().toString();

        String created = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String date = new SimpleDateFormat("MMM d, yyyy", Locale.US).format(new Date());
        String time = new SimpleDateFormat("ha", Locale.US).format(new Date());

        new Thread(() -> {
            String allTags = userTags;

            runOnUiThread(() -> {
                // Save the sketch and final tags to the database
                //performDatabaseSaveOperation(sketchBitmap, tagsToSave);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] ba = stream.toByteArray();

                ContentValues cv = new ContentValues();
                cv.put("IMAGE", ba);
                cv.put("TAGS", allTags);
                cv.put("TIME", time);
                cv.put("DATE", date);
                cv.put("CREATED", created);

                mydb.insert("SKETCHES", null, cv);
                showLatestImage();
                // Clear the drawing area and EditText
                myDrawingArea.clear();
                tagsEditText.setText("");
            });
        }).start();
    }


    public void find(View view) {
        EditText searchTagEditText = findViewById(R.id.getTag);
        String searchTag = searchTagEditText.getText().toString().trim();


            Cursor cursor = mydb.rawQuery("SELECT * FROM SKETCHES WHERE TAGS LIKE ? ORDER BY CREATED DESC", new String[]{"%" + searchTag + "%"});
        //}
        int imageIndex = cursor.getColumnIndex("IMAGE");
        int tagIndex = cursor.getColumnIndex("TAGS");
        int dateIndex = cursor.getColumnIndex("DATE");
        int timeIndex = cursor.getColumnIndex("TIME");
        ArrayList<SketchItem> sketchItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            // Retrieve data from the cursor
            byte[] imageBlob = cursor.getBlob(imageIndex);
            String tags = cursor.getString(tagIndex);
            String date = cursor.getString(dateIndex);
            String time = cursor.getString(timeIndex);

            // Convert the image blob to a Bitmap
            Bitmap image = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);

            // Create a FoodItem with sketch details
            SketchItem item = new SketchItem(image, tags, date + " - " + time);
            sketchItems.add(item);
            }
        cursor.close();


        adapter.clear();
        adapter.addAll(sketchItems);
        adapter.notifyDataSetChanged();
        }


    public void generateTags(View view) {
        EditText userTags = findViewById(R.id.userTags);
        Bitmap bitmap = drawingArea.getBitmap();

        new Thread(() -> {
            String visionTags = "";
            try {
                visionTags += myVisionTester(bitmap);
            } catch (IOException e) {
                Log.e("Vision", "Error fetching tags", e);
            }

            final String finalTags = visionTags;

            String finalVisionTags = visionTags;
            runOnUiThread(() -> {
                if (!TextUtils.isEmpty(finalTags)) {
                    if (TextUtils.isEmpty(userTags.getText())) {
                        userTags.setText(finalTags);
                    } else {
                        // Append generated tags to existing tags, separated by a comma
                        String existingTags = userTags.getText().toString();
                        userTags.setText(existingTags + ", " + finalTags);
                    }
                } else {
                    Log.i( "No tags generated", finalVisionTags);
                }
            });
        }).start();
    }

    // Method to load the most recent sketches from the database
    private void showLatestImage() {
        Cursor cursor = mydb.rawQuery("SELECT * FROM SKETCHES ORDER BY CREATED DESC", null);

        ArrayList<SketchItem> sketchItems = new ArrayList<>();
        try {
            int imageIndex = cursor.getColumnIndex("IMAGE");
            int tagIndex = cursor.getColumnIndex("TAGS");
            int dateIndex = cursor.getColumnIndex("DATE");
            int timeIndex = cursor.getColumnIndex("TIME");

            while (cursor.moveToNext()) {
                // Retrieve data from the cursor
                byte[] imageBlob = cursor.getBlob(imageIndex);
                String tags = cursor.getString(tagIndex);
                String date = cursor.getString(dateIndex);
                String time = cursor.getString(timeIndex);

                // Convert the image blob to a Bitmap
                Bitmap image = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);

                // Create a FoodItem with sketch details
                SketchItem item = new SketchItem(image, tags, date + " - " + time);
                sketchItems.add(item);
            }
            cursor.close();

            adapter.clear();
            adapter.addAll(sketchItems);
            adapter.notifyDataSetChanged();
        }catch(Exception e){
            Log.e("Show latest image error", e.toString());
        }
    }
    protected String myVisionTester(Bitmap bitmap) throws IOException
    {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bout);
        Image myimage = new Image();
        myimage.encodeContent(bout.toByteArray());

        //2. PREPARE AnnotateImageRequest
        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
        annotateImageRequest.setImage(myimage);
        Feature f = new Feature();
        f.setType("LABEL_DETECTION");
        f.setMaxResults(5);
        List<Feature> lf = new ArrayList<Feature>();
        lf.add(f);
        annotateImageRequest.setFeatures(lf);

        //3.BUILD the Vision
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(new VisionRequestInitializer(API_KEY));
        Vision vision = builder.build();

        //4. CALL Vision.Images.Annotate
        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
        List<AnnotateImageRequest> list = new ArrayList<AnnotateImageRequest>();
        list.add(annotateImageRequest);
        batchAnnotateImagesRequest.setRequests(list);
        Vision.Images.Annotate task = vision.images().annotate(batchAnnotateImagesRequest);
        BatchAnnotateImagesResponse response = task.execute();
        Log.v("MYTAG", response.toPrettyString());


        List<EntityAnnotation> annotations = response.getResponses().get(0).getLabelAnnotations();
        String tags = "";
        for(int i = 0; i<annotations.size(); i++){
            if(annotations.get(i).getScore() >= .85){
                String description = annotations.get(i).getDescription();
                if(tags.length()>0) {
                    tags += ", " + description;
                }else if(tags.length()==0){
                    tags += description;
                }

            }

        }if(tags.length()==0){
        tags += (annotations.get(0).getDescription());
    }

        return tags;


    }
    public void toMain(View view) {
        finish();
    }

}
