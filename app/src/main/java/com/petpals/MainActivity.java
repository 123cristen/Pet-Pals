package com.petpals;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    String FILENAME = "pet_info";
    String file;
    String petName;

    AnimationDrawable palAnimation;
    AnimationDrawable foodAnimation;
    PixelTextView scoreView;

    boolean isPal;
    int score;

    private String getPetInformation() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput(FILENAME);
        } catch (FileNotFoundException e) {
            return null;
        }

        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String file = stringBuilder.toString();
        String[] values = file.split(",");
        petName = values[0];

        return file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPetInformation();
        Log.d("MainActivity", "Name: " + petName);

        // TODO: load from file before adding animation
        ImageView palImage = (ImageView) findViewById(R.id.pal_view);
        palImage.setBackgroundResource(R.drawable.pal_animation);
        palAnimation = (AnimationDrawable) palImage.getBackground();
        palAnimation.start();

        ImageView foodImage = (ImageView) findViewById(R.id.food_view);
        foodImage.setBackgroundResource(R.drawable.food_animation);
        foodAnimation = (AnimationDrawable) foodImage.getBackground();

        scoreView = (PixelTextView) findViewById(R.id.scoreboard);
        score = 0;
        scoreView.setText("0");

        isPal = false;
    }

    public void onSend(View v)
    {
        Toast.makeText(this, "Clicked on Send button", Toast.LENGTH_LONG).show();
    }

    public void onReceive(View v)
    {
        Toast.makeText(this, "Clicked on Receive button", Toast.LENGTH_LONG).show();
    }

    public void onFeed(View v)
    {
        // Toast.makeText(this, "Clicked on Feed button", Toast.LENGTH_LONG).show();

        if (isPal) {
            // feed them
            if (foodAnimation.isRunning()) {
                foodAnimation.stop();
            }

            foodAnimation.start();

            // TODO: do whatever with score if we want it.
            score++;
            Log.d("new score", String.valueOf(score));
            scoreView.setText(String.valueOf(score));
        }
        else{
            // create Pal
            Intent intent = new Intent(this, CreatePetActivity.class);
            startActivity(intent);
        }
    }
}
