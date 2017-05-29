package com.petpals;

import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.bluetooth.BluetoothDevice;
import java.util.Set;
import java.util.HashSet;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.Toast;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.lang.reflect.Method;

// TODO(cristen): add error message if not enabled

public class Bluetooth extends AppCompatActivity {
    // assign this constant to a non-zero value
    private static final String TAG = "Bluetooth: ";
    static final int REQUEST_ENABLE_BT = 3;
    static final int REQUEST_DISCOVERABLE = 4;
    static final int REQUEST_CONNECT_DEVICE = 5;
    private BluetoothAdapter mBluetoothAdapter;
    private Activity mContext;
    private ConnectThread mConnectThread;
    private Set<BluetoothDevice> mUnpairedDevices;


    public Bluetooth (Activity activity) {
        this.mContext = activity;
       mUnpairedDevices = new HashSet<BluetoothDevice>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SEND: Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

    }

    public boolean createBond(BluetoothDevice btDevice)
    {
        try {
            Log.d(TAG, "Start Pairing...");
            Method m = btDevice.getClass().getMethod("createBond", (Class[]) null);
            Boolean returnValue = (Boolean) m.invoke(btDevice, (Object[]) null);
            Log.d(TAG, "Pairing finished.");
            return returnValue;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    // SEND: Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                mUnpairedDevices.add(device);

                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "Discovered: " + deviceName);
            }
            else if (mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // selectDevice();
            }
        }
    };

    // SEND: Manage
    public static void manageMyConnectedSocket(BluetoothSocket socket) {
        Log.d(TAG, "Made it to manageMyConnectedSocket!!");
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
            }
            else {
                Toast.makeText(getApplicationContext(), "Couldn't enable Bluetooth", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == REQUEST_DISCOVERABLE) { // RECEIVE
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Couldn't enable discoverability", Toast.LENGTH_LONG).show();
            }
            if (resultCode == RESULT_OK) {
                AcceptThread t = new AcceptThread();
                t.run();
            }
        }
        else if (requestCode == REQUEST_CONNECT_DEVICE) {
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                if (mConnectThread != null) {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }

                mConnectThread = new ConnectThread(device);
                mConnectThread.run();
            }

        }
    }

    // SEND
    protected void selectDevice() {
        CharSequence devices[] = new CharSequence[mUnpairedDevices.size()];
        if (mUnpairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            int i = 0;
            for (BluetoothDevice device : mUnpairedDevices) {
                //String deviceName = device.getName();
                //String deviceHardwareAddress = device.getAddress(); // MAC address
                devices[i] = device.getName() + " " + device.getAddress();
                i++;
            }
        }
        else {
            Log.d(TAG, "no devices found");
            return;
        }

        final CharSequence[] innerDevices = devices;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Select a Bluetooth Device");
        builder.setItems(devices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                CharSequence[] nameAndAddr = ((String)innerDevices[which]).split(" ");
                CharSequence macAddr = nameAndAddr[1];
                Log.d(TAG, "macAddr: " + macAddr);
                BluetoothDevice connectDevice = null;
                for (BluetoothDevice device : mUnpairedDevices) {
                    Log.d(TAG, "device MAC: " + device.getAddress());
                    if (macAddr.equals(device.getAddress())) {
                        connectDevice = device;
                        break;
                    }
                }
                if (connectDevice == null) {
                    Log.d(TAG, "device not found");
                }
                else {
                    if (connectDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                        createBond(connectDevice);
                    }
                    if (mConnectThread != null) {
                        mConnectThread.cancel();
                        mConnectThread = null;
                    }

                    mConnectThread = new ConnectThread(connectDevice);
                    mConnectThread.run();
                }

            }
        });
        builder.show();
    }

    protected void connectToDevice(String address) {

    }

    // RECEIVE
    protected void receiveBluetooth() {
        // get the Bluetooth adaptor
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
        // set to discoverable
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        mContext.startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
    }

    // SEND
    protected void sendBluetooth() {
        // get the Bluetooth adaptor
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // SEND: Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }
}
