package com.luis.cortes.vehiclediagnostics.Commands;

import android.os.Handler;

import com.luis.cortes.vehiclediagnostics.Response;

public class AutoProtocolCommand extends ObdCommand {

    public AutoProtocolCommand(Handler handler) {
        super("AT SP 0", handler, Response.AUTO);
        setAsProtocol(true);
    }

    @Override
    void shareToHandler(Handler handler, int messageType, int messageLen, Response response) {
        // Do nothing
    }
}
