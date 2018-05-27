package com.luis.cortes.vehiclediagnostics;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final int REQUEST_ENABLE_BT = 1;
    private final String ELM_ADDRESS = "00:1D:A5:00:C1:3C";

    // Member fields
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothVehicleService mBtService;
    private Handler mHandler;

    // Views
    private int progressStatus = 0;
    private TextView textBoxOut;
    private TextView sendTextView;
    private TextView valueTextView;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hook up Views
        textBoxOut =  (TextView) findViewById(R.id.text_view);
        sendTextView = (TextView) findViewById(R.id.send_text_view);
        valueTextView =  (TextView) findViewById(R.id.value_text_view);
        sendButton = (Button) findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOBD2CMD("010C");
            }
        });

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg  != null) {
                    switch (msg.what) {
                        case Constants.MESSAGE_READ:
                            byte[] readBuf = (byte[]) msg.obj;
                            String readMessage = new String(readBuf, 0, msg.arg1);
                            readMessage = readMessage.trim();

                            ResponseBuffer respBuffer = ResponseBuffer.getInstance();
                            respBuffer.addResponse(readMessage);

                            Log.i(TAG, "Buffer STATE: " + respBuffer);

                            if (respBuffer.isComplete()) {
                                String response = respBuffer.getResponse();

                                // construct a string from the valid bytes in the buffer
                                textBoxOut.setText(response);
                                double val = showEngineRPM(response);

                                valueTextView.setText(val+"");

                                Log.i(TAG, "READ: **" + response + "**");
                                Toast.makeText(getApplicationContext(), "READ:  "+"**"+response+"**", Toast.LENGTH_SHORT).show();
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
                                    sendOBD2CMD("AT SP 0");
                                    Toast.makeText(getApplicationContext(), "sending init commands", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            break;
                    }
                }
            }
        };

        // Bluetooth not supported
        if (mBtAdapter == null) {
            Log.d(TAG, "Bluetooth not supported");
            createAlertDialog("Bluetooth", "Bluetooth NOT supported!");
            finish();
        }

        // Enable Bluetooth if not on
        if (!mBtAdapter.isEnabled()) {
            Log.d(TAG, "Request to use bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        BluetoothDevice device = getPaired();
        if (device != null) {
            // Connect to bluetooth dongle
            Log.i(TAG, "Connecting");
//            BluetoothConnection connection = new BluetoothConnection(device, Constants.ELM_DONGLE_SERVICE, mBtAdapter);
//            connection.start();

            mBtService = new BluetoothVehicleService(getApplicationContext(), mHandler);
            Toast.makeText(getApplicationContext(), "Connecting to Dongle", Toast.LENGTH_LONG).show();
            mBtService.connect(device);

//           byte[] bytes = {0x7d, (byte)0xf0, 0x20, 0x10, (byte)0xd5, 0x55, 0x55, 0x55, 0x05 };
//
//            btService.write(bytes);


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

                Log.i(TAG, "\n***************");
                Log.i(TAG, deviceName);
                Log.i(TAG, device.toString());

                for (ParcelUuid id : uuid) {
                    Log.i(TAG, "**"+id.toString()+"**\n");
                }
                Log.i(TAG, "***************\n\n");

                if (deviceHardwareAddress.equalsIgnoreCase(ELM_ADDRESS)) {
                    Log.i(TAG, "FOUND DONGLE");
                    return device;
                }
            }
        }
        return null;
    }

    private void sendOBD2CMD(String sendMsg)
    {
        String strCMD = sendMsg;
        strCMD += '\r';

        byte[] byteCMD = strCMD.getBytes();
        mBtService.write(byteCMD);
    }

    private double showEngineRPM(String buffer)
    {
        String buf = buffer;
        buf = cleanResponse(buf);

        if (buf.contains("410C"))
        {
            try
            {
                buf = buf.substring(buf.indexOf("410C"));

                String MSB = buf.substring(4, 6);
                String LSB = buf.substring(6, 8);
                int A = Integer.valueOf(MSB, 16);
                int B = Integer.valueOf(LSB, 16);

                return  ((A * 256.00) + B) / 4.00;
            }
            catch (IndexOutOfBoundsException | NumberFormatException e)
            {
                Log.i(TAG, e.getMessage());
            }
        }

        return -1;
    }

    private String cleanResponse(String text)
    {
        text = text.trim();
        text = text.replace("\t", "");
        text = text.replace(" ", "");
        text = text.replace(">", "");

        return text;
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

    public String bytesToHex(byte[] bytes) {
        String x = "[ ";
        for (byte info : bytes) {
            Log.i(TAG, Integer.toHexString(info) +"  ");
            x += ""+Integer.toHexString(info & 0xFF);
            x += " ";
        }
        x += "]";
        return x;
    }
}
