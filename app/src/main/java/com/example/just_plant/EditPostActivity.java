package com.example.just_plant;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.just_plant.Adapter.PostAdapter;
import com.example.just_plant.model.Post;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 10;
    private ImageView imageAdded, cancelPostBtn;
    private TextView donePostBtn;
    private EditText description;
    private static final int EDIT_POST_REQUEST_CODE = 100;
    private PostAdapter postAdapter;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private String postId, postImage, postDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        imageAdded = findViewById(R.id.image_added);
        cancelPostBtn = findViewById(R.id.cancel_post_btn);
        donePostBtn = findViewById(R.id.done_post_btn);
        description = findViewById(R.id.description);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("posts");

        // Get post data from intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        postImage = intent.getStringExtra("postImage");
        postDescription = intent.getStringExtra("postDescription");

        // Load current post data
        Glide.with(this).load(postImage).into(imageAdded);
        description.setText(postDescription);

        // Handle Cancel button
        cancelPostBtn.setOnClickListener(v -> finish());

        // Handle Done button
        donePostBtn.setOnClickListener(v -> savePostChanges());

        // Set up image selection
        imageAdded.setOnClickListener(v -> openFileChooser());


        donePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePostChanges();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        } else if (requestCode == EDIT_POST_REQUEST_CODE && resultCode == RESULT_OK) {
            // Notify that the post has been updated
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }



    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        postImage = uri.toString();
                        Glide.with(EditPostActivity.this).load(postImage).into(imageAdded);
                        Toast.makeText(EditPostActivity.this, "Image upload successful", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditPostActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void savePostChanges() {
        String updatedDescription = description.getText().toString().trim();

        // Update post data in Firestore
        Map<String, Object> postUpdates = new HashMap<>();
        postUpdates.put("postContent", updatedDescription);
        postUpdates.put("postImage", postImage);

        db.collection("posts").document(postId)
                .update(postUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPostActivity.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                    // Pass updated post data back to calling activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedPost", (CharSequence) new Post(postId, updatedDescription, postImage)); // Adjust this constructor accordingly
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditPostActivity.this, "Failed to update post", Toast.LENGTH_SHORT).show();
                });
    }


}
