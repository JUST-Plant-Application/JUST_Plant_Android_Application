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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
public class  GardenAdapter extends RecyclerView.Adapter<GardenAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Plantt> planttList;
    private OnPlantClickListener onPlantClickListener;
    private RequestQueue requestQueue;
    protected static final String SERVER_URL_identify = "https://just-plant-server.vercel.app/identify";


    public interface OnPlantClickListener {
        void onRemoveClick(String plantId);
    }

    public GardenAdapter(Context mContext,ArrayList<Plantt> planttList, OnPlantClickListener onPlantClickListener) {
        this.planttList = planttList;
        this.mContext = mContext;
        this.requestQueue = Volley.newRequestQueue(mContext);

        this.onPlantClickListener = onPlantClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_garden_item, parent, false);
        return new ViewHolder(view, onPlantClickListener, planttList);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plantt plantt = planttList.get(position);
        holder.plantName.setText(plantt.getPlantName());
        holder.plantScientificName.setText(plantt.getScientificName());
        holder.addedDate.setText(plantt.getAddedDate());

        Glide.with(holder.plantImage.getContext())
                .load(plantt.getImageUrl())
                .placeholder(R.drawable.plant_logo)
                .into(holder.plantImage);

        holder.openPlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageUrl = plantt.getImageUrl();
                showPlantImage(imageUrl);
            }
        });

    }

    @Override
    public int getItemCount() {
        return planttList.size();
    }

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



public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView plantImage ,openPlant;
        TextView plantName;
        TextView plantScientificName;
        TextView addedDate;
        ImageView removePlantButton;


        public ViewHolder(@NonNull View itemView, OnPlantClickListener onPlantClickListener, ArrayList<Plantt> planttList) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.plantImage);
            plantName = itemView.findViewById(R.id.plantName);
            plantScientificName = itemView.findViewById(R.id.scientificName);
            addedDate = itemView.findViewById(R.id.added_date);
            removePlantButton = itemView.findViewById(R.id.remove_from_garden);
            openPlant=itemView.findViewById(R.id.openPlant);

            removePlantButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPlantClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            String plantId = planttList.get(position).getPlantId();
                            onPlantClickListener.onRemoveClick(plantId);
                        }
                    }
                }
            });
        }
    }
}
