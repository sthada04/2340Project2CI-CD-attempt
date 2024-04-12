package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import static com.example.spotifywrapped.MainActivity.AUTH_CODE_REQUEST_TOKEN;
import static com.example.spotifywrapped.MainActivity.CLIENT_ID;
import static com.example.spotifywrapped.MainActivity.CLIENT_SECRET;
import static com.example.spotifywrapped.MainActivity.REDIRECT_URI;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.databinding.ActivityProfileBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {
    public static final int AUTH_CODE_REQUEST_CODE = 1;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;
    private FirebaseFirestore fStore;
    private String token, accessCode;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call mCall;
    private com.example.spotifywrapped.databinding.ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            token = bundle.getString("token");
        }

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        userId = user.getUid();

        ImageView profilePic = binding.profileImg;
        TextView profileNameTxt = binding.profileNameTxt;
        TextView friendCodeTxt = binding.friendCodeTxt;

        DocumentReference docRef = fStore.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    setTextAsync(document.getString("name"), profileNameTxt);
                    setTextAsync("Friend Code: " +document.getString("friendCode"), friendCodeTxt);
                    if (document.contains("profilePicUrl")) {
                        Picasso.get().load(Uri.parse(document.getString("profilePicUrl"))).into(profilePic);
                    }
                } else {
                    Log.d("Doc", "No such document");
                }
            } else {
                Log.d("Doc", "get failed with ", task.getException());
            }
        });

        Button linkAccountBtn = binding.linkAccountBtn;
        Button logoutBtn = binding.logoutBtn;
        Button deleteAccountBtn = binding.deleteAccountBtn;
        Button backBtn = binding.backBtn;
        Button updateUserDetailsBtn = binding.updateAccountDetails;

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        logoutBtn.setOnClickListener((v) -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        linkAccountBtn.setOnClickListener((v) -> {
            getCode();
        });

        deleteAccountBtn.setOnClickListener((v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this, R.style.CustomAlertDialog);
            builder.setTitle("Confirm Delete");

            View popView = getLayoutInflater().inflate(R.layout.delete_confirmation_popup, null);
            Button yesBtn = popView.findViewById(R.id.yes_Btn);
            Button noBtn = popView.findViewById(R.id.no_Btn);

            builder.setView(popView);
            AlertDialog myDialog = builder.create();
            myDialog.show();

            yesBtn.setOnClickListener(v1 -> {
                fStore.collection("users").document(userId)
                        .delete()
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
                user.delete().addOnSuccessListener(v2 -> {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish();
                });
                myDialog.dismiss();
            });

            noBtn.setOnClickListener(v1 -> {
                myDialog.dismiss();
            });


        }));

        updateUserDetailsBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this, R.style.CustomAlertDialog);
            builder.setTitle("Update Account Details");

            AtomicBoolean confirmed = new AtomicBoolean(false);

            View popView = getLayoutInflater().inflate(R.layout.update_account_details_popup, null);
            Button updateEmailBtn = popView.findViewById(R.id.email_button);
            updateEmailBtn.setVisibility(View.GONE);
            Button updatePassBtn = popView.findViewById(R.id.password_button);
            updatePassBtn.setText("Submit");
            TextInputEditText emailEditText = popView.findViewById(R.id.email);
            TextInputEditText passwordEditText = popView.findViewById(R.id.password);
            TextView prompt = popView.findViewById(R.id.prompt);

            builder.setView(popView);
            AlertDialog myDialog = builder.create();
            myDialog.show();

            updateEmailBtn.setOnClickListener(v1 -> {
                String email = String.valueOf(emailEditText.getText());
                if (!checkInvalidEmail(email)) {
                    user.verifyBeforeUpdateEmail(email).addOnSuccessListener(v2 -> {
                        myDialog.dismiss();
                    });
                }
            });

            updatePassBtn.setOnClickListener(v1 -> {
                if (!confirmed.get()) {
                    String email = String.valueOf(emailEditText.getText());
                    String password = String.valueOf(passwordEditText.getText());

                    if (!checkInvalidEmail(email) && !checkInvalidPassword(password)) {
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(email, password);
                        user.reauthenticate(credential).addOnSuccessListener(v2 -> {
                            updateEmailBtn.setVisibility(View.VISIBLE);
                            emailEditText.setText(user.getEmail());
                            prompt.setText("Enter your new account details");
                            updatePassBtn.setText("Update Password");
                            confirmed.set(true);
                        });
                    }
                } else {
                    String password = String.valueOf(passwordEditText.getText());
                    if (!checkInvalidPassword(password)) {
                        user.updatePassword(password).addOnSuccessListener(v2 -> {
                            myDialog.dismiss();
                        });
                    }
                }
            });
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        ImageView profilePic = binding.profileImg;
        TextView profileNameTxt = binding.profileNameTxt;
        TextView friendCodeTxt = binding.friendCodeTxt;

        DocumentReference docRef = fStore.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    setTextAsync(document.getString("name"), profileNameTxt);
                    setTextAsync("Friend Code: " +document.getString("friendCode"), friendCodeTxt);
                    if (document.contains("profilePicUrl")) {
                        Picasso.get().load(Uri.parse(document.getString("profilePicUrl"))).into(profilePic);
                    }
                } else {
                    Log.d("Doc", "No such document");
                }
            } else {
                Log.d("Doc", "get failed with ", task.getException());
            }
        });
    }

    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }

    /**
     * Get code from Spotify using Code Auth
     * This method will open the Spotify login activity and get the code
     * What is code?
     * <a href="https://developer.spotify.com/documentation/general/guides/authorization-guide/">...</a>
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(ProfileActivity.this, AUTH_CODE_REQUEST_CODE, request);
    }

    public void getToken() {
        final Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()))
                .post(new FormBody.Builder()
                        .add("grant_type", "authorization_code")
                        .add("code", accessCode)
                        .add("redirect_uri", getRedirectUri().toString())
                        .build())
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    token = jsonObject.getString("access_token");
                    String refreshToken = jsonObject.getString("refresh_token");
                    String userId = user.getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userId);
                    Map<String, Object> map = new HashMap<>();
                    map.put("refreshToken", refreshToken);
                    documentReference.set(map, SetOptions.merge()).addOnSuccessListener(v -> {
                        Log.d(TAG, "onSuccess: Spotify refresh token added to " + userId);
                    }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }

    /**
     * Get authentication request
     *
     * @return the authentication request
     */
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[] { "user-read-email", "user-library-read", "user-top-read", "user-read-private" }) // <--- Change the scope of your requested token here
                .setCampaign("your-campaign-token")
                .build();
    }

    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        // Check which request code is present (if any)
        if (AUTH_CODE_REQUEST_CODE == requestCode) {
            accessCode = response.getCode();
            getToken();
        } else if (AUTH_CODE_REQUEST_TOKEN == requestCode) {
            token = response.getAccessToken();
            final Request request = new Request.Builder()
                    .url("https://api.spotify.com/v1/me")
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            cancelCall();
            mCall = mOkHttpClient.newCall(request);

            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("HTTP", "Failed to fetch data: " + e);
                    Toast.makeText(ProfileActivity.this, "Failed to fetch data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        final JSONObject jsonObject = new JSONObject(response.body().string());
                        Log.d("JSON", "JSON Data: " + jsonObject);
                        String name = jsonObject.getString("display_name");
                        DocumentReference documentReference = fStore.collection("users").document(userId);
                        if (jsonObject.getJSONArray("images").length() > 0) {
                            String imageUrl = jsonObject.getJSONArray("images").getJSONObject(1).getString("url");
                            Map<String, Object> map = new HashMap<>();
                            map.put("profilePicUrl", imageUrl);
                            documentReference.set(map, SetOptions.merge()).addOnSuccessListener(v -> {
                                Log.d(TAG, "onSuccess: Profile pic url saved " + imageUrl);
                            }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));
                        }
                        documentReference.update("name", name).addOnSuccessListener(r -> {
                            Log.d(TAG, "onSuccess: Name set to " + name);
                        }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));

                    } catch (JSONException e) {
                        Log.d("JSON", "Failed to parse data: " + e);
                    }
                }
            });
        }
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private boolean checkInvalidEmail(String email) {
        //Email Checks
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(ProfileActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (!email.contains("@") ||  !email.contains(".")) {
            Toast.makeText(ProfileActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    private boolean checkInvalidPassword(String password) {
        //Password Checks
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(ProfileActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (password.length() < 6) {
            Toast.makeText(ProfileActivity.this,
                    "Your password must be six characters long!", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (TextUtils.isDigitsOnly(password)) {
            Toast.makeText(ProfileActivity.this,
                    "Your password should contain at least one letter!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}