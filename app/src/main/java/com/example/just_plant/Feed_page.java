package com.example.just_plant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.just_plant.Adapter.PostAdapter;
import com.example.just_plant.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Feed_page extends AppCompatActivity {


    LinearLayout CreatePostBtn,ToGardenBtn,ToCategories,ToScanBtn,ToHomePage;

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        ToGardenBtn=findViewById(R.id.MYGarden_Btn);
        ToScanBtn=findViewById(R.id.Scan_Btn);
        ToCategories=findViewById(R.id.Diagnose_Btn);
        ToHomePage = findViewById(R.id.home_Btn);

        CreatePostBtn = findViewById(R.id.add_post);
        recyclerView = findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(this, postLists);
        recyclerView.setAdapter(postAdapter);

        fetchPosts();

        CreatePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                startActivity(intent);
            }
        });

        ToHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
                startActivity(intent);
            }
        });



        ToGardenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Garden_page.class);
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


        ToCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Categories_page.class);
                startActivity(intent);
                finish();
            }
        });


    }
///////////////////

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Feed_page.this, TheMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }


    /////////////////
    private void fetchPosts() {
        CollectionReference postsRef = FirebaseFirestore.getInstance().collection("posts");

        postsRef.orderBy("postDate", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        postLists.clear(); // Clear the list before adding new posts

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // Manually create and populate the Post object
                            Post post = new Post();
                            post.setPostId(document.getId());
                            post.setPostContent(document.getString("postContent"));
                            post.setPostAuthor(document.getString("authorId"));
                            post.setPostImage(document.getString("postImage"));
                            post.setPostTime(document.getLong("postDate")); // Assuming postDate is stored as a long

                            postLists.add(post);
                        }

                        postAdapter.setPosts(postLists);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FetchPosts", "Error fetching posts", e);
                    }
                });
    }

}
