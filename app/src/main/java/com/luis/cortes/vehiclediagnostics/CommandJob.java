package com.luis.cortes.vehiclediagnostics;

import android.bluetooth.BluetoothSocket;

import com.luis.cortes.vehiclediagnostics.Commands.ObdCommand;

import java.io.IOException;

public class CommandJob implements Runnable {
    private BluetoothSocket socket;
    private ObdCommand obdCommand;

    public CommandJob(BluetoothSocket socket, ObdCommand obdCommand) {
        this.socket = socket;
        this.obdCommand = obdCommand;
    }

    @Override
    public void run() {
        try {
            obdCommand.run(socket.getInputStream(), socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
