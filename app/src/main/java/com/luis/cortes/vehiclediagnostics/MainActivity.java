package com.luis.cortes.vehiclediagnostics;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    /** UUIDS **/
    private static final UUID ELM_DONGLE = UUID.fromString("ae18f409-9a17-4b05-9941-674ce9066b51");
    private static final UUID ELM_DONGLE_SERVICE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final UUID ELM_DONGLE_SERVICE_2 = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb");

    private final int REQUEST_ENABLE_BT = 1;
    private final String ELM_ADDRESS = "00:1D:A5:00:C1:3C";
    private BluetoothAdapter btAdapter = null;

    private int progressStatus = 0;
    private TextView textBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textBox = (TextView) findViewById(R.id.text_view);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bluetooth not supported
        if (btAdapter == null) {
            Log.d(TAG, "Bluetooth not supported");
            createAlertDialog("Bluetooth", "Bluetooth NOT supported!");
            finish();
        }

        // Enable Bluetooth if not on
        if (!btAdapter.isEnabled()) {
            Log.d(TAG, "Request to use bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (isPaired()) {
            // connect

        } else {
            // scan
            Log.i(TAG, "NOTHING!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.scan:
                // Create progress dialog
                Log.d(TAG, "Scanning");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String msg = "";
        if (requestCode != Activity.RESULT_OK) {
            msg = "Bluetooth NOT enabled.";
        }

        if (requestCode == REQUEST_ENABLE_BT) {
            msg = "Bluetooth Enabled.";
        }
        createAlertDialog("Bluetooth", msg);
    }

//    private void showDialog() {
//        FragmentManager manager = getSupportFragmentManager();
//        ChooseDeviceFragment dialog = ChooseDeviceFragment.newInstance();
//        dialog.show(manager, NEW_ANNOUNCEMENT  );
//    }

    private boolean isPaired() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        String devices = "";

        boolean isUUID = false;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                ParcelUuid[] uuid = device.getUuids();

                Log.i(TAG, "\n***************");
                Log.i(TAG, deviceName);
                Log.i(TAG, device.toString());

                for (ParcelUuid id : uuid) {
                    Log.i(TAG, "**"+id.toString()+"**\n");
                }
                Log.i(TAG, "***************\n\n");

                if (deviceName.equalsIgnoreCase(ELM_ADDRESS)) isUUID = true;
            }
        }
        return isUUID;
    }

    private void createAlertDialog(String title, String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(message);
        alertBuilder.setCancelable(false);

        alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // End app ?
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
}
