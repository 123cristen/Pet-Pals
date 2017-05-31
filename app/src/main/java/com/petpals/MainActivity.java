package com.petpals;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Handler;
import android.os.Message;
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
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Bluetooth mBluetooth;

    String FILENAME = "pet_info";
    String file;

    AnimationDrawable foodAnimation;
    LevelListDrawable palLevelAnimation;
    PixelTextView scoreView;
    PixelButton b_left;
    PixelButton b_middle;
    PixelButton b_right;

    boolean isPal = false;
    String petName;
    long lastFed = 0;
    int health = 0; // Max is 10
    ImageView palImageView;

    private final static int INTERVAL = 1000 * 10; // 1000 * 60 * 60;
    Timer timer;
    TimerTask hourlyTask;

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            updateDisplay();
        }
    };

    private void initializePetfromString(String petInformation) {
        String[] values = petInformation.split(",");

        if (values.length == 3) {
            isPal = true;
            petName = values[0];
            lastFed = Long.parseLong(values[1]);
            health = Integer.parseInt(values[2]);
        }
    }

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
        initializePetfromString(file);

        return file;
    }

    private String getPetInformationString() {
        return petName + ","
                + Long.toString(lastFed) + ","
                + Integer.toString(health);
    }

    private void updateHealth(boolean justFed) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        if (justFed) {
            lastFed = now;
        } else {
            int diff = (int) ((now - lastFed) / INTERVAL);
            if (health - diff >= 0) {
                health = health - diff;
                Log.d("Health", "Diff: " + diff);
            } else {
                health = 0;
            }
        }
    }

    /* updateDisplay():
     * Change Pal appearance based on currently health status.
     * Update top right text
     */
    private void updateDisplay() {
        if (isPal) {
            palImageView.setImageLevel(health);

            Drawable current = palLevelAnimation.getCurrent();
            ((AnimationDrawable)current).start();

            String newS = petName + "\n" + Integer.toString(health);
            scoreView.setText(newS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scoreView = (PixelTextView) findViewById(R.id.scoreboard);
        b_left = (PixelButton) findViewById(R.id.button_left);
        b_middle = (PixelButton) findViewById(R.id.button_middle);
        b_right = (PixelButton) findViewById(R.id.button_right);
        String files = "";
        Intent intent = getIntent();
        // From CreatePetActivity
        if (intent.getStringExtra("PET_NAME") != null) {
            isPal = true;
            petName = intent.getStringExtra("PET_NAME");
            health = 10;
            updateHealth(true);
        } else {
            files = getPetInformation();
            updateHealth(false);
        }

        
        mBluetooth = new Bluetooth(this);

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

            // setup graphics
            palImageView = (ImageView) findViewById(R.id.pal_view);
            palLevelAnimation = (LevelListDrawable) palImageView.getDrawable();
            ImageView foodImage = (ImageView) findViewById(R.id.food_view);
            foodImage.setBackgroundResource(R.drawable.food_animation);
            foodAnimation = (AnimationDrawable) foodImage.getBackground();

            updateDisplay();
        }
    }

    public void onPoke (View v){
        // TODO: do something fun
        float x = palImageView.getX();
        float y = palImageView.getY();
        int height = ((View) palImageView.getParent()).getHeight();
        int width = ((View) palImageView.getParent()).getWidth();


    }
    @Override
    public void onResume() {
        super.onResume();

        if (isPal) {
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

            timer = new Timer();
            timer.scheduleAtFixedRate(hourlyTask, INTERVAL, INTERVAL);
            Log.d("TIMER", "Restart timer");
        }

        if (isPal) {
            updateHealth(false);
            updateDisplay();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        timer.cancel();
        Log.d("TIMER", "Cancelled");

        Toast.makeText(this, "onStop", Toast.LENGTH_LONG).show();

        // Store pet info
        String petInfoString = getPetInformationString();

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
        mBluetooth.sendBluetooth();
        Toast.makeText(this, "Clicked on Send button", Toast.LENGTH_LONG).show();
    }

    public void onReceive(View v)
    {
        mBluetooth.receiveBluetooth();
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

            Log.d("Fed", "Health: " + health);

            updateHealth(true);
            updateDisplay();
        }
        else{
             Toast.makeText(this, petName + " is full!", Toast.LENGTH_SHORT).show();
        }

        Log.d("Fed", "Health: " + health);
    }
}
