package com.luis.cortes.vehiclediagnostics;

import android.os.Handler;

public class RpmCommand extends ObdCommand {

    public RpmCommand(Handler handler) {
        super("010C", handler, Response.RPM);
    }

    @Override
    void shareToHandler(Handler handler, int messageType, int messageLen, Response response) {
        handler.obtainMessage(messageType, Constants.RESPONSE_RPM , messageLen, response).sendToTarget();
    }

}
