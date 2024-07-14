package com.example.just_plant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.just_plant.Adapter.PlantAdapter;
import com.example.just_plant.model.PlantDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class HomePage extends AppCompatActivity {
    private static final String SERVER_URL = "https://just-plant-server.vercel.app";
    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private RequestQueue requestQueue;
    private TextView text;
    private ImageView BackHomeBtn;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        requestQueue = Volley.newRequestQueue(this);

        recyclerView = findViewById(R.id.searchResultsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(HomePage.this));

        SearchView searchView = findViewById(R.id.autoCompleteTextView);
      //  text = findViewById(R.id.text);
        BackHomeBtn = findViewById(R.id.backTohome_search);

        BackHomeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
            startActivity(intent);
            finish();
        });

        adapter = new PlantAdapter(new ArrayList<>(), plant -> getPlantDetails(plant.getEntityId()));
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query) && query.length() >= 3) {
                    saveSearchHistory(query);
                    runSearchQuery(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText) && newText.length() >= 3) {
                    runSearchQuery(newText);
                } else {
                    adapter.clearData();
                }
                return true;
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("search_history", MODE_PRIVATE);
        Set<String> searchHistory = sharedPreferences.getStringSet("searches_" + userId, new HashSet<>());
        Intent intent = getIntent();
        if (intent.hasExtra("searchTerm")) {
            String searchTerm = intent.getStringExtra("searchTerm");
            searchView.setQuery(searchTerm, true);
        }
    }
/////////////

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(HomePage.this, TheMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /////////////////
    private void saveSearchHistory(String query) {
        SharedPreferences sharedPreferences = getSharedPreferences("search_history", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> currentSearches = new HashSet<>(sharedPreferences.getStringSet("searches_" + userId, new HashSet<>()));
        if (currentSearches.size() >= 5) {
            currentSearches.remove(currentSearches.iterator().next());
        }
        currentSearches.add(query);
        editor.putStringSet("searches_" + userId, currentSearches);
        editor.apply();
    }

    private void runSearchQuery(String query) {
        String url = SERVER_URL + "/plants/search?query=" + query;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<PlantDetails> plantDetailsList = parsePlants(response);
                    if (plantDetailsList != null && !plantDetailsList.isEmpty()) {
                        adapter.updateData(plantDetailsList);
                    } else {
                        adapter.clearData();
                        Toast.makeText(HomePage.this, "No results found.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("API Error", error.toString());
                    Toast.makeText(HomePage.this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonObjectRequest);
    }

    private List<PlantDetails> parsePlants(JSONObject response) {
        List<PlantDetails> plantDetailsList = new ArrayList<>();
        try {
            JSONArray entities = response.getJSONArray("entities");
            for (int i = 0; i < entities.length(); i++) {
                JSONObject plantObject = entities.getJSONObject(i);
                String name = plantObject.getString("entity_name");
                String entityId = plantObject.getString("access_token");
                String imageUrl = plantObject.optString("image_url");

                PlantDetails plantDetails = new PlantDetails(name, imageUrl, entityId);
                plantDetailsList.add(plantDetails);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return plantDetailsList;
    }

    private void getPlantDetails(String accessToken) {
        String url = SERVER_URL + "/plants/detail/" + accessToken;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String imageUrl = null;
                        if (response.has("image") && response.getJSONObject("image").has("value")) {
                            imageUrl = response.getJSONObject("image").getString("value");
                        }
                        String entityId = response.getString("entity_id");

                        Intent intent = new Intent(HomePage.this, DisplayPlantDetailsAfterSearch.class);
                        intent.putExtra("response", response.toString());
                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(HomePage.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }
}
