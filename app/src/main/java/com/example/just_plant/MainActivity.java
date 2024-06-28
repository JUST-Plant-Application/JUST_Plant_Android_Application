package com.example.just_plant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.android.volley.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CustomDialog.CloseDialogListener {

    private static final String SERVER_URL = "https://just-plant-server.vercel.app";
    private static final String SERVER_URL_test = "https://just-plant-server.vercel.app/UsageInfo";
    protected static final String SERVER_URL_identify = "https://just-plant-server.vercel.app/identify";
    private static final String SERVER_URL_health = "https://just-plant-server.vercel.app/health-assessment";
    protected static final String SERVER_URL_retrieve = "https://just-plant-server.vercel.app/retrieve-identification";
    private static final String SERVER_URL_IDENTIFY_CROP ="https://just-plant-server.vercel.app/crop-health-identify";
    private static final String SERVER_URL_RETRIEVE_CROP_HEALTH ="https://just-plant-server.vercel.app/crop-health-retrieve";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_DIALOG_SHOWN = "dialog_shown";


    TextView textView;
    Button DiagnoseButton;
    Button IdentifyButton;
    ImageView imageView,uploadScan;
    Uri imageUri;
    RequestQueue queue;
    Button CropHealth;
    LinearLayout ToHomeBtn,ToFeedBtn,ToGardenBtn,ToCategories,ScanBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ToGardenBtn=findViewById(R.id.MYGarden_Btn);
        ToFeedBtn=findViewById(R.id.Feed_Btn);
        ToHomeBtn=findViewById(R.id.home_Btn);
        ToCategories=findViewById(R.id.Diagnose_Btn);
        uploadScan=findViewById(R.id.upload_to_scan);
        ScanBtn=findViewById(R.id.Scan_Btn);

        imageView = findViewById(R.id.imageView);
        IdentifyButton = findViewById(R.id.IdentifyButton);
        DiagnoseButton = findViewById(R.id.HealthButton);
        CropHealth= findViewById(R.id.CropHealthButton);
        textView = findViewById(R.id.textView);
        // Check if the dialog has already been shown
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean dialogShown = preferences.getBoolean(KEY_DIALOG_SHOWN, false);

        if (!dialogShown) {
            // Show the dialog and set the flag
            CustomDialog dialog = new CustomDialog(this, this);
            dialog.showDialog();
        }

// Instantiate the RequestQueue
        queue = Volley.newRequestQueue(this);
// JsonObjectRequest for asynchronous JSON response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, SERVER_URL_test, null,
                response -> {
                    try {
                        boolean isActive = response.getBoolean("active");
                        int totalRemainingCredits = response.getJSONObject("remaining").getInt("total");
//                        String formattedResponse = "Connection successful!\n" +
//                                "Value: " + isActive + "\n" +
//                                "Remaining Credits: " + totalRemainingCredits;
                        String formattedResponse = "Connection successful!\n" +
                                "Value: " + isActive + "\n" ;
                        textView.setText(formattedResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        textView.setText("Error parsing JSON response");
                    }
                },
                error -> {
                    textView.setText("Error: " + error.getMessage());
                }
        );

// Add the request to the queue
        queue.add(jsonObjectRequest);

        ////////////////TESTING

        uploadScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUploadOptionsDialog();
                // checkPermissions();
            }
        });

//
//        home.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, HomePage.class);
//                startActivity(intent);
//            }
//        });

        IdentifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    //  new IdentifyPlantTask().execute(imageUri);
                    identifyPlant(imageUri);
                } else {
                    Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });


        CropHealth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    //  new IdentifyPlantTask().execute(imageUri);
                    CropHealthIdentify(imageUri);
                } else {
                    Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });


        DiagnoseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    //new HealthAssessmentTask().execute(imageUri);
                    health_assessment(imageUri);
                } else {
                    Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });

/////////////////////////////////////navigation bar

        ToGardenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Garden_page.class);
                startActivity(intent);
                finish();
            }
        });

        ToCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Categories_page.class);
                startActivity(intent);
                finish();
            }
        });

        ToHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        ToFeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Feed_page.class);
                startActivity(intent);
                finish();
            }
        });

        ///////////////////////////////////////


//
//        Categories.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(imageUri != null){
//                    Intent intent = new Intent(MainActivity.this, Categories.class);
//                    startActivity(intent);}
//                else{
//                    Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


    } //end OnCreate()


    @Override
    public void onClose() {
        // Set the flag in SharedPreferences when the dialog is closed
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_DIALOG_SHOWN, true);
        editor.apply();
    }
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            }, REQUEST_STORAGE_PERMISSION);
        }
    }

    private void showUploadOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Options:");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            openCamera();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        }
                        break;
                    case 1:
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                            openGallery();
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this, "com.example.just_plant.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_STORAGE_PERMISSION);
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_STORAGE_PERMISSION) && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
            }
            imageView.setImageURI(imageUri);
        }
    }

    private Bitmap handleImageOrientation(Bitmap bitmap, Uri imageUri) throws IOException {
        ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(imageUri));
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationAngle = 0;

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationAngle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationAngle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationAngle = 270;
                break;
            default:
                break;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Method to enhance image quality
    private Bitmap enhanceImageQuality(Bitmap bitmap) {
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 1200); // Use a larger size for better quality

        // Apply basic sharpening filter
        float[] sharpMatrix = {
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0
        };
        Bitmap sharpenedBitmap = applyConvolutionFilter(resizedBitmap, sharpMatrix);

        //return sharpenedBitmap;
        return resizedBitmap;
    }

    // Method to apply a convolution filter for image enhancement
    private Bitmap applyConvolutionFilter(Bitmap src, float[] kernel) {
        Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        RenderScript rs = RenderScript.create(this);
        Allocation allocIn = Allocation.createFromBitmap(rs, src);
        Allocation allocOut = Allocation.createFromBitmap(rs, output);
        ScriptIntrinsicConvolve3x3 convolution = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
        convolution.setInput(allocIn);
        convolution.setCoefficients(kernel);
        convolution.forEach(allocOut);
        allocOut.copyTo(output);
        rs.destroy();
        return output;
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void identifyPlant(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap enhancedBitmap = enhanceImageQuality(bitmap);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream); // Use a higher quality compression
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64ImageString = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            JSONObject jsonRequestBody = createJsonRequestBody(base64ImageString);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, SERVER_URL_identify, jsonRequestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String accessToken = response.getString("access_token");
                                if (accessToken == null || accessToken.isEmpty()) {
                                    Toast.makeText(MainActivity.this, "Invalid access token received", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Log.d("ACCESS TOKEN", "access_token: " + accessToken);

                                Intent intent = new Intent(MainActivity.this, DisplayIdentificationResult.class);
                                intent.putExtra("response", response.toString());
                                startActivity(intent);

                                Retrieve_Identification(accessToken);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                textView.setText("Error parsing JSON response");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("API Error", error.toString());
                    Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            queue.add(jsonObjectRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Retrieve_Identification(String accessToken) {
        String url = SERVER_URL_retrieve + "/" + accessToken;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API Error", error.toString());
                        Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }


    private void health_assessment(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = handleImageOrientation(bitmap, imageUri); // Ensure orientation is handled
            Bitmap enhancedBitmap = enhanceImageQuality(bitmap);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream); // Use a higher quality compression
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64ImageString = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            // Debugging logs
            Log.d("Image Data", "Base64 Image String: " + base64ImageString);
            Log.d("Image Data", "Image Byte Length: " + imageBytes.length);

            JSONObject jsonRequestBody = createJsonRequestBody(base64ImageString);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    SERVER_URL_health,
                    jsonRequestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("API Response", response.toString()); // Debug API response
                                if (response.has("result")) {
                                    JSONObject result = response.getJSONObject("result");
                                    if (result.has("disease")) {
                                        JSONObject disease = result.getJSONObject("disease");
                                        if (disease.has("suggestions")) {
                                            JSONArray suggestions = disease.getJSONArray("suggestions");

                                            if (suggestions.length() > 0) {
                                                JSONObject firstSuggestion = suggestions.getJSONObject(0);
                                                String firstSuggestionId = firstSuggestion.getString("id");
                                                Log.d("PlantID", "ID of the first disease suggestion: " + firstSuggestionId);

                                                Intent intent = new Intent(MainActivity.this, DisplayHealthAssResult.class);
                                                intent.putExtra("HealthResponse", response.toString());
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(MainActivity.this, "No disease suggestions found.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "Disease data not found in the response.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "Result does not contain disease information.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Result section not found in the response.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Error parsing JSON response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("API Error", error.toString());
                            Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(jsonObjectRequest);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//////////////////////////crop health

    private void CropHealthIdentify(Uri imageUri) {
        try {

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = handleImageOrientation(bitmap, imageUri); // Ensure orientation is handled
            Bitmap enhancedBitmap = enhanceImageQuality(bitmap);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream); // Use a higher quality compression
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64ImageString = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            JSONObject jsonRequestBody = createJsonRequestBody(base64ImageString);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, SERVER_URL_IDENTIFY_CROP, jsonRequestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String accessToken = response.getString("access_token");
                                if (accessToken == null || accessToken.isEmpty()) {
                                    Toast.makeText(MainActivity.this, "Invalid access token received", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Log.d("ACCESS TOKEN", "access_token: " + accessToken);
                                Retrieve_Crop_Health(accessToken);
                                Intent intent = new Intent(MainActivity.this,DisplayCropHealthIdentification.class );
                                intent.putExtra("crop_response", response.toString());
                                startActivity(intent);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                textView.setText("Error parsing JSON response");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("API Error", error.toString());
                    Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            queue.add(jsonObjectRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Retrieve_Crop_Health(String accessToken) {
        String url = SERVER_URL_RETRIEVE_CROP_HEALTH + "/" + accessToken;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Intent intent = new Intent(MainActivity.this,DisplayCropHealthIdentification.class );
//                        intent.putExtra("crop_response", response.toString());
//                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API Error", error.toString());
                        Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }
    /////////////////////////////////
    private JSONObject createJsonRequestBody(String base64Image) {
        JSONObject jsonRequestBody = new JSONObject();
        try {
            JSONArray images = new JSONArray();
            images.put("data:image/jpg;base64," + base64Image);

            jsonRequestBody.put("images", images);
            jsonRequestBody.put("latitude", 49.207);
            jsonRequestBody.put("longitude", 16.608);
            jsonRequestBody.put("similar_images", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonRequestBody;
    }

}
