package com.example.spotifywrapped.ui.wrappedfragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.spotifywrapped.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WrappedGuessWhich#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WrappedGuessWhich extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<String> stringsList;
    private int correctIndex;
    private EditText editTextGuess;
    private TextView textViewResult;

    public WrappedGuessWhich() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WrappedGuessWhich.
     */
    // TODO: Rename and change types and number of parameters
    public static WrappedGuessWhich newInstance(String param1, String param2) {
        WrappedGuessWhich fragment = new WrappedGuessWhich();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wrapped_guess_which, container, false);
    }
}

/*
package com.example.yourpackage.ui.guessgame;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.yourpackage.R;

import java.util.ArrayList;
import java.util.Random;

public class GuessGameFragment extends Fragment {

    private ArrayList<String> stringsList;
    private int correctIndex;
    private EditText editTextGuess;
    private TextView textViewResult;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_guess_game, container, false);

        // Sample list of strings (replace with your actual list)
        stringsList = new ArrayList<>();
        stringsList.add("Apple");
        stringsList.add("Banana");
        stringsList.add("Orange");
        stringsList.add("Grapes");

        // Generate a random index for the correct string
        Random random = new Random();
        correctIndex = random.nextInt(stringsList.size());

        editTextGuess = root.findViewById(R.id.editTextGuess);
        textViewResult = root.findViewById(R.id.textViewResult);

        Button buttonGuess = root.findViewById(R.id.buttonGuess);
        buttonGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGuess();
            }
        });

        return root;
    }

    private void checkGuess() {
        try {
            int guessedIndex = Integer.parseInt(editTextGuess.getText().toString());
            if (guessedIndex < 0 || guessedIndex >= stringsList.size()) {
                textViewResult.setText("Please enter a valid index between 0 and " + (stringsList.size() - 1));
            } else {
                if (guessedIndex == correctIndex) {
                    textViewResult.setText("Congratulations! You guessed it right.");
                } else {
                    textViewResult.setText("Wrong guess. Try again!");
                }
            }
        } catch (NumberFormatException e) {
            textViewResult.setText("Please enter a valid number.");
        }
    }
}

 */