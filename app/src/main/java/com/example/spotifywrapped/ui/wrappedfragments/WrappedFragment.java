package com.example.spotifywrapped.ui.wrappedfragments;

import androidx.lifecycle.ViewModelProvider;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spotifywrapped.R;

public class WrappedFragment extends Fragment {

    private WrappedViewModel mViewModel;

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


        return inflater.inflate(R.layout.fragment_wrapped, container, false);
    }

}