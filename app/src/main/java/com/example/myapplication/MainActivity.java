package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.URI;
import java.net.HttpURLConnection;
import org.json.JSONObject;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /** Constant to perform a read file request. */
    private static final int READ_REQUEST_CODE = 42;

    /** Default logging tag for messages from the main activity. */
    private static final String TAG = "ScanToTranslate";

    /** Request code for taking photo. */
    private static final int CAMERA_REQUEST_CODE = 233;

    /** Request code for write to storage. */
    private static final int WRITE_TO_STORAGE_REQUEST = 321;

    /** Whether we can write to public storage. */
    private boolean canWriteToPublicStorage = false;

    /** Input imageView. */
    private ImageView inputImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputImage = findViewById(R.id.input);
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
        /*
         * Here we check for permission to write to external storage and request it if necessary.
         * Normally you would not want to do this on ever start, but we want to be persistent
         * since it makes development a lot easier.
         */
        canWriteToPublicStorage = (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        Log.d(TAG, "Do we have permission to write to external storage: "
                + canWriteToPublicStorage);
        if (!canWriteToPublicStorage) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_TO_STORAGE_REQUEST);
        }
    }


    File photoFile = null;

    /** Take a photo using the camera. */
    private void startTakePhoto() {

        // Set up an intent to launch the camera app and have it take a photo for us
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String imageFileName = "STT_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
        File storageDir;
        if (canWriteToPublicStorage) {
            storageDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        } else {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        try {
            photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.w(TAG, "Problem saving file: " + e);
            return;
        }
        if (takePicture.resolveActivity(getPackageManager()) == null) {
            // Alert the user if there was a problem taking the photo
            Toast.makeText(getApplicationContext(), "Problem taking photo",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "Problem taking photo");
            return;
        }
        Uri photoUri = FileProvider.getUriForFile(MainActivity.this, "com.scantotranslate.fileprovider", photoFile);
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
    }


    /**
     * Get a new file location for saving.
     *
     * @return the path to the new file or null of the create failed
     */
    /*File getSaveFilename() {
        String imageFileName = "STT_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
        File storageDir;
        if (canWriteToPublicStorage) {
            storageDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        } else {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.w(TAG, "Problem saving file: " + e);
            return null;
        }
    }*/

    /**
     * Start an open file dialog to look for image files.
     */
    private void startOpenFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "Activity with code" + requestCode + "failed.");
            return;
        }
        Uri photoUri;
        if (requestCode == CAMERA_REQUEST_CODE) {
            Picasso.get().load(photoFile).fit().into(inputImage);
            if (canWriteToPublicStorage) {
               //addPhotoToGallery()
            }
        } else if (requestCode == READ_REQUEST_CODE) {
            Picasso.get().load(data.getData()).fit().into(inputImage);
        }
    }
}
