package com.luis.cortes.vehiclediagnostics;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.luis.cortes.vehiclediagnostics.Commands.ObdCommand;

import java.io.IOException;

public class CommandJob extends Thread {
    private final String TAG = "CommandJob.class";

    private BluetoothSocket socket;
    private ObdCommand obdCommand;

    public CommandJob(BluetoothSocket socket, ObdCommand obdCommand) {
        this.socket = socket;
        this.obdCommand = obdCommand;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(TAG, "Running " + obdCommand.getCommandType());
                obdCommand.run(socket.getInputStream(), socket.getOutputStream());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, obdCommand.getCommandType() + "Ending");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
