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

public class Register extends AppCompatActivity {

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
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        Button buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        TextView textView = findViewById(R.id.loginNow);

        //Set on click listener for text to switch to login
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        //Set on click listener for register button
        buttonReg.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            //Get email and password from edit text fields
            String email, password;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());

            //Check if register credentials are valid
            if (checkInvalidCredentials(email, password)) return;

            //Create new user with given email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            //Switch to main activity
                            Toast.makeText(Register.this, "Account Created",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private boolean checkInvalidCredentials(String email, String password) {
        //Email Checks
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (!email.contains("@") ||  !email.contains(".")) {
            Toast.makeText(Register.this, "Invalid email", Toast.LENGTH_SHORT).show();
            return true;
        }

        //Password Checks
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (password.length() < 6) {
            Toast.makeText(Register.this,
                    "Your password must be four characters long!", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (TextUtils.isDigitsOnly(password)) {
            Toast.makeText(Register.this,
                    "Your password should contain at least one letter!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}