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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.just_plant.Adapter.CategoryAdapter;
import com.example.just_plant.model.Category;
import com.example.just_plant.model.FirestoreUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;


public class Categories_page extends AppCompatActivity {

    LinearLayout ToHomeBtn,ToFeedBtn,ToGardenBtn,ToSearchBtn,ToScanBtn;


    private FirebaseAuth mAuth;
    private FirestoreUtils firestoreUtils;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_categories_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });


        ToGardenBtn=findViewById(R.id.MYGarden_Btn);
        ToFeedBtn=findViewById(R.id.Feed_Btn_);
        ToHomeBtn=findViewById(R.id.home_Btn);
        ToSearchBtn=findViewById(R.id.SearchPlant_category);
        ToScanBtn=findViewById(R.id.Scan_Btn);

        mAuth = FirebaseAuth.getInstance();
        firestoreUtils = new FirestoreUtils();

        if (mAuth.getCurrentUser() == null) {
            // Redirect to login if user is not authenticated
            Intent intent = new Intent(Categories_page.this, Login.class);
            startActivity(intent);
            finish();
            return;
        }

        ToGardenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Garden_page.class);
                startActivity(intent);
                finish();
            }
        });


        ToSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                startActivity(intent);
                finish();
            }
        });

        ToHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        ToFeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Feed_page.class);
                startActivity(intent);
                finish();
            }
        });

        ToScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        ////////////////////////////


        recyclerView = findViewById(R.id.recyclerViewCC);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);




        // RecyclerView categoryRecyclerView = findViewById(R.id.category_recycler_view);
        //categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            String categoryId = category.getId();
            Log.d("Categories_page", "Passing category ID: " + categoryId);
            Intent intent = new Intent(Categories_page.this, PlantsActivity.class);
            intent.putExtra("categoryId", categoryId);
            startActivity(intent);
        });


        recyclerView.setAdapter(categoryAdapter);
        fetchCategories();

    }

    private void fetchCategories() {
        firestoreUtils.fetchCategories(categories -> {
            categoryList.clear();
            categoryList.addAll(categories);
            categoryAdapter.notifyDataSetChanged();
            Log.d("CategoriesActivity", "Categories fetched: " + categories.size());
        });
    }

    // Override onStart to check if user is authenticated
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Redirect to login screen if not authenticated
            Intent intent = new Intent(Categories_page.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            // Fetch categories if authenticated
            fetchCategories();
        }
    }
}




