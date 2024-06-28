package com.example.just_plant.Adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.example.just_plant.model.Plantt;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantTAdapter extends RecyclerView.Adapter<PlantTAdapter.PlantViewHolder> {

    private Context mContext;
    private List<Plantt> planttList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private RequestQueue requestQueue;
    protected static final String SERVER_URL_identify = "https://just-plant-server.vercel.app/identify";


    public PlantTAdapter(Context mContext, List<Plantt> planttList) {
        this.mContext = mContext;
        this.planttList = planttList;
        this.requestQueue = Volley.newRequestQueue(mContext);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.plantt_item, parent, false);
        return new PlantTAdapter.PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plantt plantt = planttList.get(position);
        holder.plantName.setText(plantt.getPlantName());
        holder.scientificName.setText(plantt.getScientificName());

        Glide.with(mContext)
                .load(plantt.getImageUrl()) // plantt.getImage() should return the URL string
                .placeholder(R.drawable.plant_logo) // Error image if the URL is invalid
                .into(holder.plantImage);

        holder.addToGarden.setOnClickListener(v -> addToGarden(plantt));

        holder.openPlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageUrl = plantt.getImageUrl();
                showPlantImage(imageUrl);
            }
        });

        Log.e("FirebaseStorage", "5555555555555555555plant info"+holder.plantImage);
    }

    @Override
    public int getItemCount() {
        return planttList.size();
    }


    public static class PlantViewHolder extends RecyclerView.ViewHolder {

        ImageView plantImage,openPlant;
        public ImageView addToGarden;
        TextView plantName, scientificName;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            openPlant=itemView.findViewById(R.id.openPlantPage);
            plantImage = itemView.findViewById(R.id.plantImage);
            plantName = itemView.findViewById(R.id.plantName);
            scientificName = itemView.findViewById(R.id.scientificName);
            addToGarden = itemView.findViewById(R.id.AddToGarden_Btn);
        }
    }

    private void addToGarden(Plantt plantt) {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userGardenRef = db.collection("user_garden").document(userId);

        userGardenRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Check if the plantt is already in the garden
                    List<String> plantIds = (List<String>) document.get("plant_ids");
                    if (plantIds != null && plantIds.contains(plantt.getPlantId())) {
                        Toast.makeText(mContext, "Plant is already added", Toast.LENGTH_SHORT).show();
                    } else {
                        // Add the plantt to the garden
                        plantIds = plantIds == null ? new ArrayList<>() : plantIds;
                        plantIds.add(plantt.getPlantId());
                        Map<String, Object> data = new HashMap<>();
                        data.put("plant_ids", plantIds);
                        data.put("addedDate", new Date()); // Add the current date
                        userGardenRef.update(data).addOnSuccessListener(aVoid -> {
                            Toast.makeText(mContext, "Plant added to garden", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    // Create a new garden document for the user
                    Map<String, Object> data = new HashMap<>();
                    List<String> plantIds = new ArrayList<>();
                    plantIds.add(plantt.getPlantId());
                    data.put("plant_ids", plantIds);
                    data.put("addedDate", new Date()); // Add the current date
                    userGardenRef.set(data).addOnSuccessListener(aVoid -> {
                        Toast.makeText(mContext, "Plant added to garden", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }//end add to garden

    private void showPlantImage(String imageUrl) {
        Uri imageUri = Uri.parse(imageUrl);
        identifyPlant(imageUri);
    }

    private void identifyPlant(Uri imageUri) {
        try {
            Glide.with(mContext)
                    .asBitmap()
                    .load(imageUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Bitmap resizedBitmap = getResizedBitmap(resource, 800);
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
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
                                                    Toast.makeText(mContext, "Invalid access token received", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                Log.d("ACCESS TOKEN", "access_token: " + accessToken);

                                                Intent intent = new Intent(mContext, DisplayIdentificationResult.class);
                                                intent.putExtra("response", response.toString());
                                                mContext.startActivity(intent);

                                                retrieveIdentificationResult(accessToken);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(mContext, "Error parsing identification response", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("API Error", error.toString());
                                    Toast.makeText(mContext, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            requestQueue.add(jsonObjectRequest);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Error loading image", Toast.LENGTH_SHORT).show();
        }
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

    private void retrieveIdentificationResult(String accessToken) {
        // Implement this function if you need to retrieve additional results
    }
}


