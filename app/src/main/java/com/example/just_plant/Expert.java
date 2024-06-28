package com.example.just_plant;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;
import java.util.Map;

public class Expert extends AppCompatActivity {

    ImageView image_added, backToHome;
    TextInputEditText email, description;

    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    Button SendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expert);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        image_added = findViewById(R.id.image_added_expert);
        backToHome = findViewById(R.id.backTohome_expert);
        email = findViewById(R.id.email_expert);
        description = findViewById(R.id.Description_expert);
        SendBtn = findViewById(R.id.sendBtn);

        storageReference = FirebaseStorage.getInstance().getReference("Expert Suggestions");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        backToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = email.getText().toString().trim();
                String descriptionInput = description.getText().toString().trim();

                if (emailInput.isEmpty() || descriptionInput.isEmpty()) {
                    if(TextUtils.isEmpty(emailInput)){
                        email.setError("Email is required");
                        return;
                    }//end if
                    if(TextUtils.isEmpty(descriptionInput)){
                        description.setError("Description is required");
                        return;
                    }//end if
                }
                else{
                    sendMsg();
                }
            }
        });

        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    private void sendMsg() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();
                        saveData(progressDialog);
                    } else {
                        Toast.makeText(Expert.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Expert.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            saveData(progressDialog);
        }
    }

    private void saveData(ProgressDialog progressDialog) {
        String content = description.getText().toString();

        Map<String, Object> expert = new HashMap<>();
        expert.put("Description", content);
        expert.put("ExpertId", mAuth.getCurrentUser().getUid());
        expert.put("Expert Email", email.getText().toString());

        if (!myUrl.isEmpty()) {
            expert.put("Image", myUrl);
        }

        db.collection("Experts Feedback")
                .add(expert)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(Expert.this, "Successfully sent", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Expert.this, "Failed to send!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUri = data.getData();
            image_added.setImageURI(imageUri);
        } else {
            Toast.makeText(Expert.this, "You didn't add a photo", Toast.LENGTH_SHORT).show();
        }
    }
}
