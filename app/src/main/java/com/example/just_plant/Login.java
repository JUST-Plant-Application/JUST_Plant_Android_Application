//package com.example.just_plant;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//public class Login extends AppCompatActivity {
//
//    Button SignInButton, BackToRegButton, btnResetPassword;
//    TextInputEditText EditTextEmail, EditTextPassword;
//    FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_login);
//        mAuth = FirebaseAuth.getInstance();
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });//end listener
//
//        EditTextEmail = findViewById(R.id.email);
//        EditTextPassword = findViewById(R.id.password);
//        SignInButton = findViewById(R.id.sign_Btn);
//        BackToRegButton = findViewById(R.id.signUp_Btn);
//        btnResetPassword = findViewById(R.id.reset_pw_Btn);
//
//        SignInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String email, password;
//                email = EditTextEmail.getText().toString();
//                password = EditTextPassword.getText().toString();
//
//                if (TextUtils.isEmpty(email)) {
//                    EditTextEmail.setError("Email is required");
//                    return;
//                }//end if
//                if (TextUtils.isEmpty(password)) {
//                    EditTextPassword.setError("Password is required");
//                    return;
//                }//end if
//
//                mAuth.signInWithEmailAndPassword(email, password)
//                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    FirebaseUser user = mAuth.getCurrentUser();
//                                    if (user != null && user.isEmailVerified()) {
//                                        Toast.makeText(Login.this, "Login Successful",
//                                                Toast.LENGTH_SHORT).show();
//                                        Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
//                                        startActivity(intent);
//                                    } else {
//                                        Toast.makeText(Login.this, "Please verify your email address to login",
//                                                Toast.LENGTH_SHORT).show();
//                                        mAuth.signOut(); // Sign out the user
//                                    }
//                                } else {
//                                    Toast.makeText(Login.this, "Authentication failed. check the password and email",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//            }
//        }); //end SignInButton Listener
//
//        BackToRegButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Login.this, Register.class);
//                startActivity(intent);
//            }
//        }); //end BackToRegButton Listener
//
//        // Reset password
//        btnResetPassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showResetPasswordDialog();
//            }
//        });
//    } //end onCreate
//
//    // Show reset password dialog
//    private void showResetPasswordDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Reset Password");
//
//        View view = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
//        builder.setView(view);
//
//        EditText emailEditText = view.findViewById(R.id.email_edit_text);
//
//        builder.setPositiveButton("Reset", (dialog, which) -> {
//            String email = emailEditText.getText().toString().trim();
//            if (TextUtils.isEmpty(email)) {
//                Toast.makeText(Login.this, "Please enter your email", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            mAuth.sendPasswordResetEmail(email)
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(Login.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(Login.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        });
//
//        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//}




package com.example.just_plant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class Login extends AppCompatActivity {

    Button SignInButton, BackToRegButton, btnResetPassword;
    TextInputEditText EditTextEmail, EditTextPassword;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditTextEmail = findViewById(R.id.email);
        EditTextPassword = findViewById(R.id.password);
        SignInButton = findViewById(R.id.sign_Btn);
        BackToRegButton = findViewById(R.id.signUp_Btn);
        btnResetPassword = findViewById(R.id.reset_pw_Btn);

        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = EditTextEmail.getText().toString();
                password = EditTextPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    EditTextEmail.setError("Email is required");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    EditTextPassword.setError("Password is required");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null && user.isEmailVerified()) {
                                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), TheMainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(Login.this, "Please verify your email address to login", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut(); // Sign out the user
                                    }
                                } else {
                                    Toast.makeText(Login.this, "Authentication failed. Check the password and email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        BackToRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

        // Reset password
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetPasswordDialog();
            }
        });
    }

    // Show reset password dialog
    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        View view = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
        builder.setView(view);

        EditText emailEditText = view.findViewById(R.id.email_edit_text);

        builder.setPositiveButton("Reset", (dialog, which) -> {
            String email = emailEditText.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Login.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Login.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
