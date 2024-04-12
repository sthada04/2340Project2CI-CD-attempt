package com.example.spotifywrapped.ui.wrappedfragments;





import static android.content.ContentValues.TAG;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.example.spotifywrapped.MainActivity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.spotifywrapped.WrappedData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import com.example.spotifywrapped.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class WrappedFragment extends Fragment {


    public static final int AUTH_CODE_REQUEST_CODE = 1;
    public int step = 0;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;
    private FirebaseFirestore fStore;
    private String token, accessCode;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call mCall;


    private WrappedViewModel mViewModel;

    private WrappedData wrappedData = new WrappedData(Arrays.asList("Song1", "Song2", "Song3"), Arrays.asList("album1", "album2", "album3"), Arrays.asList("artist1", "artist2", "artist"), Arrays.asList("genre3", "genre1", "genre2"), Arrays.asList("genre1", "genre2", "genre3"), null, LocalDateTime.now().toString(), Arrays.asList("rec1", "rec2", "rec3"));

    public static WrappedFragment newInstance() {
        return new WrappedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WrappedViewModel.class);
        // TODO: Use the ViewModel
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_wrapped, container, false);

        TextView IntroHeaderText = rootView.findViewById(R.id.wrappedWelcomeText);
        ImageView profileImage = rootView.findViewById(R.id.profilePictureWelcome);
        Button continueBtn = rootView.findViewById(R.id.continue_btn);
        ViewGroup layout = rootView.findViewById(R.id.wrapped);
        ViewGroup layoutBtns = rootView.findViewById(R.id.buttonHolder);

        Context context = this.getContext();

        IntroHeaderText.setText("Welcome to Your Spotify Wrapped!");



        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        userId = user.getUid();

//        ImageView profilePic = binding.profileImg;
//        TextView profileNameTxt = binding.profileNameTxt;
//        TextView friendCodeTxt = binding.friendCodeTxt;

//        DocumentReference docRef = fStore.collection("users").document(userId);
//        docRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                if (document.exists()) {
//                    setTextAsync(document.getString("name"), IntroHeaderText);
////                    setTextAsync("Friend Code: " +document.getString("friendCode"), friendCodeTxt);
//                    if (document.contains("profilePicUrl")) {
//                        Picasso.get().load(Uri.parse(document.getString("profilePicUrl"))).into(profileImage);
//                    }
//                } else {
//                    Log.d("Doc", "No such document");
//                }
//            } else {
//                Log.d("Doc", "get failed with ", task.getException());
//            }
//        });
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        Log.d("Header text", IntroHeaderText.getText().toString());



//        ArrayList<WrappedData> wrappedDataList = loadPastWrapped();

        TextView headerText = new TextView(context);
        TextView bodyText = new TextView(context);
        int jil = 1;

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (step){
                    case (0):
                        layout.removeView(IntroHeaderText);
                        layout.removeView(profileImage);
                        step++;
                        // Create a new LinearLayout to contain the header and body text
                        LinearLayout newLayout = new LinearLayout(context);
                        newLayout.setHorizontalGravity(1);
                        newLayout.setOrientation(LinearLayout.VERTICAL);

                        // Create the TextView for the header text
                        ;
                        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        headerParams.topMargin = 250; // Convert dp to pixels
                        headerText.setLayoutParams(headerParams);
                        headerText.setText("Top 3 Songs");
                        headerText.setGravity(Gravity.CENTER_HORIZONTAL);
                        headerText.setTextSize(50); // Adjust text size
                        headerText.setId(0); // Set the ID for referencing later
                        newLayout.addView(headerText);

                        // Create the TextView for the body text
                        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        bodyParams.topMargin = 50; // Adjust as needed
                        bodyText.setLayoutParams(bodyParams);
//                        bodyText.setText(String.format("1. %s\n2. %s\n3. %s", "","",""));
                        bodyText.setText(String.format("1. %s\n2. %s\n3. %s", wrappedData.getSongNames().get(0),wrappedData.getSongNames().get(1),wrappedData.getSongNames().get(2)));
                        bodyText.setGravity(Gravity.CENTER_HORIZONTAL);
                        bodyText.setTextSize(60); // Adjust text size
                        bodyText.setId(1); // Set the ID for referencing later
                        newLayout.addView(bodyText);

                        // Set the layout parameters for the new LinearLayout
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        layoutParams.gravity = Gravity.CENTER; // Center the new LinearLayout horizontally
                        newLayout.setLayoutParams(layoutParams);

                        // Add the new LinearLayout to the existing layout
                        layout.addView(newLayout);

                        step++;



                        break;
                    case (1):
                        headerText.setText("Top 3 Artists");
//                        bodyText.setText(String.format("1. %s\n2. %s\n3. %s", "","",""));
                        bodyText.setText(String.format("1. %s\n2. %s\n3. %s", wrappedData.getArtistNames().get(0),wrappedData.getArtistNames().get(1),wrappedData.getArtistNames().get(2)));
                        step++;
                        break;
                    case (2):
                        headerText.setText("Top 3 Genre");
//                        bodyText.setText(String.format("1. %s\n2. %s\n3. %s", "","",""));
                        bodyText.setText(String.format("1. %s\n2. %s\n3. %s", wrappedData.getArtistNames().get(0),wrappedData.getArtistNames().get(1),wrappedData.getArtistNames().get(2)));
                        step++;
                        break;
                    case (3):
                        headerText.setText("Top 3 Albums");
//                        bodyText.setText(String.format("1. %s\n2. %s\n3. %s", "","",""));
                        bodyText.setText(String.format("1. %s\n2. %s\n3. %s", wrappedData.getAlbumNames().get(0),wrappedData.getAlbumNames().get(1),wrappedData.getAlbumNames().get(2)));
                        step++;
                        break;
                    case (4):
                        headerText.setText("Summary");
//                        bodyText.setText(String.format("1. %s\n2. %s\n3. %s", "","",""));
                        Random random = new Random();
                        int randomIndex = random.nextInt(wrappedData.getReccommended().size());

                        String rec = wrappedData.getReccommended().get(randomIndex);
                        bodyText.setTextSize(40);

                        bodyText.setText(String.format("Top Song: %s\nTop Artist: %s\nTop Genre: %s\nTop Album: %s\n\nReccommended Listen: %s\n", wrappedData.getSongNames().get(0),wrappedData.getArtistNames().get(0),wrappedData.getOrderedGenreNames().get(0), wrappedData.getAlbumNames().get(0), rec));
                        step++;





                        Button additionalButton = new Button(context);
                        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
//                        buttonParams.topMargin = 16; // Adjust as needed
//                        buttonParams.gravity = Gravity.CENTER_HORIZONTAL;
                        additionalButton.setLayoutParams(buttonParams);
                        additionalButton.setText("Export Image");
                        layoutBtns.addView(additionalButton); // Add the button to the layout

                        additionalButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d("E", "clicked");

                                rootView.setDrawingCacheEnabled(true);
                                Bitmap screenshot = Bitmap.createBitmap(rootView.getDrawingCache());
                                rootView.setDrawingCacheEnabled(false);

// Save the bitmap as an image file
                                File screenshotsDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Screenshots");
                                screenshotsDirectory.mkdirs(); // Create the directory if it doesn't exist
                                File imageFile = new File(screenshotsDirectory, "screenshot.png");
                                try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                                    screenshot.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

// Notify the media scanner to scan the saved image file
                                MediaScannerConnection.scanFile(context, new String[]{imageFile.getAbsolutePath()}, null, null);


                            }
                        });
                        break;
                    default:
//                        for (int j = 1; j < wrappedDataList.size(); j++){
//                        if (jil >= wrappedDataList.size()){
//                            break;
//                        }
//                        WrappedData wrappedDataCur = wrappedDataList.get(jil);
//                        jil++;
//                        bodyText.setText(String.format("Creates:%s\nTop Song: %s\nTop Artist: %s\nTop Genre: %s\nTop Album: %s\n", wrappedDataCur.getCreated().toString(), wrappedDataCur.getSongNames().get(0),wrappedDataCur.getArtistNames().get(0),wrappedDataCur.getOrderedGenreNames().get(0), wrappedDataCur.getAlbumNames().get(0)));

//                        }


                }

//                layout.removeView(continueBtn);

            }
        });



        //FIXME: No images are loading in
        Picasso.get().load(getProfileImage()).into(profileImage, new Callback() {
            @Override
            public void onSuccess() {
                // Image loaded successfully
            }

            @Override
            public void onError(Exception e) {
                // Log error
                Log.e("Picasso", "Error loading image: " + e.getMessage());
            }
        });
        return rootView;
    }

    private String getProfileImage() {
        //TODO: Get image from profile
//        (MainActivity).getToken();
//        if

        return "https://media.istockphoto.com/id/1337144146/vector/default-avatar-profile-icon-vector.jpg?s=612x612&w=0&k=20&c=BIbFwuv7FxTWvh5S3vB6bkT0Qv8Vn8N5Ffseq84ClGI=";
//        return "https://pyxis.nymag.com/v1/imgs/10e/009/494db51ff482ab739ac078e8a3ddc66482-jennifer-lopez.rsquare.w400.jpg";
    }

    private String getUserName() {
        //TODO: Get username from the profile

        return "JenniferLopez";
    }

    private void setTextAsync(final String text, TextView textView) {
        getActivity().runOnUiThread(() -> textView.setText(text));
    }


    private ArrayList<WrappedData> loadPastWrapped() {
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
        wrappedData = pastWraps.get(0);
        return pastWraps;
    }

}