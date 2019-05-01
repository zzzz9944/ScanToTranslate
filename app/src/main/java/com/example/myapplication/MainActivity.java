package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;

import com.example.myapplication.helper.ImageHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.*;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

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

    /** TextView for display result.*/
    private TextView text;

    /** Uri of the image file. */
    private Uri photoUri;

    /** Microsoft Vision service client. */
    private VisionServiceClient client;

    /** bitmap of the image file to display. */
    private Bitmap photoBitmap;

    /** String to save the user selected source language to use in request url. */
    private String languageHintCode;

    /** String to save the user selected output language to use in request url. */
    private String outputLanguageCode = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestQueue = Volley.newRequestQueue(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (client == null) {
            client = new VisionServiceRestClient("48c6dc9d14f048eb945c8ad728c5911f", "http://westus.api.cognitive.microsoft.com/vision/v2.0");
        }
        inputImage = findViewById(R.id.input);
        Spinner languageHint = findViewById(R.id.languangeHint);
        languageHint.setOnItemSelectedListener(this);
        Spinner outputLanguage = findViewById(R.id.outputlanguage);
        outputLanguage.setOnItemSelectedListener(this);
        final ImageButton openFile = findViewById(R.id.gallery);
        text = findViewById(R.id.output);
        openFile.setOnClickListener(v -> {
            Log.d(TAG, "Open file button clicked");
            startOpenFile();
        });
        final ImageButton takePhoto = findViewById(R.id.camara);
        takePhoto.setOnClickListener(v -> {
            Log.d(TAG, "Take photo button clicked");
            doRecognize();
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

    /**

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
            Toast.makeText(getApplicationContext(), "Problem taking photo",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "Problem taking photo");
            return;
        }
        photoUri = FileProvider.getUriForFile(MainActivity.this, "com.scantotranslate.fileprovider", photoFile);
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
    }
    */



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
        /*if (requestCode == CAMERA_REQUEST_CODE) {
            Picasso.get().load(photoFile).fit().into(inputImage);
        } else if (requestCode == READ_REQUEST_CODE) {
            photoUri = data.getData();
            Picasso.get().load(photoUri).fit().into(inputImage);
        }*/
        if (requestCode == READ_REQUEST_CODE) {
            photoUri = data.getData();
        }
        if (photoUri != null) {
            Log.d("uri: ", photoUri.toString());
            photoBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(photoUri, getContentResolver());
            //Log.d("bitmap:", photoBitmap.toString());
            inputImage.setImageBitmap(photoBitmap);
            doRecognize();
        }
    }

    public void doRecognize() {
        text.setText(R.string.recognizing_prompt);

        try {
            new doRequest().execute();
        } catch (Exception e) {
            text.setText("Error encountered");
        }
    }

    private void setLanguageCode(int parentID, String languageCode) {
        if (parentID == R.id.languangeHint) {
            languageHintCode = languageCode;
        } else if (!languageCode.equals("unk")){
            outputLanguageCode = languageCode;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parentID = parent.getId();
        switch (position) {
            case 1:
                setLanguageCode(parentID, "en");
                break;
            case 2:
                setLanguageCode(parentID, "de");
                break;
            case 3:
                setLanguageCode(parentID, "fr");
                break;
            case 4:
                setLanguageCode(parentID, "zh-Hans");
                break;
            case 5:
                setLanguageCode(parentID, "ja");
                break;
            case 6:
                setLanguageCode(parentID, "es");
                break;
            case 7:
                setLanguageCode(parentID, "ko");
                break;
            default:
                setLanguageCode(parentID, "unk");
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                String result = process();
                result = toStrings(result);
                return Post(result);
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                text.setText(e.getMessage());
                this.e = null;
            } else {
                JsonParser parser = new JsonParser();
                Log.d("data: ", data);
                String display = parser.parse(data)
                        .getAsJsonArray()
                        .get(0)
                        .getAsJsonObject()
                        .get("translations")
                        .getAsJsonArray()
                        .get(0)
                        .getAsJsonObject()
                        .get("text")
                        .getAsString();
                text.setText(display);
            }
        }
    }
    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        OCR ocr;
        ocr = this.client.recognizeText(inputStream, languageHintCode, true);

        String result = gson.toJson(ocr);
        Log.d("result ", result);
        return result;
    }

    String url2 = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&from=en&to=zh-Hans";
    String subscriptionKey = "f39799e99d0943d2aeaff6ff9de67164";

    // Instantiates the OkHttpClient.
    OkHttpClient client1 = new OkHttpClient();

    // This function performs a POST request.
    public String Post(String input) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        String url = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0";

        if (languageHintCode.equals("unk")) {
            url = url + "&to=" + outputLanguageCode;
        } else {
            url = url + "&from=" + languageHintCode + "&to=" + outputLanguageCode;
        }
        Log.d("request url: ", url);
        String inputobj = buildJsonArray(input);
        Log.d("input obj: ", inputobj);
        RequestBody body = RequestBody.create(mediaType, inputobj);
        Request request = new Request.Builder()
                .url(url).post(body)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .addHeader("Content-type", "application/json").build();
        Response response = client1.newCall(request).execute();
        return response.body().string();
    }

    private String toStrings(String data) {
        Gson gson = new Gson();
        OCR r = gson.fromJson(data, OCR.class);
        String result = "";
        for (Region reg : r.regions) {
            for (Line line : reg.lines) {
                for (Word word : line.words) {
                    result += word.text + " ";
                }
                result += "\n";
            }
            result += "\n\n";
        }
        return result;
    }

    protected String buildJsonArray(String values) {
        StringBuilder toReturn = new StringBuilder("[");
        toReturn.append("\n\t{\"Text\": ");
        toReturn.append("\"" + values + "\"}");
        toReturn.append("\n]");
        return toReturn.toString();
    }
}

