package com.luis.cortes.vehiclediagnostics.Commands;

import android.os.Handler;

import com.luis.cortes.vehiclediagnostics.Constants;
import com.luis.cortes.vehiclediagnostics.Response;

public class Throttle extends ObdCommand {
    public Throttle(Handler handler) {
        super("0111", handler, Response.THROTTLE);
    }

    @Override
    void shareToHandler(Handler handler, int messageType, int messageLen, Response response) {
        handler.obtainMessage(messageType, Constants.RESPONSE_THROTTLE, messageLen, response).sendToTarget();
    }
}
