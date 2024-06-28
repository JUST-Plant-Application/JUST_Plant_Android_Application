package com.example.just_plant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    TextInputEditText EditTextEmail,EditTextPassword,EditTextUserName,EditTextLocation;
    Button RegButton,BackToSignInButton;
    FirebaseAuth mAuth;

    FirebaseFirestore db;

    Switch IsExpert;


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        mAuth= FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });//end setOnApplyWindowInsetsListener




        //....................................findViewById
        EditTextEmail=findViewById(R.id.email);
        EditTextPassword=findViewById(R.id.password);
        RegButton=findViewById(R.id.Reg_Btn);
        BackToSignInButton=findViewById(R.id.signInBack_Btn);
        EditTextUserName=findViewById(R.id.userName);
        IsExpert=findViewById(R.id.expert);
        EditTextLocation=findViewById(R.id.location);

        final boolean[] expert = new boolean[1];


        IsExpert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    expert[0] =true;
                } else {
                    expert[0] =false;
                }

            }
        });// end switch isExpert Listener



        //......................................Register Button Click listener
        RegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, userName,location,profile_image;
                email = EditTextEmail.getText().toString();
                password = EditTextPassword.getText().toString();
                userName = EditTextUserName.getText().toString();
                location=EditTextLocation.getText().toString();
                profile_image=null;

                //...............................................Check if Empty
                if(TextUtils.isEmpty(email)){
                    EditTextEmail.setError("Email is required");
                    return;
                }//end if
                if(TextUtils.isEmpty(password)){
                    EditTextPassword.setError("Password is required");
                    return;
                }//end if

                if(TextUtils.isEmpty(userName)){
                    EditTextUserName.setError("User Name is required");
                    return;
                }//end if

                if(TextUtils.isEmpty(location)){
                    EditTextLocation.setError("Location is required");
                    return;
                }//end if







                //.........................................Add user info to user FireStore collection
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    String userId = currentUser.getUid(); // Get the user's UID

                                    currentUser.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Register.this, "Verification email sent. Please verify your email address", Toast.LENGTH_LONG).show();
                                                        Map<String, Object> user = new HashMap<>();
                                                        user.put("user name", userName);
                                                        user.put("email", email);
                                                        user.put("Expert user", expert[0]);
                                                        user.put("location", location);
                                                        user.put("profile photo", profile_image);

                                                        db.collection("users")
                                                                .document(userId)
                                                                .set(user)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(Register.this, "Successfully saved user data",
                                                                                Toast.LENGTH_LONG).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(Register.this, "failed to save user data!!",
                                                                                Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                    } else {
                                                        Toast.makeText(Register.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                                        mAuth.getCurrentUser().delete();
                                                    }
                                                }
                                            });
                                }//end if for authentication check

//                                else {
//                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//                                    if(user!=null){
//                                        if(user.isEmailVerified()){
//                                        Toast.makeText(Register.this, "This email is already registered.  ",
//                                                Toast.LENGTH_SHORT).show();}
//                                        else{
//                                            Toast.makeText(Register.this, "Please verify your email address",
//                                                    Toast.LENGTH_SHORT).show();}
//                                    }
                                else{
                                    Toast.makeText(Register.this, "Authentication failed.or password syntax not correct",
                                            Toast.LENGTH_SHORT).show();
                                }//end else for authentication check

                            }//end onComplete
                        });//end OnCompleteListener
            }//end onClick
        });//end RegButton Listener


        BackToSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }//end onCreate
        });//end BackToSignInButton listener

    }//end onCreate

}//end Class