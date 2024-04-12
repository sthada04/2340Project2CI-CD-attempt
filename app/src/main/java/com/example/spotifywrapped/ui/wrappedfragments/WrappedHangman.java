package com.example.spotifywrapped.ui.wrappedfragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.spotifywrapped.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WrappedHangman extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MAX_CHANCES = 5;

    private String mParam1;
    private String mParam2;
    private String wordToGuess;
    private List<Character> guessedLetters;
    private int chancesLeft;

    private TextView guessedWordTextView;
    private TextView gameResultsTextView;
    private TextInputEditText guessInputEditText;

    public WrappedHangman() {
        // Required empty public constructor
    }

    public static WrappedHangman newInstance(String param1, String param2) {
        WrappedHangman fragment = new WrappedHangman();
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
        wordToGuess = "example"; // Default word to guess
        guessedLetters = new ArrayList<>();
        chancesLeft = MAX_CHANCES;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wrapped_hangman, container, false);

        guessedWordTextView = rootView.findViewById(R.id.guessedWord);
        gameResultsTextView = rootView.findViewById(R.id.gameResults);
        guessInputEditText = rootView.findViewById(R.id.input_guess);

        updateGuessedWordDisplay();

        rootView.findViewById(R.id.guessButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String guess = guessInputEditText.getText().toString().toLowerCase().trim();
                if (!TextUtils.isEmpty(guess) && guess.length() == 1) {
                    char guessedChar = guess.charAt(0);
                    if (!guessedLetters.contains(guessedChar)) {
                        guessedLetters.add(guessedChar);
                        if (!wordToGuess.contains(guess)) {
                            chancesLeft--;
                            updateLivesDisplay(rootView);
                        }
                        updateGuessedWordDisplay();
                        checkGameEnd();
                    } else {
                        Toast.makeText(getContext(), "You've already guessed this letter!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter a single letter guess!", Toast.LENGTH_SHORT).show();
                }
                guessInputEditText.getText().clear();
            }
        });

        return rootView;
    }

    private void updateGuessedWordDisplay() {
        char[] guessedWord = new char[wordToGuess.length()];
        Arrays.fill(guessedWord, '_');
        for (char c : guessedLetters) {
            for (int i = 0; i < wordToGuess.length(); i++) {
                if (wordToGuess.charAt(i) == c) {
                    guessedWord[i] = c;
                }
            }
        }
        guessedWordTextView.setText(String.valueOf(guessedWord));
    }

    private void updateLivesDisplay(View rootView) {
        int visibleChips = MAX_CHANCES - chancesLeft;
        switch (visibleChips) {
            case 1:
                rootView.findViewById(R.id.chip).setVisibility(View.VISIBLE);
                break;
            case 2:
                rootView.findViewById(R.id.chip2).setVisibility(View.VISIBLE);
                break;
            case 3:
                rootView.findViewById(R.id.chip3).setVisibility(View.VISIBLE);
                break;
            case 4:
                rootView.findViewById(R.id.chip4).setVisibility(View.VISIBLE);
                break;
            case 5:
                rootView.findViewById(R.id.chip5).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void checkGameEnd() {
        if (chancesLeft == 0) {
            gameResultsTextView.setText("Sorry, you failed :(");
            gameResultsTextView.setVisibility(View.VISIBLE);
        } else if (!guessedWordTextView.getText().toString().contains("_")) {
            gameResultsTextView.setText("Congratulations, you won!");
            gameResultsTextView.setVisibility(View.VISIBLE);
        }
    }
}




//String wordToGuess = WrappedActivity.curData.getArtistNames().get(0);