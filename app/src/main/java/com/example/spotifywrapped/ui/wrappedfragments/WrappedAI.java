package com.example.spotifywrapped.ui.wrappedfragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.spotifywrapped.R;
import com.example.spotifywrapped.WrappedActivity;
import com.example.spotifywrapped.WrappedData;

import java.lang.ref.WeakReference;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WrappedAI#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WrappedAI extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public WrappedAI() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WrappedAI.
     */
    // TODO: Rename and change types and number of parameters
    public static WrappedAI newInstance(String param1, String param2) {
        WrappedAI fragment = new WrappedAI();
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
        View rootView = inflater.inflate(R.layout.fragment_wrapped_ai, container, false);

        TextView aiResponse = rootView.findViewById(R.id.AIResponseText);

        // Start AsyncTask to perform network operation
        new LLMApiTask(aiResponse, WrappedActivity.curData).execute();

        return rootView;
    }

    private static class LLMApiTask extends AsyncTask<Void, Void, String> {
        private WeakReference<TextView> aiResponseRef;
        private WrappedData userData;

        LLMApiTask(TextView aiResponse, WrappedData userData) {
            this.aiResponseRef = new WeakReference<>(aiResponse);
            this.userData = userData;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = "Generating...";

            // Initialize your model
            ChatLanguageModel model2 = HuggingFaceChatModel.withAccessToken("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");

            // Build prompt string
            String prompt = "Imagine you’re describing the fashion style of a user. This user's favorite albums are: ";
            for (String album : userData.getAlbumNames()) {
                prompt += album + ", ";
            }
            prompt += ", and the user's favorite songs are: ";
            for (String artist : userData.getArtistNames()) {
                prompt += artist + ", ";
            }
            prompt += ". How would this person dress? What colors, fabrics, and accessories would they choose? Write a detailed description of their outfit, capturing their unique personality and the emotions evoked by their favorite music.";

            // Generate response asynchronously
            response = model2.generate(prompt);

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            TextView aiResponse = aiResponseRef.get();
            if (aiResponse != null) {
                aiResponse.setText(response);
            }
        }
    }


    public String llmCall(WrappedData userData) {
        String response = "Generating...";
        //OpenAiChatModel model = OpenAiChatModel.withApiKey(apiKey); --> works for OpenAI

        //NOTE: If you want to use a different type of model, you need to add the dependency in build.gradle.kts
        ChatLanguageModel model2 = HuggingFaceChatModel.withAccessToken("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");
        //hugging face key: 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
        //gemini key: AIzaSyBKW-wrvMw7LmQUqWLxKa04w_5fVyQbhoY




        String prompt = "Imagine you’re describing the fashion style of a user. This user's favorite albums are: ";

        for (String album : userData.getAlbumNames()) {
            prompt += album + ", ";
        }

        prompt += ", and the user's favorite songs are: ";

        for (String artist : userData.getArtistNames()) {
            prompt += artist + ", ";
        }

        prompt += ". How would this person dress? What colors, fabrics, and accessories would they choose? Write a detailed description of their outfit, capturing their unique personality and the emotions evoked by their favorite music.";


        response = model2.generate(prompt);


        return response;
    }

}