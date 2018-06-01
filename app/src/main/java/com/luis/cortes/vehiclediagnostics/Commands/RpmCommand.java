package com.luis.cortes.vehiclediagnostics.Commands;

import android.os.Handler;

import com.luis.cortes.vehiclediagnostics.Commands.ObdCommand;
import com.luis.cortes.vehiclediagnostics.Constants;
import com.luis.cortes.vehiclediagnostics.Response;

public class RpmCommand extends ObdCommand {

    public RpmCommand(Handler handler) {
        super("010C", handler, Response.RPM);
    }

    @Override
    void shareToHandler(Handler handler, int messageType, int messageLen, Response response) {
        handler.obtainMessage(messageType, Constants.RESPONSE_RPM , messageLen, response).sendToTarget();
    }

}
