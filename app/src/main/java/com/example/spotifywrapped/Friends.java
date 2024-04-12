package com.example.spotifywrapped;

import android.app.AlertDialog;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.spotifywrapped.ui.friendsFragments.SectionsPagerAdapter;
import com.example.spotifywrapped.databinding.ActivityFriendsBinding;

public class Friends extends AppCompatActivity {

    private ActivityFriendsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton addFriend = binding.fab;


        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Implement adding friends


                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(Friends.this);
                builder.setTitle("Add Many Friend");

                View popView = getLayoutInflater().inflate(R.layout.add_friend_popup, null);
                EditText friendID = popView.findViewById(R.id.friendNameBox);
                Button addFriendBtn = popView.findViewById(R.id.addFriendButton);

                builder.setView(popView);
                AlertDialog myDialog = builder.create();
                myDialog.show();

                addFriendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String friendUserID = friendID.getText().toString();
                        //call any methods that use friendUserID

                        myDialog.dismiss();
                    }
                });


                //TODO Do whatever you need to do with friendID here


            }
        });
    }
}