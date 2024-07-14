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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.just_plant.DisplayIdentificationResult;
import com.squareup.picasso.Picasso;

import com.example.just_plant.R;
import com.example.just_plant.model.Plant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;
public class PlantSAdapter extends RecyclerView.Adapter<PlantSAdapter.ViewHolder> {
    private List<Plant> plants;
    private OnPlantClickListener listener;
    private Context mContext;
    private RequestQueue requestQueue;
    protected static final String SERVER_URL_retrieve = "https://just-plant-server.vercel.app/retrieve-identification";
    protected static final String SERVER_URL_identify = "https://just-plant-server.vercel.app/identify";

    public interface OnPlantClickListener {
        void onPlantClick(Plant plant);
    }

    public PlantSAdapter(Context mContext, List<Plant> plants, OnPlantClickListener listener) {
        this.mContext = mContext;
        this.plants = plants;
        this.listener = listener;
        this.requestQueue = Volley.newRequestQueue(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plant plant = plants.get(position);
        holder.bind(plant, listener);

        holder.openPlant.setOnClickListener(v -> {
            String imageUrl = plant.getImage();
            showPlantImage(imageUrl);
        });
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    private void showPlantImage(String imageUrl) {
        Uri imageUri = Uri.parse(imageUrl);
        identifyPlant(imageUri);
    }

    private void identifyPlant(Uri imageUri) {
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
                                response -> {
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
                                },
                                error -> {
                                    Log.e("API Error", error.toString());
                                    Toast.makeText(mContext, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        requestQueue.add(jsonObjectRequest);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageView imageView;
        public TextView sName;
        public TextView categoryName;
        ImageView openPlant;


        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.plant_name);
            imageView = itemView.findViewById(R.id.plant_image);
            sName = itemView.findViewById(R.id.scientificName_plant);
            openPlant = itemView.findViewById(R.id.openPlantPagee);
            categoryName = itemView.findViewById(R.id.category_name_plant);

        }

        public void bind(final Plant plant, final OnPlantClickListener listener) {
            nameTextView.setText(plant.getName());
            sName.setText(plant.getsName());
            Picasso.get().load(plant.getImage())
                    .resize(200,200)
                    .centerInside()
                    .into(imageView);

            itemView.setOnClickListener(v -> listener.onPlantClick(plant));
        }
    }
}
