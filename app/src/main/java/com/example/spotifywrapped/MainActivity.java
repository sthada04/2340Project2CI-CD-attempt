package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "eb4c992fe84d4e0dbad8f1f198437b9d";
    public static final String REDIRECT_URI = "spotify-sdk://auth";

    public static final int AUTH_CODE_REQUEST_CODE = 1, AUTH_CODE_REQUEST_TOKEN = 0;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessCode, token;
    private Call mCall;
    private TextView profileTextView;

    private FirebaseAuth auth;
    private Button signOutBtn;
    private Button friendsListBtn;
    private FirebaseUser user;

    private FirebaseFirestore fStore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the views
        profileTextView = (TextView) findViewById(R.id.response_text_view);

        // Initialize the buttons
        Button linkAccBtn = (Button) findViewById(R.id.code_btn);
        Button profileBtn = (Button) findViewById(R.id.profile_btn);

        auth = FirebaseAuth.getInstance();
        signOutBtn = findViewById(R.id.logout_btn);
        friendsListBtn = findViewById(R.id.friends_btn);
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        fStore = FirebaseFirestore.getInstance();

        //Check for access code in Firebase database
        userId = user.getUid();
        DocumentReference docRef = fStore.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    mAccessCode = document.getString("code");
                    if (mAccessCode != null) {
                        getToken();
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });


        //Button Click Listeners
        signOutBtn.setOnClickListener((v) -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        friendsListBtn.setOnClickListener((v) -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Friends.class);
            startActivity(intent);
        });

        linkAccBtn.setOnClickListener((v) -> {
            getCode();
        });

        profileBtn.setOnClickListener((v) -> {
            onGetUserProfileClicked();
        });

    }


    /**
     * Get code from Spotify using Code Auth
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_CODE_REQUEST_CODE, request);
    }

    public void getToken() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_CODE_REQUEST_TOKEN, request);
    }


    /**
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        // Check which request code is present (if any)
        if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            userId = user.getUid();
            DocumentReference documentReference = fStore.collection("users").document(userId);
            Map<String, Object> user = new HashMap<>();
            user.put("code", mAccessCode);
            documentReference.set(user, SetOptions.merge()).addOnSuccessListener(v -> {
                Log.d(TAG, "onSuccess: Spotify code added to " + userId);
            }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));
            getToken();
        } else if (AUTH_CODE_REQUEST_TOKEN == requestCode) {
            token = response.getAccessToken();
        }
    }

    public void onGetUserProfileClicked() {
        Log.d("ProfileCLicked", "onGetUserProfile clicked\n");
        onGetUserTopSongs();
    }
    public void sample(){
        if (token == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
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
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    //Log.d("JSON", "Info: "+ response.body().string());
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    setTextAsync(jsonObject.toString(3), profileTextView);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    //FIXME Calling looper.prepare() here fixes the initial crash. The issue on my
                    // computer may have to do with something wrong with my toast class. Also, I
                    // get: Failed to open file '/data/data/com.example.spotifywrapped/code_cache/.overlay/base.apk/assets/dexopt/baseline.prof': No such file or directory earlier in call
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
                .setScopes(new String[] { "user-read-email", "user-library-read", "user-top-read" }) // <--- Change the scope of your requested token here
                .setCampaign("your-campaign-token")
                .build();
    }

    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }


    public void onGetUserTopSongs() {
        if (token == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks?time_range=long_term&limit=10&offset=0")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    Log.d("body message: ", response.body().string());
                    final JSONObject jsonObject = new JSONObject();
                    setTextAsync(jsonObject.toString(3), profileTextView);
                    Log.d("JSON to string for get songs", jsonObject.toString());
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    //FIXME Calling looper.prepare() here fixes the initial crash. The issue on my
                    // computer may have to do with something wrong with my toast class. Also, I
                    // get: Failed to open file '/data/data/com.example.spotifywrapped/code_cache/.overlay/base.apk/assets/dexopt/baseline.prof': No such file or directory earlier in call
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}