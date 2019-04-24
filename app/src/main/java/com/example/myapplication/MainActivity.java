package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    /** Constant to perform a read file request. */
    private static final int READ_REQUEST_CODE = 42;

    /** Default logging tag for messages from the main activity. */
    private static final String TAG = "ScanToTranslate";

    /** Request code for taking photo. */
    private static final int CAMERA_REQUEST_CODE = 233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageButton openFile = findViewById(R.id.gallery);
        openFile.setOnClickListener(v -> {
            Log.d(TAG, "Open file button clicked");
            startOpenFile();
        });
        final ImageButton takePhoto = findViewById(R.id.camara);
        takePhoto.setOnClickListener(v -> {
            Log.d(TAG, "Take photo button clicked");
            startTakePhoto();
        });
    }


    /** Take a photo using the camera. */
    private void startTakePhoto() {

        // Set up an intent to launch the camera app and have it take a photo for us
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            // Alert the user if there was a problem taking the photo
            Toast.makeText(getApplicationContext(), "Problem taking photo",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "Problem taking photo");
            return;
        }
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
    }

    /**
     * Start an open file dialog to look for image files.
     */
    private void startOpenFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
}
