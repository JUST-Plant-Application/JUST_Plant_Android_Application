package com.example.just_plant;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.just_plant.Adapter.PlantTAdapter;
import com.example.just_plant.model.Plantt;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TheMainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;

    FirebaseAuth auth;
    FirebaseUser user;
    TextView nameInNav;
    TextView emailInNav;

    FirebaseFirestore db;
    ImageView profileImage;

    DrawerLayout drawerLayout;
    View menu;
    LinearLayout showPosts,deleteAccount, logout, SendExpertUpdate, SetProfileImage,EditProfile,About,ToGardenBtn,ToSearchBtn,ToScanBtn,ToFeedPage,ToCategories;

    TextView expertOptionT;
    ImageView expertOptionI, expertCheck;
    Uri imageUri;
    String myUrl="";
    StorageTask uploadTask;
    StorageReference storageReference;

    private RecyclerView recyclerView;
    private PlantTAdapter plantTAdapter;
    private List<Plantt> planttList;

    private LinearLayout recentSearchesList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_the_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });//listener


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        planttList = new ArrayList<>();
        plantTAdapter = new PlantTAdapter(this, planttList);
        recyclerView.setAdapter(plantTAdapter);


        ToSearchBtn=findViewById(R.id.SearchEngin_main);
        EditProfile=findViewById(R.id.Edit_profile_Btn);
        ToGardenBtn=findViewById(R.id.MYGarden_Btn);
        ToScanBtn=findViewById(R.id.Scan_Btn);
        ToFeedPage=findViewById(R.id.Feed_Btn);
        ToCategories=findViewById(R.id.Diagnose_Btn);
        deleteAccount =findViewById(R.id.nav_delete_account);
        showPosts=findViewById(R.id.show_posts_Btn);


        db=FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        ToFeedPage=findViewById(R.id.Feed_Btn);
        nameInNav=findViewById(R.id.usernameNav);
        emailInNav=findViewById(R.id.emailNav);
        drawerLayout=findViewById(R.id.drawerLayout);
        logout=findViewById(R.id.Logout);
        menu=findViewById(R.id.menu);
        expertOptionI=findViewById(R.id.expertOptionI);
        expertOptionT=findViewById(R.id.expertOptionT);
        SendExpertUpdate=findViewById(R.id.expertBtn);
        expertCheck=findViewById(R.id.check);
        SetProfileImage=findViewById(R.id.setPic);
        profileImage=findViewById(R.id.profile_image);
        About=findViewById(R.id.infoBtn);
        storageReference= FirebaseStorage.getInstance().getReference("users");
        recentSearchesList = findViewById(R.id.recent_searches_list);


        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String userName = document.getString("user name");
                        String email = document.getString("email");
                        boolean expert = document.getBoolean("Expert user");
                        Log.d(TAG, "Welcome " + userName);
                        nameInNav.setText(userName);
                        emailInNav.setText(email);

                        String profileImageUrl = document.getString("profile photo");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            loadImageIntoImageView(profileImageUrl);
                        }

                        if (expert) {
                            expertOptionT.setTextColor(getResources().getColor(R.color.app_green));
                            expertOptionI.setImageResource(R.drawable.if_expert);
                            expertCheck.setImageResource(R.drawable.checklist);
                            SendExpertUpdate.setOnClickListener(v -> {
                                Intent intent = new Intent(getApplicationContext(), Expert.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            SendExpertUpdate.setOnClickListener(v -> Toast.makeText(TheMainActivity.this, "You Are Not A Plantt Expert", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(TheMainActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TheMainActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            });

            loadSearchHistory(userId);
        } else {
            redirectToLogin();
        }
        if(user==null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }



        menu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
//        logout.setOnClickListener(v -> {
//            FirebaseAuth.getInstance().signOut();
//            redirectToLogin();
//            Toast.makeText(TheMainActivity.this, "Logout Successfully", Toast.LENGTH_SHORT).show();
//        });



        logout.setOnClickListener(v -> {
            new AlertDialog.Builder(TheMainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User clicked "Yes"
                        FirebaseAuth.getInstance().signOut();
                        redirectToLogin();
                        Toast.makeText(TheMainActivity.this, "Logout Successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null) // User clicked "No", so do nothing
                    .show();
        });

        SetProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });//end stProfileImage btn Listener

        About.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AboutApp.class);
                startActivity(intent);
                finish();
            }
        });//end about btn Listener



        ToFeedPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Feed_page.class);
                startActivity(intent);
                finish();
            }
        });//end ToFeedPage Listener


        ToGardenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Garden_page.class);
                startActivity(intent);
                finish();
            }
        });

        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ToScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
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

        ToCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Categories_page.class);
                startActivity(intent);
                finish();
            }
        });


        deleteAccount.setOnClickListener(v -> deleteAccount());
        fetchPlants();
/////////////////////////////////

        showPosts.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                Intent intent = new Intent(TheMainActivity.this, UserPostsActivity.class);
                intent.putExtra("authorId", userId);
                startActivity(intent);
            } else {
                // Handle case where currentUser is null
                Log.e("MainActivity", "Current user is null");
            }
        });

    }//end onCreate

///////////////////////////////
@Override
public void onBackPressed() {
    // Do something if needed, otherwise let the default behavior happen
    // For example, you might want to exit the app or show a confirmation dialog
    super.onBackPressed();
    finishAffinity(); // This will close all activities and exit the app
}


    ///////////////////////////
    private void deleteAccount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account permanently? This action cannot be undone.")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Continue with account deletion
                        String userId = currentUser.getUid();

                        // Delete user data from Firestore
                        db.collection("users").document(userId)
                                .delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Delete user profile photo from Storage
                                        StorageReference profilePhotoRef = storageReference.child(userId + "/profile.jpg");
                                        profilePhotoRef.delete().addOnCompleteListener(photoDeleteTask -> {
                                            if (photoDeleteTask.isSuccessful()) {
                                                Log.d(TAG, "Profile photo deleted");
                                            } else {
                                                Log.d(TAG, "Failed to delete profile photo");
                                            }
                                        });

                                        // Delete user authentication
                                        currentUser.delete().addOnCompleteListener(deleteTask -> {
                                            if (deleteTask.isSuccessful()) {
                                                Log.d(TAG, "User account deleted");
                                                Toast.makeText(TheMainActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                redirectToLogin();
                                            } else {
                                                Log.d(TAG, "Failed to delete user account");
                                                Toast.makeText(TheMainActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Log.d(TAG, "Failed to delete user data");
                                        Toast.makeText(TheMainActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        // Do nothing
                        dialog.dismiss();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(TheMainActivity.this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

//    private void redirectToLogin() {
//        Intent intent = new Intent(getApplicationContext(), Login.class);
//        startActivity(intent);
//        finish();
//    }

    private void loadSearchHistory(String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("search_history", MODE_PRIVATE);
        Set<String> searchHistory = sharedPreferences.getStringSet("searches_" + userId, new HashSet<>());

        recentSearchesList.removeAllViews();
        for (String searchTerm : searchHistory) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View searchHistoryItem = inflater.inflate(R.layout.search_history_item, recentSearchesList, false);

            TextView searchTermText = searchHistoryItem.findViewById(R.id.search_term_text);
            searchTermText.setText(searchTerm);

            ImageView deleteIcon = searchHistoryItem.findViewById(R.id.delete_icon);
            deleteIcon.setOnClickListener(v -> {
                removeSearchTerm(userId, searchTerm);
                recentSearchesList.removeView(searchHistoryItem);
            });

            recentSearchesList.addView(searchHistoryItem);
        }
    }

    private void removeSearchTerm(String userId, String searchTerm) {
        SharedPreferences sharedPreferences = getSharedPreferences("search_history", MODE_PRIVATE);
        Set<String> searchHistory = new HashSet<>(sharedPreferences.getStringSet("searches_" + userId, new HashSet<>()));
        searchHistory.remove(searchTerm);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("searches_" + userId, searchHistory);
        editor.apply();
    }

    public void onHistoryItemClick(View view) {
        TextView searchTermText = view.findViewById(R.id.search_term_text);
        String searchTerm = searchTermText.getText().toString();

        Intent intent = new Intent(this, HomePage.class);
        intent.putExtra("searchTerm", searchTerm);
        startActivity(intent);
    }

    private void fetchPlants() {
        CollectionReference plantsRef = FirebaseFirestore.getInstance().collection("plants");
                plantsRef.orderBy("orderNumber", Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot d : list) {
                                    Log.d(TAG, "DocumentSnapshot ID: " + d.getId());  // Log document ID
                                    Plantt plantt = new Plantt();
                                    plantt.setPlantId(d.getId());  // Use document ID as plantId
                                    plantt.setImageUrl(d.getString("plantImage"));
                                    plantt.setPlantName(d.getString("plantName"));
                                    plantt.setScientificName(d.getString("scientificName"));
                                    planttList.add(plantt);
                                    Log.d(TAG, "Plant added: " + plantt.getPlantName());  // Log plantt name
                                }
                                plantTAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "No documents found");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error getting documents: ", e);
                        });
    }


    public static void openDrawer(DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);

    }//end openDrawer function

    public static void closeDrawer(DrawerLayout drawerLayout){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }//end closerDrawer function

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }//end onPause function



    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }//end getFileExtension function

    private void selectImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }//end selectImage function

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && data!=null &&data.getData()!=null){
            imageUri=data.getData();
            profileImage.setImageURI(imageUri);
            uploadImageToFirebaseStorage(imageUri);
        }//end if
        else{
            Toast.makeText(TheMainActivity.this, "You Didn't add a photo",
                    Toast.LENGTH_SHORT).show();
        }//end else
    }//end onActivityResult function

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();
        StorageReference profileImageRef = storageRef.child("profile_images/" + userId + ".jpg");

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateProfileImageInFirestore(downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle unsuccessful uploads
                    Log.e("FirebaseStorage", "Failed to upload image", e);
                });
    }//end uploadImageToFirebaseStorage

    private void updateProfileImageInFirestore(String downloadUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.update("profile photo", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    // Profile image URL updated successfully
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Log.e("Firestore", "Failed to update profile image URL", e);
                });
    }//end updateProfileImageInFirestore


    private void loadImageIntoImageView(String profileImageUrl) {
        Glide.with(this)
                .load(profileImageUrl)
                .into(profileImage);
    }//end loadImageIntoImageView


}//end class


