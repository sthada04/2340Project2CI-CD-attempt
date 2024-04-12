package com.example.spotifywrapped.ui.friendsFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.spotifywrapped.databinding.FragmentFriendsBinding;

/**
 * A placeholder fragment containing a simple view.
 */
public class FriendsListFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private FriendsPageViewModel friendsPageViewModel;
    private FragmentFriendsBinding binding;

    public static FriendsListFragment newInstance(int index) {
        FriendsListFragment fragment = new FriendsListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendsPageViewModel = new ViewModelProvider(this).get(FriendsPageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        friendsPageViewModel.setIndex(index);



        //((ImageButton) view).setImageResource(R.drawable.icon2);


    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageButton editFriendship = binding.editFriendshipButton;

        final TextView textView = binding.sectionLabel;
        friendsPageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}