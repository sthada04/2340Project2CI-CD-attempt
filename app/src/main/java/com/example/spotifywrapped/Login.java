package com.example.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private TextInputEditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize views, button, and auth
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        TextView textView = findViewById(R.id.registerNow);
        Button buttonLogin = findViewById(R.id.btn_login);

        //Set on click listener for text to switch to register
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        //Set on click listener for login button
        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            //Get email and password from edit text fields
            String email, password;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            //Sign in user with given email and password
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            //Switch to main activity
                            Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}