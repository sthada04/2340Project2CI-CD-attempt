package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.spotifywrapped.ui.wrappedfragments.WrappedAI;
import com.example.spotifywrapped.ui.wrappedfragments.WrappedFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.example.spotifywrapped.WrappedData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String CLIENT_ID = "eb4c992fe84d4e0dbad8f1f198437b9d";
    public static final String REDIRECT_URI = "spotify-sdk://auth";
    public static final String CLIENT_SECRET = "14f5e2e940844ffd9d6dd627062250bd";

    public static final int AUTH_CODE_REQUEST_TOKEN = 0;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String refreshToken, token;
    private Call mCall;
    private TextView profileTextView;
    private FirebaseUser user;

    private FirebaseFirestore fStore;
    private String userId;
    private List<String> songNames = new ArrayList<>();

    private List<String> albumNames = new ArrayList<>();
    private List<String> artistNames = new ArrayList<>();

    private List<String> genreNames = new ArrayList<>();
    private List<String> orderedGenreNames = new ArrayList<>();
    private Map<String, Integer> genreCountMap = new HashMap<>();

    private List<String> reccommendedSongNames= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.example.spotifywrapped.databinding.ActivityMainBinding binding = com.example.spotifywrapped.databinding.ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the views
        profileTextView = binding.responseTextView;

        // Initialize the buttons
        Button actionBtn = binding.spotifyBtn;
        Button profileBtn = binding.profileBtn;
        Button wrappedBtn = binding.wrappedBtn;
        Button wrappedAI = binding.wrappedAi;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Button friendsListBtn = findViewById(R.id.friends_btn);
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
                    refreshToken = document.getString("refreshToken");
                    if (refreshToken != null) {
                        getToken();
                    }
                } else {
                    Log.d(TAG, "No sucddh document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });


        //Button Click Listeners
        friendsListBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
            startActivity(intent);
            finish();
        });


        actionBtn.setOnClickListener((v) -> {
            onGetUserProfileClicked();
        });

        profileBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra("token", token);
            startActivity(intent);
            finish();
        });

        wrappedBtn.setOnClickListener((v) -> {
//            onGetUserTopSongs(0);
            onGenerateWrappedClicked();



            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            Intent intent = new Intent(getApplicationContext(), WrappedActivity.class);
            startActivity(intent);
            finish();
        });

        wrappedAI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WrappedData wrappedData = new WrappedData(songNames, albumNames, artistNames, genreNames, orderedGenreNames, genreCountMap, LocalDateTime.now().toString(), reccommendedSongNames);
                WrappedActivity.processData(wrappedData);

                if (savedInstanceState == null) {
                    // Create a new instance of WrappedAI fragment
                    WrappedAI wrappedAIFragment = new WrappedAI();

                    // Get the FragmentManager
                    FragmentManager fragmentManager = getSupportFragmentManager();

                    // Begin the transaction
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    // Replace the fragment container with the WrappedAI fragment
                    fragmentTransaction.replace(R.id.fragment_container, wrappedAIFragment);

                    // Add the transaction to the back stack
                    fragmentTransaction.addToBackStack(null);

                    // Commit the transaction
                    fragmentTransaction.commit();
                }

            }
        });

    }

    public void getToken() {
        final Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()))
                .post(new FormBody.Builder()
                        .add("grant_type", "refresh_token")
                        .add("refresh_token", refreshToken)
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
                    refreshToken = jsonObject.getString("refresh_token");
                    DocumentReference documentReference = fStore.collection("users").document(userId);
                    documentReference.update("refreshToken", refreshToken).addOnSuccessListener(v -> {
                        Log.d(TAG, "onSuccess: Spotify refresh token added to " + userId);
                    }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }


    /**
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        // Check which request code is present (if any)
        if (AUTH_CODE_REQUEST_TOKEN == requestCode) {
            token = response.getAccessToken();
        }
    }

    public void onGenerateWrappedClicked() {
        Log.d("GenerateCLicked", "GenerateWrapped Clicked\n");
        onGetUserTopSongs(0);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onGetRecommended();
        WrappedData wrappedData = new WrappedData(songNames, albumNames, artistNames, genreNames, orderedGenreNames, genreCountMap, LocalDateTime.now().toString(), reccommendedSongNames);
        WrappedActivity.processData(wrappedData);
        saveWrapped(wrappedData);
    }

    public void onGetUserProfileClicked() {
        ArrayList<WrappedData> da = loadPastWrapped();
        Log.d("info", String.valueOf(da.size()));
        for (WrappedData wd : da){
            Log.d("info", wd.getSongNames().get(0));
        }
//        onGenerateWrappedClicked();
//        Log.d("ProfileCLicked", "onGetUserProfile clicked\n");
//        onGetUserTopSongs(0);
    }
    public void sample(){
        if (token == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
//            getToken();
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


    public void onGetUserTopSongs(int offset) {
        if (token == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url(String.format("https://api.spotify.com/v1/me/top/tracks?time_range=long_term&limit=50&offset=%d", offset))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
//                Toast.makeText(MainActivity.this, "Failed to fetch data, watch Logcat for more details",
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBodyString = response.body().string();
                    extractSongData(responseBodyString);
//                    Log.d("body message: ", responseBodyString);
                    final JSONObject jsonObject = new JSONObject();
                    setTextAsync(jsonObject.toString(3), profileTextView);
//                    Log.d("JSON to string for get songs", jsonObject.toString());
                    onGetUserTopArtists();

                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

//        if (offset == 0){
//            onGetUserTopSongs(50);
//        }
    }

    public void extractSongData(String responseData){
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray itemsArray = jsonObject.getJSONArray("items");

//            List<String> songNames = new ArrayList<>();
//            List<String> artistNames = new ArrayList<>();
//            List<String> genreNames = new ArrayList<>();
//            List<String> songNames = new ArrayList<>();
//
//            List<String> albumNames = new ArrayList<>();

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                String songName = item.getString("name");


//                JSONArray artistsArray = item.getJSONArray("artists");
//                StringBuilder artistNamesBuilder = new StringBuilder();


//                for (int j = 0; j < artistsArray.length(); j++) {
//                    JSONObject artist = artistsArray.getJSONObject(j);
//                    String artistName = artist.getString("name");
//                    if (j > 0) {
//                        artistNamesBuilder.append(", ");
//                    }
//                    artistNamesBuilder.append(artistName);
//                }
//                artistNames.add(artistNamesBuilder.toString());

                JSONObject albumObject = item.getJSONObject("album");
                String albumName = albumObject.getString("name");
                albumNames.add(albumName);

                songNames.add(songName);


//                JSONArray genresArray = item.getJSONArray("genres");
//                StringBuilder genresBuilder = new StringBuilder();
//                for (int k = 0; k < genresArray.length(); k++) {
//                    if (k > 0) {
//                        genresBuilder.append(", ");
//                    }
//                    genresBuilder.append(genresArray.getString(k));
//                }
//                genreNames.add(genresBuilder.toString());


            }


            for (int i = 0; i < songNames.size(); i++) {
                String songName = songNames.get(i);
//                String artistName = artistNames.get(i);
                String albumName = albumNames.get(i);
//                String genre = genreNames.get(i);

                Log.d("Song Name", songName);
//                Log.d("Artist Name", artistName);

                if (albumName.equals(songName)){
                    albumNames.set(i,"");
                    Log.d("Album Name", "no album");
                } else {
                    Log.d("Album Name", albumName);
                }
//                Log.d("Genres", genre);
            }


            // Now you have a list of song names
//            for (String songName : songNames) {
//                Log.d("Song Name", songName);
//            }
////
//            for (String artistName : artistNames) {
//                Log.d("Artist Name", artistName);
//            }

            // If you want to do something with the list, you can pass it to another function or manipulate it here

        } catch (Exception e) {
            Log.e("Extract Songs", "Error extracting songs: " + e.getMessage());
        }
    }


    public void onGetUserTopArtists() {
        if (token == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?time_range=long_term&limit=50&offset=0")
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
                    String responseBodyString = response.body().string();
                    extractArtistData(responseBodyString);
//                    Log.d("body message: ", responseBodyString);
                    final JSONObject jsonObject = new JSONObject();
                    setTextAsync(jsonObject.toString(3), profileTextView);
//                    Log.d("JSON to string for get songs", jsonObject.toString());
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



    public void extractArtistData(String responseData){
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray itemsArray = jsonObject.getJSONArray("items");

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                String artistName = item.getString("name");


                JSONArray genreObject = item.getJSONArray("genres");
                Log.d("album object", genreObject.toString());
                for (int j = 0; j < genreObject.length(); j++){
                    genreNames.add(genreObject.get(j).toString());
                }
//                String albumName = albumObject.getString("name");
//                albumNames.add(albumName);

                artistNames.add(artistName);




            }


            for (int i = 0; i < genreNames.size(); i++) {
                String songName = genreNames.get(i);

                Log.d("Song Name", songName);
            }

            for (String i : artistNames){
                Log.d("Artist name", i);
            }

            int maxGenreCount = 0;

            for (String str : genreNames) {
                genreCountMap.put(str, genreCountMap.getOrDefault(str, 0) + 1);
                if (genreCountMap.get(str) > maxGenreCount){
                    maxGenreCount = genreCountMap.get(str);
                }
            }

            for (Map.Entry<String, Integer> entry : genreCountMap.entrySet()) {
                Log.d("Map array values", entry.getKey() + " -> " + entry.getValue());
            }

            for (int i = maxGenreCount; i >= 0; i--){
                for (Map.Entry<String, Integer> entry : genreCountMap.entrySet()) {
                    if (entry.getValue() == i){
                        orderedGenreNames.add(entry.getKey());
                    }
                }
            }

            for (String x : orderedGenreNames){
                Log.d("ordered genre", x);
            }


        } catch (Exception e) {
            Log.e("Extract Songs", "Error extracting songs: " + e.getMessage());
        }
    }


    private void saveWrapped(WrappedData wrap) {
        fStore.collection("users").document(userId).collection("pastWraps").document(wrap.getCreated()).set(wrap);
    }

    private ArrayList<WrappedData> loadPastWrapped() {
        Log.d("loading", "past wrapped");
        ArrayList<WrappedData> pastWraps = new ArrayList<>();
        fStore.collection("users").document(userId).collection("pastWraps")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            WrappedData wrap = documentSnapshot.toObject(WrappedData.class);
                            pastWraps.add(wrap);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        return pastWraps;
    }





    public void onGetRecommended() {
        if (token == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }


        String urlString = String.format("https://api.spotify.com/v1/recommendations?limit=10&seed_genres=%s%%2C%s%%2C%s%%2C%s%%2C%s", orderedGenreNames.get(0),  orderedGenreNames.get(1),  orderedGenreNames.get(2),  orderedGenreNames.get(3),  orderedGenreNames.get(4));

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url(urlString)
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
                    String responseBodyString = response.body().string();
                    extractTrackData(responseBodyString);
//                    Log.d("body message: ", responseBodyString);
                    final JSONObject jsonObject = new JSONObject();
                    setTextAsync(jsonObject.toString(3), profileTextView);
//                    Log.d("JSON to string for get songs", jsonObject.toString());
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




    public void extractTrackData(String responseData){
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray itemsArray = jsonObject.getJSONArray("tracks");

//            List<String> songNames = new ArrayList<>();
//            List<String> artistNames = new ArrayList<>();
//            List<String> genreNames = new ArrayList<>();
//            List<String> songNames = new ArrayList<>();
//
//            List<String> albumNames = new ArrayList<>();

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                String songName = item.getString("name");


//                JSONArray artistsArray = item.getJSONArray("artists");
//                StringBuilder artistNamesBuilder = new StringBuilder();


//                for (int j = 0; j < artistsArray.length(); j++) {
//                    JSONObject artist = artistsArray.getJSONObject(j);
//                    String artistName = artist.getString("name");
//                    if (j > 0) {
//                        artistNamesBuilder.append(", ");
//                    }
//                    artistNamesBuilder.append(artistName);
//                }
//                artistNames.add(artistNamesBuilder.toString());

                reccommendedSongNames.add(songName);


//                JSONArray genresArray = item.getJSONArray("genres");
//                StringBuilder genresBuilder = new StringBuilder();
//                for (int k = 0; k < genresArray.length(); k++) {
//                    if (k > 0) {
//                        genresBuilder.append(", ");
//                    }
//                    genresBuilder.append(genresArray.getString(k));
//                }
//                genreNames.add(genresBuilder.toString());


            }


            for (int i = 0; i < reccommendedSongNames.size(); i++) {
                String songName = reccommendedSongNames.get(i);
//                String artistName = artistNames.get(i);

                Log.d("Song Name Reccomended", songName);
//                Log.d("Artist Name", artistName);

//                Log.d("Genres", genre);
            }


            // Now you have a list of song names
//            for (String songName : songNames) {
//                Log.d("Song Name", songName);
//            }
////
//            for (String artistName : artistNames) {
//                Log.d("Artist Name", artistName);
//            }

            // If you want to do something with the list, you can pass it to another function or manipulate it here

        } catch (Exception e) {
            Log.e("Extract Songs", "Error extracting songs: " + e.getMessage());
        }
    }


}


