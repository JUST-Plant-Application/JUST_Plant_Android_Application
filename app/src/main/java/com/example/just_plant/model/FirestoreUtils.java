package com.example.just_plant.model;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreUtils {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface CategoryCallback {
        void onCallback(List<Category> categoryList);
    }

    public interface PlantCallback {
        void onCallback(List<Plant> plantList);
    }
    public void fetchCategories(final CategoryCallback callback) {
        db.collection("categories")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Category> categories = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Category category = document.toObject(Category.class);
                            category.setId(document.getId()); // Ensure ID is set
                            Log.d("FirestoreUtils", "Category: " + category.getName() + ", ID: " + category.getId());
                            categories.add(category);
                        }
                        callback.onCallback(categories);
                    } else {
                        Log.d("FirestoreUtils", "Error getting categories: ", task.getException());
                    }
                });
    }


    public void fetchPlants(String categoryId, final PlantCallback callback) {
        db.collection("categories").document(categoryId).collection("Plants")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Plant> plants = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Plant plant = document.toObject(Plant.class);
                            plant.setsName(document.getString("scientificName"));
                            Log.d("FirestoreUtils", "Plant: " + plant.getName());
                            plants.add(plant);
                        }
                        callback.onCallback(plants);
                    } else {
                        Log.d("FirestoreUtils", "Error getting plants: ", task.getException());
                    }
                });
    }

}
