package com.example.spotifywrapped;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.spotifywrapped.databinding.ActivityWrappedBinding;
import com.example.spotifywrapped.ui.wrappedfragments.WrappedFragment;


public class WrappedActivity extends AppCompatActivity {

    public static WrappedData curData;

    public static void processData(WrappedData cur) {
        curData = cur;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrapped);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, WrappedFragment.newInstance())
                    .commitNow();
        }
//        ActivityWrappedBinding binding = ActivityWrappedBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//
////        ConstraintSet.Layout l =
//
//        Button continueBtn = findViewById(R.id.wrapped);
//
//
//        continueBtn.setOnClickListener((v) -> {
//            Log.d("Continue On CLick", "continue clicked");
//        });


    }
//
//    public void getTokenFunction(){
//        MainActivity.getToken();
//    }


}