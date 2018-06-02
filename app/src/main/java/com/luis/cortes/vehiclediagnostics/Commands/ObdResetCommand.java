package com.luis.cortes.vehiclediagnostics.Commands;

import android.os.Handler;

import com.luis.cortes.vehiclediagnostics.Response;

public class ObdResetCommand extends ObdCommand {
    public ObdResetCommand(Handler handler) {
        super("AT Z", handler, Response.RESET);
        setAsProtocol(true);

    }

    @Override
    void shareToHandler(Handler handler, int messageType, int messageLen, Response response) {
        // Nothing to do
    }
}
