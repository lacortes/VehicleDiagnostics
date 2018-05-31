package com.luis.cortes.vehiclediagnostics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;


/**
 * Created by luis_cortes on 5/19/18.
 */

public class BluetoothVehicleService {
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private static final String TAG = "BluetoothVehicleService";
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private final Context mContext;
    private ConnectThread mConnectThread;
    private StreamThread mStreamThread;
    private int mState;
    private int mNewState;
    private BluetoothSocket mSocket;

    public BluetoothVehicleService(Context context, Handler handler) {
        this.mContext = context;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = handler;
        this.mState = STATE_NONE;
        this.mNewState  = mState;
    }

    public void connect(BluetoothDevice device) {
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public BluetoothSocket getSocket() {
        return this.mSocket;
    }

    public void stream(BluetoothSocket socket, BluetoothDevice device) {
        // Start the thread to manage the connection and perform transmissions
        Log.i(TAG, "Creating stream thread" );
        mStreamThread = new StreamThread(socket);
        mStreamThread.start();
        updateUserInterfaceTitle();
    }

    /**
     * Write to the ConnectedThread
     *
     * @param out The bytes to write
     */
    public void write(byte[] out, String type) {
        // Create temporary object
        StreamThread r = mStreamThread;

        // Perform the write unsynchronized
        r.write(out, type);
    }

    public int getState() {
        return this.mState;
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private void updateUserInterfaceTitle() {
        mState = getState();
        Log.i(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }


    /**
     * Class to initiate connection
     */
    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmDeviceSocket;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tempSocket = null;

            try {
                tempSocket = device.createRfcommSocketToServiceRecord(Constants.ELM_DONGLE_SERVICE);
                Log.i(TAG, "Getting socket");
            } catch (IOException e) {
                Log.i(TAG, "Failed to get socket");
                e.printStackTrace();
            }
            mmDeviceSocket = tempSocket;
        }


        @Override
        public void run() {
            mAdapter.cancelDiscovery();

            // Make connection to Bluetooth Socket
            try {
                Log.i(TAG, "Connecting via socket ... ");
                mmDeviceSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mmDeviceSocket.close();
                } catch (IOException e2) {
                    Log.i(TAG, "unable to close() " + mmDeviceSocket +
                            " socket during connection failure", e2);
                }
                // connectionFailed();
                return;
            }

            // Reset ConnectThread
            mConnectThread = null;

            // Start the connected thread
            // connected(mmDeviceSocket, mmDevice);
            mSocket = mmDeviceSocket;
            // stream(mmDeviceSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmDeviceSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class StreamThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public StreamThread(BluetoothSocket socket) {
            Log.i(TAG, "creating StreamThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.i(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN listening on inputstream");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            // TODO: change this condition to check for state
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    Log.i(TAG, "Reading in: "+bytes + "");

                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(Constants.MESSAGE_READ, buffer).sendToTarget();
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.i(TAG, "disconnected", e);
//                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer, String type) {
            try {
                mmOutStream.write(buffer);
                mmOutStream.flush();

                // Share the sent message back to the UI Activity

                mHandler.obtainMessage(Constants.MESSAGE_WRITE, type.hashCode(), -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.i(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.i(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
