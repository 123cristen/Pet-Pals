package com.petpals;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTED = 6;


    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private EditText mOutEditText;
    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the service
    private BluetoothService mService = null;

    public int counter = 0;

    // will be set to 1 once connected to a device
    private int shouldSend = 0;

    String FILENAME = "pet_info";
    String file;

    AnimationDrawable foodAnimation;
    LevelListDrawable palLevelAnimation;
    PixelTextView scoreView;
    PixelButton b_left;
    PixelButton b_middle;
    PixelButton b_right;

    boolean isPal = false;
    boolean isMoving = false;
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

    private void setisPal(boolean b){
        if(b){
            isPal = true;
            palImageView.setVisibility(View.VISIBLE);
            scoreView.setVisibility(View.VISIBLE);
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
        }else{
            isPal = false;
            palImageView.setVisibility(View.INVISIBLE);
            scoreView.setVisibility((View.INVISIBLE));
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
    }

    private void initializePetfromString(String petInformation) {
        String[] values = petInformation.split(",");

        if (values.length == 3) {
            setisPal(true);
            petName = values[0];
            lastFed = Long.parseLong(values[1]);
            health = Integer.parseInt(values[2]);
        }
    }

    private boolean removePet() {
        setisPal(false);
        updateDisplay();
        String dir = getFilesDir().getAbsolutePath();
        File file = new File(dir, FILENAME);
        
        return file.delete();
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
        initializePetfromString(file); // call setIsPal within this function.

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
        shouldSend = 0;

        // setup graphics
        palImageView = (ImageView) findViewById(R.id.pal_view);
        palLevelAnimation = (LevelListDrawable) palImageView.getDrawable();
        ImageView foodImage = (ImageView) findViewById(R.id.food_view);
        foodImage.setBackgroundResource(R.drawable.food_animation);
        foodAnimation = (AnimationDrawable) foodImage.getBackground();

        // From CreatePetActivity
        if (intent.getStringExtra("PET_NAME") != null) {
            setisPal(true);
            petName = intent.getStringExtra("PET_NAME");
            health = 10;
            updateHealth(true);
        } else {
            files = getPetInformation();
            updateHealth(false);
        }

        if (!isPal) setisPal(false);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        updateDisplay();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mService == null) setupService();
        }
    }

    private void setupService() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mService != null) mService.stop();
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendFile(String message) {

        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }



    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //Toast.makeText(getApplicationContext(), "Received: "+readMessage, Toast.LENGTH_LONG).show();
                    initializePetfromString(readMessage);
                    updateDisplay();
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    if (shouldSend == 1) {
                        sendFile(getPetInformationString());
                        removePet();
                        shouldSend = 0;
                    }
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_CONNECTED:
                    shouldSend = 1;
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupService();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public void connect(View v) {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public void discoverable(View v) {
        ensureDiscoverable();
    }


    // on poke, corgi walks horizontally
    public void onPoke (View v) {
        if (isMoving) return;
        isMoving = true;
        final float x = palImageView.getX();
        final float y = palImageView.getY();
        final int height = ((View) palImageView.getParent()).getHeight();
        final int width = ((View) palImageView.getParent()).getWidth() + 50;
        Handler handler1 = new Handler();
        final float interval = 15f;
        int n = (int) x / (int) interval + 50;
        int i = 0;

        // move to left until disappear
        for (; i < n; i++){
            handler1.postDelayed(new Runnable() {

                @Override
                public void run() {
                    palImageView.setX(palImageView.getX() - interval);
                }
            }, 100 * i);
        }

        // move to right most
        handler1.postDelayed(new Runnable() {

            @Override
            public void run() {
                palImageView.setX((float) width);
            }
        }, 100 * i);

        // move from righ to center
        n += (int) (width - x) / (int) interval;
        for (; i < n; i++){
            handler1.postDelayed(new Runnable() {

                @Override
                public void run() {
                    palImageView.setX(palImageView.getX() - interval);
                }
            }, 100 * i);
        }

        // make sure it is at the center at last
        handler1.postDelayed(new Runnable() {

            @Override
            public void run() {
                palImageView.setX(x);
                isMoving = false;
            }
        }, 100 * i);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mService != null) {
            if (mService.getState() == BluetoothService.STATE_NONE) {
                mService.start();
            }
        }

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

        if (timer != null) {
            timer.cancel();
        }

        Log.d("TIMER", "Cancelled");

        if (isPal) {
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

    }

    public void onSend(View v)
    {
        connect(v);
        Toast.makeText(this, "Sending... please wait", Toast.LENGTH_LONG).show();
    }

    public void onReceive(View v)
    {
        ensureDiscoverable();
        Toast.makeText(this, "Ready to receive!", Toast.LENGTH_LONG).show();
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
            // feegd them
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
        else {
             Toast.makeText(this, petName + " is full!", Toast.LENGTH_SHORT).show();
        }

        Log.d("Fed", "Health: " + health);
    }
}
