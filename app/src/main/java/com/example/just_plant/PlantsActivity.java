package com.example.just_plant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.just_plant.Adapter.PlantSAdapter;
import com.example.just_plant.model.FirestoreUtils;
import com.example.just_plant.model.Plant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantsActivity extends AppCompatActivity {
    private RecyclerView plantRecyclerView;
    private PlantSAdapter plantSAdapter;
    private List<Plant> plantList;
    private FirestoreUtils firestoreUtils;

    ImageView backToCategory;
    ImageView openPlant;

    RequestQueue queue;
    protected static final String SERVER_URL_identify = "https://just-plant-server.vercel.app/identify";
    protected static final String SERVER_URL_retrieve = "https://just-plant-server.vercel.app/retrieve-identification";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plants);
        queue = Volley.newRequestQueue(this);

        backToCategory = findViewById(R.id.backToCategory);

        plantRecyclerView = findViewById(R.id.plant_recycler_view);
        plantRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        plantList = new ArrayList<>();

        plantSAdapter = new PlantSAdapter(this, plantList, plant -> {
            // Handle plant click
        });

        plantRecyclerView.setAdapter(plantSAdapter);

        backToCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlantsActivity.this, Categories_page.class);
                startActivity(intent);
                finish();
            }
        });

        firestoreUtils = new FirestoreUtils();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("categoryId")) {
            String categoryId = intent.getStringExtra("categoryId");
            Log.d("PlantsActivity", "Received categoryId: " + categoryId);
            if (categoryId != null) {
                fetchPlants(categoryId);
            } else {
                Log.e("PlantsActivity", "Category ID is null in the intent extras.");
                Toast.makeText(this, "Category ID is null.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("PlantsActivity", "No category ID found in the intent extras.");
            Toast.makeText(this, "No category ID provided.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPlants(String categoryId) {
        firestoreUtils.fetchPlants(categoryId, plants -> {
            plantList.clear();
            plantList.addAll(plants);
            if (plantSAdapter != null) {
                plantSAdapter.notifyDataSetChanged();
            } else {
                Log.e("PlantsActivity", "plantSAdapter is null");
            }
            Log.d("PlantsActivity", "Plants fetched: " + plants.size());
        });
    }

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

    private void identifyPlant(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
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
                                    Toast.makeText(PlantsActivity.this, "Invalid access token received", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                JSONObject result = response.getJSONObject("result");
                                JSONArray suggestions = result.getJSONObject("classification").getJSONArray("suggestions");
                                JSONObject firstSuggestion = suggestions.getJSONObject(0);
                                String plantId = firstSuggestion.getString("id");
                                Log.d("ACCESS TOKEN", "access_token: " + accessToken);
                                Log.d("PlantID", "ID of the first plant suggestion: " + plantId);

                                Intent intent = new Intent(PlantsActivity.this, DisplayIdentificationResult.class);
                                intent.putExtra("response", response.toString());
                                startActivity(intent);

                                Retrieve_Identification(accessToken);
                            } catch (JSONException e) {
                                e.printStackTrace();

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("API Error", error.toString());
                    Toast.makeText(PlantsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Log.d("the response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API Error", error.toString());
                        Toast.makeText(PlantsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
}
