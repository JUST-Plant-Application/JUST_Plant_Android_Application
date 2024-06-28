package com.example.just_plant;

import android.app.Instrumentation;
import android.app.ProgressDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    ImageView CancelPost,image_added;
    TextView done;
    EditText description;
    Uri imageUri;
    String myUrl="";
    StorageTask uploadTask;
    StorageReference storageReference;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    ImageView imageNull;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);
        mAuth= FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CancelPost=findViewById(R.id.cancel_post_btn);
        image_added=findViewById(R.id.image_added);
        done=findViewById(R.id.done_post_btn);
        description=findViewById(R.id.description);
        storageReference= FirebaseStorage.getInstance().getReference("posts");
        imageNull=image_added;


        CancelPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Feed_page.class);
                startActivity(intent);
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        image_added.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });




    }


    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();


        if (image_added != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isComplete()) {
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
                                String content = description.getText().toString();
                                long currentTime=System.currentTimeMillis();

                                Map<String, Object> post = new HashMap<>();
                                post.put("postImage", myUrl);
                                post.put("postContent", content);
                                post.put("authorId", mAuth.getCurrentUser().getUid());
                                post.put("postDate", currentTime);

                                db.collection("posts")
                                        .add(post)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(PostActivity.this, "Successfully saved",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        })//end SuccessListener for data save
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(PostActivity.this, "failed to save user data!!",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });//end failureListener for data save

                                progressDialog.dismiss();
                                startActivity(new Intent(PostActivity.this,Feed_page.class));
                                finish();
                            }//end if task
                            else {
                                Toast.makeText(PostActivity.this, "Failed task",
                                        Toast.LENGTH_SHORT).show();

                            }//end else task
                        }//end onComplete
                    })//end onCompleteListener
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, ""+e.getMessage(),
                                    Toast.LENGTH_SHORT).show();

                        }
                    });//end FailureListener
        }//end if image not null
        else{
            Toast.makeText(PostActivity.this, "No Image Selected!!",
                    Toast.LENGTH_SHORT).show();
        }//end else image is null

    }//end uploadImage


    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void selectImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && data!=null &&data.getData()!=null){
            imageUri=data.getData();
            image_added.setImageURI(imageUri);
        }
        else{
            Toast.makeText(PostActivity.this, "You Didn't add a photo",
                    Toast.LENGTH_SHORT).show();
        }
    }
}