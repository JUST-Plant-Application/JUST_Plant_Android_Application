package com.example.just_plant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.just_plant.Adapter.GardenAdapter;
import com.example.just_plant.model.Plantt;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


    public class Garden_page extends AppCompatActivity {

        LinearLayout ToHomeBtn, ToFeedBtn, CategoriesBtn, ToScanBtn;
        private RecyclerView recyclerView;
        private GardenAdapter gardenAdapter;
        private ArrayList<Plantt> planttList;
        private FirebaseFirestore db;
        private FirebaseAuth mAuth;
        private CardView emptyGardenCard;

        ImageView addPlant;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_garden_page);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            CategoriesBtn = findViewById(R.id.Diagnose_Btn);
            ToFeedBtn = findViewById(R.id.Feed_Btn_);
            ToHomeBtn = findViewById(R.id.home_Btn);
            ToScanBtn = findViewById(R.id.Scan_Btn);
            addPlant = findViewById(R.id.add_plant);

            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            planttList = new ArrayList<>();
            gardenAdapter = new GardenAdapter(this,planttList, new GardenAdapter.OnPlantClickListener() {
                @Override
                public void onRemoveClick(String plantId) {
                    removePlantFromGarden(plantId);
                }
            });

            recyclerView.setAdapter(gardenAdapter);

            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            emptyGardenCard = findViewById(R.id.empty_garden_card);

            fetchUserGarden();

            ToHomeBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
                startActivity(intent);
                finish();
            });

            addPlant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            ToFeedBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), Feed_page.class);
                startActivity(intent);
                finish();
            });

            CategoriesBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), Categories_page.class);
                startActivity(intent);
                finish();
            });

            ToScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        private void fetchUserGarden() {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("user_garden").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            ArrayList<String> plantIds = (ArrayList<String>) document.get("plant_ids");

                            if (plantIds != null && !plantIds.isEmpty()) {
                                emptyGardenCard.setVisibility(View.GONE);
                                for (String plantId : plantIds) {
                                    db.collection("plants").document(plantId).get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful() && task1.getResult() != null) {
                                                    DocumentSnapshot doc = task1.getResult();
                                                    if (doc.exists()) {
                                                        Plantt plantt = doc.toObject(Plantt.class);
                                                        if (plantt != null) {
                                                            plantt.setPlantId(doc.getId());
                                                            plantt.setImageUrl(doc.getString("plantImage").toString());// Set the plantId
                                                            planttList.add(plantt);
                                                            gardenAdapter.notifyDataSetChanged();
                                                        }
                                                    } else {
                                                        Log.e("FetchGarden", "No Document");
                                                    }
                                                }
                                            });
                                    db.collection("garden_plants").document(plantId).get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful() && task1.getResult() != null) {
                                                    DocumentSnapshot doc = task1.getResult();
                                                    if (doc.exists()) {
                                                        Plantt plantt = doc.toObject(Plantt.class);
                                                        if (plantt != null) {
                                                            plantt.setPlantId(doc.getId());
                                                            plantt.setImageUrl(doc.getString("image").toString());
                                                            plantt.setPlantName(doc.getString("name").toString());
                                                            plantt.setScientificName(doc.getString("scientificName").toString());// Set the plantId
                                                            planttList.add(plantt);
                                                            gardenAdapter.notifyDataSetChanged();
                                                        }
                                                    } else {
                                                        Log.e("FetchGarden", "No Document");
                                                    }
                                                }
                                            });
                                }
                            } else {
                                emptyGardenCard.setVisibility(View.VISIBLE);
                            }
                        } else {
                            emptyGardenCard.setVisibility(View.VISIBLE);
                            Toast.makeText(Garden_page.this, "Failed to load garden", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void removePlantFromGarden(String plantId) {
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("user_garden").document(userId)
                    .update("plant_ids", FieldValue.arrayRemove(plantId))
                    .addOnSuccessListener(aVoid -> {
                        for (int i = 0; i < planttList.size(); i++) {
                            if (planttList.get(i).getPlantId().equals(plantId)) {
                                planttList.remove(i);
                                gardenAdapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                        Toast.makeText(Garden_page.this, "Plant removed from garden", Toast.LENGTH_SHORT).show();

                        if (planttList.isEmpty()) {
                            emptyGardenCard.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(Garden_page.this, "Failed to remove plant", Toast.LENGTH_SHORT).show());
        }
    }
