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
    private TextView CName;

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
        CName = findViewById(R.id.category_name_plant);

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
                fetchCategoryName(categoryId);
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


    ////////////
    private void fetchCategoryName(String categoryId) {
        firestoreUtils.getCategoryNameById(categoryId, new FirestoreUtils.FirestoreCallback() {
            @Override
            public void onCallback(String categoryName) {
                if (categoryName != null) {
                    CName.setText(categoryName);
                } else {
                    Log.e("PlantsActivity", "Category name not found for categoryId: " + categoryId);
                    Toast.makeText(PlantsActivity.this, "Category name not found.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ///
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




}
