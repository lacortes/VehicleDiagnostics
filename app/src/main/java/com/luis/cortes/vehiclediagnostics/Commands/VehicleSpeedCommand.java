package com.luis.cortes.vehiclediagnostics.Commands;

import android.os.Handler;

import com.luis.cortes.vehiclediagnostics.Constants;
import com.luis.cortes.vehiclediagnostics.Response;

public class VehicleSpeedCommand extends ObdCommand {
    public VehicleSpeedCommand(Handler handler) {
        super("010D", handler, Response.SPEED);
    }

    @Override
    void shareToHandler(Handler handler, int messageType, int messageLen, Response response) {
        handler.obtainMessage(messageType, Constants.RESPONSE_SPEED, messageLen, response).sendToTarget();
    }
}
