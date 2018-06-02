package com.luis.cortes.vehiclediagnostics.Commands;

import android.os.Handler;

import com.luis.cortes.vehiclediagnostics.Response;

public class EchoOffCommand extends ObdCommand {
    public EchoOffCommand(Handler handler) {
        super("AT E0", handler, Response.ECHO_OFF);
        setAsProtocol(true);
    }

    @Override
    void shareToHandler(Handler handler, int messageType, int messageLen, Response response) {
        // Do Nothing
    }
}
