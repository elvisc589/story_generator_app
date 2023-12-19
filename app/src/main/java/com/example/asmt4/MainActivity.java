package com.example.asmt4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent serviceIntent = new Intent(MainActivity.this, MusicService.class);
                    startService(serviceIntent);
                }
            }).start();
        }


        public void toSketchTagger(View view) {
            Intent intent = new Intent(this, Sketch.class);
            startActivity(intent);
        }

        public void toPhotoTagger(View view) {
            Intent intent = new Intent(this, Photo.class);
            startActivity(intent);
        }

        public void toStory(View view) {
            Intent intent = new Intent(this, Story.class);
            startActivity(intent);
        }
}