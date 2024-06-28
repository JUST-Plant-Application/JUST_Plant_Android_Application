package com.example.just_plant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class EditProfileActivity extends AppCompatActivity {

    private EditText etUsername, etLocation;
    private Button btnUpdate,btnResetPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    ImageView ToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ToHome=findViewById(R.id.backTohome_update);
        etUsername = findViewById(R.id.et_username);
        etLocation = findViewById(R.id.et_location);
        btnUpdate = findViewById(R.id.btn_update);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        ToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /////////////////////////

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });


        // loadUserProfile();
    }
    private void resetPassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            if (email != null) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfileActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfileActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "User email not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfile() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating data");
        progressDialog.show();
        String username = etUsername.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            Map<String, Object> userMap = new HashMap<>();
            // Only add non-empty values to the update map
            if (!TextUtils.isEmpty(username)) {
                userMap.put("user name", username);
            }
            if (!TextUtils.isEmpty(location)) {
                userMap.put("location", location);
            }
            if (!userMap.isEmpty()) {
                userRef.update(userMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "No fields to update", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Optionally, load existing user profile
    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    etUsername.setText(documentSnapshot.getString("username"));
                    etLocation.setText(documentSnapshot.getString("location"));
                    // Note: Do not set password in the EditText for security reasons
                }
            }).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show());
        }
    }
}