package com.petpals;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
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
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    String FILENAME = "pet_info";
    String file;

    AnimationDrawable palAnimation;
    AnimationDrawable foodAnimation;
    PixelTextView scoreView;
    PixelButton b_left;
    PixelButton b_middle;
    PixelButton b_right;

    boolean isPal;
    String petName;
    long lastFed = 0;
    boolean justFed = false;
    int score;
    int health = 0; // Max is 10

    private final static int INTERVAL = 1000 * 10; // 1000 * 60 * 60;
    Timer timer = new Timer ();
    TimerTask hourlyTask = new TimerTask () {
        @Override
        public void run () {
            Log.d("Health", "Decrease Health");

            if (health > 0) {
                health--;
                handler.obtainMessage(1).sendToTarget();
            }
        }
    };

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            displayHealth();
        }
    };

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
            petName = values[0];
            lastFed = Long.parseLong(values[1]);
            health = Integer.parseInt(values[2]);
        }

        return file;
    }

    private void updateHealth() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        if (justFed) {
            lastFed = now;
        } else {
            int diff = (int) ((now - lastFed) / (1000)); // in seconds for testing
            //(int) ((now - lastFed)/(1000 * 60 * 60)); // in hours

            if (health - diff >= 0) {
                health = health - diff;
            } else {
                health = 0;
            }
        }
    }

    private void displayHealth() {
        scoreView = (PixelTextView) findViewById(R.id.scoreboard);
        scoreView.setText(Integer.toString(health));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b_left = (PixelButton) findViewById(R.id.button_left);
        b_middle = (PixelButton) findViewById(R.id.button_middle);
        b_right = (PixelButton) findViewById(R.id.button_right);

        String files = getPetInformation();

        Intent intent = getIntent();
        // From CreatePetActivity
        if (intent.getStringExtra("PET_NAME") != null || files != null) {
            isPal = true;

            if (intent.getStringExtra("PET_NAME") != null) {
                petName = intent.getStringExtra("PET_NAME");
                health = 10;
                justFed = true;
            }

            Log.d("OnCreate", "Pet Name: " + petName);

            updateHealth();
            displayHealth();
            timer.scheduleAtFixedRate(hourlyTask, INTERVAL, INTERVAL);
            Log.d("Health", "Scheduled");

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
        } else if (files == null){
            isPal = false;
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
        }
    }

    public void onPoke (View v){
        // TODO: do something fun
    }
    @Override
    public void onResume() {
        super.onResume();

        if (isPal) {
            updateHealth();
            displayHealth();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

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
        if (health < 10) {
            timer.cancel();
            // Toast.makeText(this, "Clicked on Feed button", Toast.LENGTH_LONG).show();
            // feed them
            if (foodAnimation.isRunning()) {
                foodAnimation.stop();
            }

            foodAnimation.start();

            Calendar calendar = Calendar.getInstance();
            lastFed = calendar.getTimeInMillis();

            health++;
            justFed = true;
            Log.d("Fed", "Health: " + health);

            updateHealth();
            displayHealth();
        }

        Log.d("Fed", "Health: " + health);
    }
}
