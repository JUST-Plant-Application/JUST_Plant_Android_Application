package com.example.just_plant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.just_plant.Adapter.CommentAdapter;
import com.example.just_plant.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommentActivity extends AppCompatActivity {

    EditText CommentContent;
    ImageView image_profile;
    TextView done;
    String postId;
    String publisherId;
    FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    ImageView goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CommentContent = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.profile_image_comment);
        done = findViewById(R.id.doneComment);
        recyclerView = findViewById(R.id.recycler_view_comments);
        goBack=findViewById(R.id.backToFeed);

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Feed_page.class);
                startActivity(intent);
                finish();
            }
        });

        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        publisherId = intent.getStringExtra("postAuthor");




        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, db);
        recyclerView.setAdapter(commentAdapter);


        done.setOnClickListener(v -> {
            if (CommentContent.getText().toString().equals("")) {
                Toast.makeText(CommentActivity.this, "You can't send an empty comment",
                        Toast.LENGTH_SHORT).show();
            } else {
                addComment();
            }
        });

        getImage();
        loadComments();
    }

    private void addComment() {
        Map<String, Object> comment = new HashMap<>();
        comment.put("userId", firebaseUser.getUid());
        comment.put("postId", postId);
        comment.put("CommentContent", CommentContent.getText().toString());
        comment.put("CommentDate", System.currentTimeMillis());

        db.collection("Comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    documentReference.update("id", documentReference.getId());
                    Log.d("CommentActivity", "Comment added successfully");
                    CommentContent.setText("");
                    loadComments(); // Refresh comments
                })
                .addOnFailureListener(e -> Log.e("CommentActivity", "Error adding comment", e));
    }


    private void getImage() {
        if (firebaseUser != null) {
            DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String profilePhotoUrl = document.getString("profile photo");
                        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                            Glide.with(CommentActivity.this).load(profilePhotoUrl).into(image_profile);
                        } else {
                            Log.e("CommentActivity", "No profile photo found");
                        }
                    } else {
                        Log.e("CommentActivity", "No such document");
                    }
                } else {
                    Log.e("CommentActivity", "get failed with ", task.getException());
                }
            });
        }
    }

    private void loadComments() {
        CollectionReference commentsRef = db.collection("Comments");

        commentsRef.orderBy("CommentDate", Query.Direction.DESCENDING).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        commentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String post = document.getString("postId");
                            if (postId.equals(post)) {
                                Comment comment = new Comment();
                                comment.setUserId(document.getString("userId"));
                                comment.setPostId(document.getString("postId"));
                                comment.setCommentContent(document.getString("CommentContent"));
                                comment.setCommentDate(document.getLong("CommentDate"));
                                comment.setId(document.getId()); // Set comment ID
                                commentList.add(comment);
                                Log.d("CommentActivity", "Comment: " + comment.getCommentContent());
                            } else {
                                Log.e("Firestore", postId + "not equal" + post);
                            }
                        }
                        commentAdapter.notifyDataSetChanged();
                        Log.d("CommentActivity", "Comments loaded: " + commentList.size());
                    } else {
                        Log.e("CommentActivity", "Error getting comments: ", task.getException());
                    }
                });
    }


}
