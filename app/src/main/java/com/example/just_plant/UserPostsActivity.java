
package com.example.just_plant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.just_plant.Adapter.PostAdapter;
import com.example.just_plant.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserPostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;
    private FirebaseFirestore db;
    private String authorId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the authorId passed through the Intent
        authorId = getIntent().getStringExtra("authorId");

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(this, postLists);
        recyclerView.setAdapter(postAdapter);

        // Fetch posts by the author
        readPosts();
    }
//////////////
@Override
public void onBackPressed() {
    super.onBackPressed();
    Intent intent = new Intent(UserPostsActivity.this, TheMainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
    finish();
}
    ////////////////
    private void readPosts() {
        CollectionReference postsRef = db.collection("posts");
        postsRef.whereEqualTo("authorId", authorId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        postLists.clear(); // Clear the list before adding new posts

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Post post = new Post();
                            post.setPostId(document.getId());
                            post.setPostContent(document.getString("postContent"));
                            post.setPostAuthor(document.getString("authorId"));
                            post.setPostImage(document.getString("postImage"));
                            post.setPostTime(document.getLong("postDate"));

                            postLists.add(post);
                        }

                        // Sort the list by postDate in descending order
                        Collections.sort(postLists, new Comparator<Post>() {
                            @Override
                            public int compare(Post p1, Post p2) {
                                return Long.compare(p2.getPostTime(), p1.getPostTime());
                            }
                        });

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
