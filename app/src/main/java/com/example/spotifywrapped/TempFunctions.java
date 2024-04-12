package com.example.spotifywrapped;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class TempFunctions {

    //NOTE: THIS CLASS WILL BE TEMPORARY AND SHOULD BE MERGED WITH ANOTHER CLASS LATER

    public Bitmap generateImage(String[] userArtists, String[] userTracks){
        Bitmap userImage = Bitmap.createBitmap(500, 800, Bitmap.Config.ALPHA_8);
        //idk what config to use, but it does change what color system it is?

        Canvas canvas = new Canvas(userImage);

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setTextSize(40);
        int yPos = 30;
        canvas.drawText("Spotify In A Glance", 50, yPos, paint);

        paint.setTextSize(30);
        yPos = 70;
        canvas.drawText("Your Top Artists", 50, yPos, paint);
        yPos += 40;
        for (int i = 1; i < 6; i++) {
            canvas.drawText(i + ". " + userArtists[i], 50, yPos, paint);
            yPos += 40;
        }

        canvas.drawText("Your Top Tracks", 50, yPos, paint);
        for (int i = 1; i < 6; i++) {
            canvas.drawText(i + ". " + userTracks[i], 50, yPos, paint);
            yPos += 40;
        }
        //userImage.compress(Bitmap.CompressFormat.PNG, 100, );



        return userImage;

    }



}
