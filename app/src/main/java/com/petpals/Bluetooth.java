package com.petpals;

import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.bluetooth.BluetoothDevice;
import java.util.Set;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.Toast;
import android.util.Log;

// TODO(cristen): add error message if not enabled

public class Bluetooth extends AppCompatActivity {
    // assign this constant to a non-zero value
    private static final String TAG = "Bluetooth: ";
    static final int REQUEST_ENABLE_BT = 3;
    static final int REQUEST_DISCOVERABLE = 4;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SEND: Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

    }

    // SEND: Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    // SEND: Manage
    public static void manageMyConnectedSocket(BluetoothSocket socket) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        Log.d(TAG, "onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) { // SEND
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user enabled bluetooth
                // The Intent's data Uri identifies which contact was selected.
                selectDevice();
            }
            else {
                Toast.makeText(getApplicationContext(), "Couldn't enable Bluetooth", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_DISCOVERABLE) { // SEND
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Couldn't enable discoverability", Toast.LENGTH_LONG).show();
            }
        }
    }

    // SEND
    protected void selectDevice() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
        // if none of paired devices are wanted, discover new ones
        mBluetoothAdapter.startDiscovery();
    }

    protected void connectToDevice(String address) {

    }

    // RECEIVE
    protected void receiveBluetooth() {
        // set to discoverable
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);

        // get the Bluetooth adaptor
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
    }

    // SEND
    protected void sendBluetooth() {
        Log.d(TAG, "in sendBluetooth1");
        // get the Bluetooth adaptor
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "in sendBluetooth2");
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // SEND: Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }
}
