package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Register extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private FirebaseFirestore fStore;


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

        fStore = FirebaseFirestore.getInstance();
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
                            String friendCode;
                            do {
                                friendCode = genRandomString();
                            } while (checkFriendCode(friendCode));
                            String finalFriendCode = friendCode;

                            Map<String, Object> user = new HashMap<>();
                            user.put("friendCode", friendCode);
                            user.put("friends", new ArrayList<String>());
                            user.put("name", mAuth.getCurrentUser().getUid());
                            DocumentReference documentReference = fStore.collection("users").document(mAuth.getCurrentUser().getUid());
                            documentReference.set(user, SetOptions.merge()).addOnSuccessListener(r -> {
                                Log.d(TAG, "onSuccess: Friend Code " + finalFriendCode);
                            }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));
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
                    "Your password must be six characters long!", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (TextUtils.isDigitsOnly(password)) {
            Toast.makeText(Register.this,
                    "Your password should contain at least one letter!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private String genRandomString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private boolean checkFriendCode(String friendCode) {
        AtomicBoolean valid = new AtomicBoolean(false);
        fStore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (friendCode.equals(document.getString("friendCode"))) {
                                valid.set(true);
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        return valid.get();
    }
}