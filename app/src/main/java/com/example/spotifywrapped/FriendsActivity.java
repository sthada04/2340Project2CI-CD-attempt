package com.example.spotifywrapped;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.spotifywrapped.databinding.ActivityFriendsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private ActivityFriendsBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private ArrayList<String> friends;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        FloatingActionButton addFriend = binding.fab;
        Button backBtn = binding.backBtn;
        View view = binding.getRoot();
        friends = new ArrayList<>();

        DocumentReference docRef = fStore.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    friends.addAll((List<String>) document.get("friends"));
                    for (String friendId: friends) {
                        createFriendWidget(view, friendId);
                    }
                } else {
                    Log.d("Doc", "No such document");
                }
            } else {
                Log.d("Doc", "get failed with ", task.getException());
            }
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        addFriend.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
            builder.setTitle("Add Friend");

            View popView = getLayoutInflater().inflate(R.layout.add_friend_popup, null);
            EditText friendID = popView.findViewById(R.id.friendNameBox);
            Button addFriendBtn = popView.findViewById(R.id.addFriendButton);

            builder.setView(popView);
            AlertDialog myDialog = builder.create();
            myDialog.show();

            addFriendBtn.setOnClickListener(v1 -> {
                String friendIDString = friendID.getText().toString();
                //call any methods that use friendUserID
                fStore.collection("users")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String friendUserId = document.getId();
                                    if (friendIDString.equals(document.getString("friendCode"))) {
                                        DocumentReference currUser = fStore.collection("users").document(userId);
                                        currUser.update("friends", FieldValue.arrayUnion(friendUserId)).addOnSuccessListener(r -> {
                                            Log.d(TAG, "onSuccess: Friend Code " + friendUserId);
                                        }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));

                                        DocumentReference friendUser = fStore.collection("users").document(friendUserId);
                                        friendUser.update("friends", FieldValue.arrayUnion(userId)).addOnSuccessListener(r -> {
                                            Log.d(TAG, "onSuccess: Friend Code " + friendUser);
                                        }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));
                                        createFriendWidget(view, friendUserId);
                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        });
                myDialog.dismiss();
            });
        });
    }

    private void removeFriend(String friendId) {
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        friends.remove(friendId);

        DocumentReference currUser = fStore.collection("users").document(userId);
        currUser.update("friends", FieldValue.arrayRemove(friendId)).addOnSuccessListener(r -> {
            Log.d("Doc", "onSuccess: Friend Code Removed " + friendId);
        }).addOnFailureListener(e -> Log.d("Doc", "onFailure: " + e));

        DocumentReference friendUser = fStore.collection("users").document(friendId);
        friendUser.update("friends", FieldValue.arrayRemove(userId)).addOnSuccessListener(r -> {
            Log.d("Doc", "onSuccess: Friend Code Removed " + friendUser);
        }).addOnFailureListener(e -> Log.d("Doc", "onFailure: " + e));
    }

    private void createFriendWidget(View view, String friendId) {
        View friendFragment = getLayoutInflater().inflate(R.layout.fragment_friends, null);
        friendFragment.setTag(friendId);
        LinearLayout container = view.findViewById(R.id.LinearContainer);
        TextView name = friendFragment.findViewById(R.id.friend_Name);
        ImageView profilePic = friendFragment.findViewById(R.id.profile_img);

        DocumentReference friendUser = fStore.collection("users").document(friendId);
        friendUser.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    name.setText(document.getString("name"));
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
        ImageButton removeFriendBtn = friendFragment.findViewById(R.id.editFriendshipButton);
        removeFriendBtn.setOnClickListener(v -> {
            removeFriend(friendId);
            container.removeView(friendFragment);
        });
        friendFragment.setOnClickListener(v -> {
            //TODO Show friend profile on click
        });
        container.addView(friendFragment);
    }
}