package com.luis.cortes.vehiclediagnostics;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.luis.cortes.vehiclediagnostics.Commands.AutoProtocolCommand;
import com.luis.cortes.vehiclediagnostics.Commands.RpmCommand;
import com.luis.cortes.vehiclediagnostics.Commands.Throttle;
import com.luis.cortes.vehiclediagnostics.Commands.VehicleSpeedCommand;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity.class";

    private final int REQUEST_ENABLE_BT = 1;
    private final String ELM_ADDRESS = "00:1D:A5:00:C1:3C";

    // Member fields
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothVehicleService mBtService;
    private Handler mHandler;
    private ArrayList<CommandJob> mCommandJobs;
    private LinkedBlockingQueue<CommandJob> mCommandList;

    // Views
    private int progressStatus = 0;
    private TextView textBoxOut;
    private TextView sendTextView;
    private TextView rpmTextView;
    private TextView speedTextView;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hook up Views
        textBoxOut =  (TextView) findViewById(R.id.text_view);
        sendTextView = (TextView) findViewById(R.id.send_text_view);
        rpmTextView =  (TextView) findViewById(R.id.value_rpm_text_view);
        speedTextView = (TextView) findViewById(R.id.value_speed_text_view);
        sendButton = (Button) findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToDongle();
            }
        });

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.MESSAGE_READ:
                        int respType = -1;

                        respType = msg.arg1;
                        Response response = (Response) msg.obj;

                        switch (respType) {
                            case Constants.RESPONSE_RPM:
                                // Show rpm
                                Log.i(TAG, "Reading RPM ... ");
                                displayRespToLogcat(response);
                                Double rpm = VehStats.getValue(response, new Formula() {
                                    @Override
                                    public double calculate(int a, int b, int c, int d) {
                                        return ((a * 256.00) + b) / 4.00;
                                    }
                                });
                                rpmTextView.setText(rpm + "");
                                break;
                            case Constants.RESPONSE_SPEED:
                                Log.i(TAG, "Reading Speed ... ");
                                Double speed = VehStats.getValue(response, new Formula() {
                                    @Override
                                    public double calculate(int a, int b, int c, int d) {
                                        // 1 km = 0.621371 m
                                        return a * 0.621371;
                                    }
                                });
                                speedTextView.setText(speed + "");
                                break;
                        }
                        break;
                    case Constants.MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        String writeMessage = new String(writeBuf);
                        sendTextView.setText("");
                        sendTextView.setText(writeMessage);
                        break;
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothVehicleService.STATE_CONNECTED:
                                // Send Default commands
                                // sendOBD2CMD("AT SP 0", Response.NONE);
                                Toast.makeText(getApplicationContext(), "sending init commands", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        break;
                    case BluetoothVehicleService.STATE_BT_SOCKET_AVAILABLE:
                        // Init commands
                        mCommandJobs = new ArrayList<>();
                        mCommandList = new LinkedBlockingQueue<>();

                        BluetoothSocket socket = (BluetoothSocket) msg.obj;

                        // Add commands
                        Log.i(TAG, "Init commands ... ");
                        try {
                            mCommandList.put(new CommandJob(socket, new AutoProtocolCommand(mHandler)));
                            mCommandList.put(new CommandJob(socket, new RpmCommand(mHandler)));
                            mCommandList.put(new CommandJob(socket, new VehicleSpeedCommand(mHandler)));
                            mCommandList.put(new CommandJob(socket, new Throttle(mHandler)));
//                                mCommandList.add(new CommandJob(socket, new EchoOffCommand(mHandler)));
                        } catch (InterruptedException e) {
                            Log.e(TAG, "PUT Interrupted");
                            e.printStackTrace();
                        }

                        performCommands();

                        break;
                     default:
                         // Do nothing
                         break;
                }
            }
        };

        // Bluetooth not supported
        if (mBtAdapter == null) {
            Log.d(TAG, "Bluetooth not supported");
            createAlertDialog("Bluetooth", "Bluetooth NOT supported!");
            finish();
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

    /**
     *  Query already paired devices and look for ELM-327 dongle; return it if found, null otherwise.
     * @return BluetoothDevice
     */
    private BluetoothDevice getPaired() {
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        String devices = "";

        boolean isUUID = false;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                ParcelUuid[] uuid = device.getUuids();

                if (deviceHardwareAddress.equalsIgnoreCase(ELM_ADDRESS)) {
                    Log.i(TAG, "FOUND DONGLE");
                    return device;
                }
            }
        }
        return null;
    }

    private void performCommands() {
        while (mCommandList.size() > 0) {
            CommandJob job = null;

            try {
                job = mCommandList.take();
                job.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
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

    private void connectToDongle() {
        // Enable Bluetooth if not on
        if (!mBtAdapter.isEnabled()) {
            Log.d(TAG, "Request to use bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        BluetoothDevice device = getPaired();
        if (device != null) {
            // Connect to bluetooth dongle
            Log.i(TAG, "Connecting to Dongle");

            mBtService = new BluetoothVehicleService(getApplicationContext(), mHandler);
            Toast.makeText(getApplicationContext(), "Connecting to Dongle", Toast.LENGTH_LONG).show();
            mBtService.connect(device);

        } else {
            // TODO: Scan for devices
            Log.i(TAG, "No device paired.");
        }
    }

    private void displayRespToLogcat(Response response) {
        Log.i(TAG, response.getResponse());

        Log.i(TAG, "A: "+response.getA());
        Log.i(TAG, "B: "+response.getB());
        Log.i(TAG, "C: "+response.getC());
        Log.i(TAG, "D: "+response.getD());
    }
}
