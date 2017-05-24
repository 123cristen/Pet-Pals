package com.petpals;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    String FILENAME = "pet_info";
    String file;

    AnimationDrawable palAnimation;
    AnimationDrawable foodAnimation;
    PixelTextView scoreView;
    PixelButton b_left;
    PixelButton b_middle;
    PixelButton b_right;

    boolean isPal = false;
    String petName;
    long lastFed = 0;
    int score;
    int health = 0; // Max is 10

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

        if (values.length == 3) {
            isPal = true;
            petName = values[0];
            lastFed = Long.parseLong(values[1]);
            health = Integer.parseInt(values[2]);
        }

        return file;
    }

    private void updateHealth(boolean justFed) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        if (justFed) {
            lastFed = now;
            Intent intent = new Intent(this, HealthService.class);
            intent.putExtra("LAST_FED", lastFed);
            startService(intent);
        } else {
            int diff = (int) ((now - lastFed)/(1000)); // in seconds for testing
            //(int) ((now - lastFed)/(1000 * 60 * 60)); // in hours

            if (health - diff >= 0) {
                health = health - diff;
            } else {
                health = 0;
            }
        }
        updateDisplay();
    }

    private void updateDisplay() {
        if (isPal) {
            String newS = petName + "\n" + Integer.toString(health);
            scoreView.setText(newS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b_left = (PixelButton) findViewById(R.id.button_left);
        b_middle = (PixelButton) findViewById(R.id.button_middle);
        b_right = (PixelButton) findViewById(R.id.button_right);
        scoreView = (PixelTextView) findViewById(R.id.scoreboard);

        String files = "";
        Intent intent = getIntent();
        // From CreatePetActivity
        if (intent.getStringExtra("PET_NAME") != null) {
            isPal = true;
            petName = intent.getStringExtra("PET_NAME");
            health = 10;
            updateHealth(true);
        } else {
//            updateHealth(false);
            files = getPetInformation();
        }


        if (!isPal){
            b_left.setText("receive");
            b_middle.setText("poke");
            b_right.setText("create");
            b_left.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onReceive(v);
                }
            });

            b_middle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onPoke(v);
                }
            });

            b_right.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onPalCreate(v);
                }
            });
        }
        else {
            Log.d("File", files);

            updateDisplay();

            b_left.setText("send");
            b_middle.setText("poke");
            b_right.setText("feed");
            b_left.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onSend(v);
                }
            });

            b_middle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onPoke(v);
                }
            });

            b_right.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onFeed(v);
                }
            });

            ImageView palImage = (ImageView) findViewById(R.id.pal_view);
            palImage.setBackgroundResource(R.drawable.pal_animation);
            palAnimation = (AnimationDrawable) palImage.getBackground();
            palAnimation.start();

            ImageView foodImage = (ImageView) findViewById(R.id.food_view);
            foodImage.setBackgroundResource(R.drawable.food_animation);
            foodAnimation = (AnimationDrawable) foodImage.getBackground();
        }
    }

    public void onPoke (View v){
        // TODO: do something fun
    }
    @Override
    public void onResume() {

        super.onResume();

        if (isPal) {
            updateHealth(false);
            updateDisplay();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Toast.makeText(this, "onStop", Toast.LENGTH_LONG).show();

        // Store pet info
        String petInfoString = petName + "," + lastFed + "," + health;

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

        Intent intent = new Intent(this, HealthService.class);
        stopService(intent);
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
        finish();
    }

    public void onFeed(View v)
    {
        if (health < 10) {

            // feed them
            if (foodAnimation.isRunning()) {
                foodAnimation.stop();
            }

            foodAnimation.start();

            Calendar calendar = Calendar.getInstance();
            lastFed = calendar.getTimeInMillis();

            health++;
            updateHealth(true);
            updateDisplay();
        }
        else{
             Toast.makeText(this, petName + " is full!", Toast.LENGTH_SHORT).show();
        }
    }
}
