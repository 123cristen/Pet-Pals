package com.petpals;

import android.content.Context;
import android.content.Intent;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    String FILENAME = "pet_info";
    String file;
    String petName;

    AnimationDrawable palAnimation;
    AnimationDrawable foodAnimation;
    PixelTextView scoreView;
    PixelButton b_left;
    PixelButton b_middle;
    PixelButton b_right;

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
        Log.d("MainActivity", "Name: " + petName);
        b_left = (PixelButton) findViewById(R.id.button_left);
        b_middle = (PixelButton) findViewById(R.id.button_middle);
        b_right = (PixelButton) findViewById(R.id.button_right);

        String files = getPetInformation();
        if (files == null || files == ""){
            isPal = false;
            b_left.setText("receive");
            b_middle.setText("poke");
            b_right.setText("create");
        }
        else {
            isPal = true;

            b_left.setText("send");
            b_middle.setText("poke");
            b_right.setText("feed");

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
        }
    }

    public void onButtonLeft(View v){
        if (isPal)  onSend(v);
        else        onReceive(v);
    }

    public void onButtonMiddle(View v){
        // TODO: insert some fun features
        ;
    }

    public void onButtonRight(View v){
        if (isPal)  onFeed(v);
        else        onPalCreate(v);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Store pet info
        Calendar calendar = Calendar.getInstance();
        long time = calendar.getTimeInMillis();

        String petInfoString = petName + "," + time;

        Log.d("INFO", petInfoString);

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fileOutputStream.write(petInfoString.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onSend(View v)
    {
        Toast.makeText(this, "Clicked on Send button", Toast.LENGTH_LONG).show();
    }

    public void onReceive(View v)
    {
        Toast.makeText(this, "Clicked on Receive button", Toast.LENGTH_LONG).show();
    }

    public void onPalCreate(View v){
        // create Pal
        Intent intent = new Intent(this, CreatePetActivity.class);
        startActivity(intent);
    }

    public void onFeed(View v)
    {
        // Toast.makeText(this, "Clicked on Feed button", Toast.LENGTH_LONG).show();
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
}
