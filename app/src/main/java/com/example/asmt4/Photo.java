package com.example.asmt4;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Photo extends AppCompatActivity {
    ImageView bigImageView;
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private ListView photoListView;
    private PhotoListAdapter adapter;
    private SQLiteDatabase mydb;

    private final String API_KEY = "YOUR_VISION_API_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        bigImageView = findViewById(R.id.big_image_view);
        mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);
        databaseWork();

        // Find the ListView by its ID and set up the adapter
        photoListView = findViewById(R.id.photoListView);
        adapter = new PhotoListAdapter(this, R.layout.list_item_no_checkbox, new ArrayList<>());
        photoListView.setAdapter(adapter);

        // Load saved images on app startup
        showLatestImage();
    }


    void databaseWork(){
        mydb.execSQL("CREATE TABLE IF NOT EXISTS PHOTOS (PHOTOS_ID INTEGER PRIMARY KEY AUTOINCREMENT, IMAGE BLOB, TAGS TEXT, TIME TEXT, DATE TEXT, CREATED TEXT)");

    }

    public void generateTags(View view) {
        EditText userTags = findViewById(R.id.userTags);
        Bitmap bitmap = ((BitmapDrawable) bigImageView.getDrawable()).getBitmap();

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

    public void saveToDataBase(View view) {
        String created = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String date = new SimpleDateFormat("MMM d, yyyy", Locale.US).format(new Date());
        String time = new SimpleDateFormat("ha", Locale.US).format(new Date());



        Bitmap bmp = ((BitmapDrawable) bigImageView.getDrawable()).getBitmap();


        EditText tagsEditText = findViewById(R.id.userTags);
        String userTags = tagsEditText.getText().toString();

        new Thread(() -> {

            // Combine the manual tag with the API tag
            String allTags = userTags;

            runOnUiThread(() -> {
                // Save the image and final tags to the database
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] ba = stream.toByteArray();

                ContentValues cv = new ContentValues();
                cv.put("IMAGE", ba);
                cv.put("TAGS", allTags);
                cv.put("TIME", time);
                cv.put("DATE", date);
                cv.put("CREATED", created);

                mydb.insert("PHOTOS", null, cv);
                showLatestImage();

                // Clear the ImageView and EditText

                tagsEditText.setText("");
            });
        }).start();
        showLatestImage();
    }


    // Method to load the most recent photos from the database
    private void showLatestImage() {
        Cursor cursor = mydb.rawQuery("SELECT * FROM PHOTOS ORDER BY CREATED DESC", null);

        ArrayList<PhotoItem> photoItems = new ArrayList<>();
        int imageIndex = cursor.getColumnIndex("IMAGE");
        int tagIndex = cursor.getColumnIndex("TAGS");
        int dateIndex = cursor.getColumnIndex("DATE");
        int timeIndex = cursor.getColumnIndex("TIME");

        while (cursor.moveToNext()) {
                byte[] ba = cursor.getBlob(imageIndex);
                Bitmap bitmap = BitmapFactory.decodeByteArray(ba, 0, ba.length);
                String tags = cursor.getString(tagIndex);
                String date = cursor.getString(dateIndex);
                String time = cursor.getString(timeIndex);

                photoItems.add(new PhotoItem(bitmap, tags, date + " - " + time));

        }
        cursor.close();

        // Update the adapter with the new list of photos
        adapter.clear();
        adapter.addAll(photoItems);
        adapter.notifyDataSetChanged();
    }

    public void find(View view) {
        EditText searchTagEditText = findViewById(R.id.getTag);
        String searchTag = searchTagEditText.getText().toString().trim();

        Cursor cursor = mydb.rawQuery("SELECT * FROM PHOTOS WHERE TAGS LIKE ? ORDER BY CREATED DESC", new String[]{"%" + searchTag + "%"});

        // Convert the cursor into a list of PhotoItems
        ArrayList<PhotoItem> photoItems = new ArrayList<>();
        int imageIndex = cursor.getColumnIndex("IMAGE");
        int tagsIndex = cursor.getColumnIndex("TAGS");
        int dateIndex = cursor.getColumnIndex("DATE");
        int timeIndex = cursor.getColumnIndex("TIME");
        while(cursor.moveToNext()) {


                byte[] ba = cursor.getBlob(imageIndex);
                Bitmap bitmap = BitmapFactory.decodeByteArray(ba, 0, ba.length);
                String tags = cursor.getString(tagsIndex);
                String date = cursor.getString(dateIndex);
                String time = cursor.getString(timeIndex);
                photoItems.add(new PhotoItem(bitmap, tags, date + " - " + time));

            }
            cursor.close();


        // Update the adapter with the search results
        adapter.clear();
        adapter.addAll(photoItems);
        adapter.notifyDataSetChanged();
    }

    public void startCamera(View view) {
        // Check for camera permission at runtime
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
            return;
        }
        // Launch the camera activity
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                bigImageView.setImageBitmap(imageBitmap);
            }
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