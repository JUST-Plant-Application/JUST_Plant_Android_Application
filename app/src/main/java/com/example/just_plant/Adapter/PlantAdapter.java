package com.example.just_plant.Adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
//
//public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.ViewHolder> {
//    private final List<Plant> plantList;
//    private final OnItemClickListener listener;
//
//    public interface OnItemClickListener {
//        void onItemClick(Plant plant);
//    }
//
//    public PlantAdapter(List<Plant> plantList, OnItemClickListener listener) {
//        this.plantList = plantList;
//        this.listener = listener;
//    }
//
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant_result, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        holder.bind(plantList.get(position), listener);
//    }
//
//    @Override
//    public int getItemCount() {
//        return plantList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        public ViewHolder(View itemView) {
//            super(itemView);
//        }
//
//        public void bind(final Plant plant, final OnItemClickListener listener) {
//            itemView.setOnClickListener(v -> listener.onItemClick(plant));
//        }
//    }
//    @SuppressLint("NotifyDataSetChanged")
//    public void updateData(List<Plant> newPlantList) {
//        Log.d("updateData", "Updating data with " + newPlantList.size() + " items");
//        plantList.clear();
//        plantList.addAll(newPlantList);
//        notifyDataSetChanged();
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    public void clearData() {
//        plantList.clear();
//        notifyDataSetChanged();
//    }
//
//}
//
/*
public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.ViewHolder> {
    private final List<PlantDetails> plantList;
    private final OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(PlantDetails plant);
    }
    public PlantAdapter(List<PlantDetails> plantList, OnItemClickListener listener) {
        this.plantList = plantList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant_result, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(plantList.get(position), listener);
    }
    @Override
    public int getItemCount() {
        return plantList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView plantNameTextView; // Assuming you have a TextView in your item layout to display the plant name
        public ViewHolder(View itemView) {
            super(itemView);
            plantNameTextView = itemView.findViewById(R.id.plantNameTextView); // Replace with the actual ID of your TextView
        }
        public void bind(final PlantDetails plant, final OnItemClickListener listener) {
            plantNameTextView.setText(plant.getName()); // Set the plant name on the TextView
            itemView.setOnClickListener(v -> listener.onItemClick(plant));
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<PlantDetails> newPlantList) {
        Log.d("updateData", "Updating data with " + newPlantList.size() + " items");
        plantList.clear();
        plantList.addAll(newPlantList);
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        plantList.clear();
        notifyDataSetChanged();
    }
}
*/
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.just_plant.R;
import com.example.just_plant.model.PlantDetails;
import com.squareup.picasso.Picasso;
import java.util.List;
public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.ViewHolder> {
    private final List<PlantDetails> plantList;
    private final OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(PlantDetails plant);
    }
    public PlantAdapter(List<PlantDetails> plantList, OnItemClickListener listener) {
        this.plantList = plantList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant_result, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(plantList.get(position), listener);
    }
    @Override
    public int getItemCount() {
        return plantList.size();
    }
    public List<PlantDetails> getCurrentList() {
        return plantList;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView plantNameTextView;
        ImageView plantImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            plantNameTextView = itemView.findViewById(R.id.plantNameTextView);
            plantImageView = itemView.findViewById(R.id.plantImageView);
        }
        public void bind(final PlantDetails plant, final OnItemClickListener listener) {
            plantNameTextView.setText(plant.getName());
            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(plant.getImageUrl())
                        .resize(800, 800)
                        .centerInside()
                        .into(plantImageView);
            } else {
                plantImageView.setImageResource(R.drawable.logo_edited); // Set a default image
            }
            itemView.setOnClickListener(v -> listener.onItemClick(plant));
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<PlantDetails> newPlantList) {
        plantList.clear();
        plantList.addAll(newPlantList);
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        plantList.clear();
        notifyDataSetChanged();
    }
}

