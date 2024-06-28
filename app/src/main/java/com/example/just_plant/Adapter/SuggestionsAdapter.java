package com.example.just_plant.Adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.just_plant.DisplayIdentificationResult;
import com.example.just_plant.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {


    protected static final String SERVER_URL_identify = "https://just-plant-server.vercel.app/identify";

    protected static final String SERVER_URL_retrieve = "https://just-plant-server.vercel.app/retrieve-identification";

    private Context context;
    private JSONArray suggestions;
    private RequestQueue requestQueue;
    public SuggestionsAdapter(Context context, JSONArray suggestions) {
        this.context = context;
        this.suggestions = suggestions;
        this.requestQueue = Volley.newRequestQueue(context);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject suggestion = suggestions.getJSONObject(position);
            JSONObject details = suggestion.getJSONObject("details");
            // Load image using Glide for smooth handling of image loading and caching
            String imageUrl = details.getJSONObject("image").getString("value");
            Glide.with(context).load(imageUrl).into(holder.imageView);
            holder.name.setText(suggestion.getString("name"));
            holder.probability.setText("Probability: " + suggestion.getString("probability"));
            // Set click listener to handle item click
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Get image URL from suggestion
                        String imageUrl = details.getJSONObject("image").getString("value");
                        Uri imageUri = Uri.parse(imageUrl); // Convert to Uri
                        // Call identifyPlant method
                        identifyPlant(imageUri);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error parsing suggestion details", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error parsing suggestion", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to resize bitmap to avoid memory issues
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
    // Method to handle plant identification
    // Method to handle plant identification
    private void identifyPlant(Uri imageUri) {
        try {
            Bitmap bitmap = null;
            // Directly decode image from URL using Glide for smooth loading
            Glide.with(context)
                    .asBitmap()
                    .load(imageUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            // Resize bitmap to avoid memory issues
                            Bitmap resizedBitmap = getResizedBitmap(resource, 800);
                            // Convert bitmap to base64 string
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                            byte[] imageBytes = byteArrayOutputStream.toByteArray();
                            String base64ImageString = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                            // Create JSON request body
                            JSONObject jsonRequestBody = createJsonRequestBody(base64ImageString);
                            // Make identification request
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, SERVER_URL_identify, jsonRequestBody,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                String accessToken = response.getString("access_token");
                                                if (accessToken == null || accessToken.isEmpty()) {
                                                    Toast.makeText(context, "Invalid access token received", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                Log.d("ACCESS TOKEN", "access_token: " + accessToken);
                                                // Start DisplayIdentificationResult activity
                                                Intent intent = new Intent(context, DisplayIdentificationResult.class);
                                                intent.putExtra("response", response.toString());
                                                context.startActivity(intent);
                                                // Retrieve identification results
                                                retrieveIdentificationResult(accessToken);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(context, "Error parsing identification response", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("API Error", error.toString());
                                    Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            // Add request to queue
                            requestQueue.add(jsonObjectRequest);
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Handle placeholder or clear resources if needed
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to retrieve identification results
    private void retrieveIdentificationResult(String accessToken) {
        String url = SERVER_URL_retrieve + "/" + accessToken;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle retrieved identification results if needed
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API Error", error.toString());
                        Toast.makeText(context, "Error retrieving identification results", Toast.LENGTH_SHORT).show();
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
        // Add request to queue
        requestQueue.add(jsonObjectRequest);
    }
    // Method to create JSON request body for plant identification
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
    @Override
    public int getItemCount() {
        return suggestions.length();
    }
    // ViewHolder class for the RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name;
        TextView probability;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.grid_item_image);
            name = itemView.findViewById(R.id.grid_item_name);
            probability = itemView.findViewById(R.id.grid_item_probability);
        }
    }
}
