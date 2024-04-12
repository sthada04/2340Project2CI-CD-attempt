package com.example.spotifywrapped;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.spotifywrapped.ui.wrappedfragments.WrappedFragment;
import com.example.spotifywrapped.MainActivity;


public class Wrapped extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrapped);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, WrappedFragment.newInstance())
                    .commitNow();
        }
    }
//
//    public void getTokenFunction(){
//        MainActivity.getToken();
//    }


}