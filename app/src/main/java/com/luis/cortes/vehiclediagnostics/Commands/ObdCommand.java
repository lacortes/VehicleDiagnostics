package com.luis.cortes.vehiclediagnostics.Commands;

import android.os.Handler;
import android.util.Log;

import com.luis.cortes.vehiclediagnostics.Constants;
import com.luis.cortes.vehiclediagnostics.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

public abstract class ObdCommand {
    private static final String TAG = "ObdCommand.class";

    private String cmd;
    private Handler handler;
    private Long responseDelayInMs = null;
    private boolean isProtocol = false;

    private String pidType;

    public ObdCommand(String command, Handler handler, String pidType) {
        this.cmd = command;
        this.handler = handler;
        this.pidType = pidType;
    }

    private ObdCommand() { }

    public void run(InputStream inputStream, OutputStream outputStream) {
        synchronized (ObdCommand.class) {
            sendCommand(outputStream);
            readResult(inputStream);
        }
    }

    public void sendCommand(OutputStream outputStream) {
        try {
            Log.i(TAG, "Sending: "+cmd);
            byte[] buffer = (cmd + "\r").getBytes();
            outputStream.write(buffer);
            outputStream.flush();

            // Share the sent message back to the UI Activity
            shareToHandler(this.handler, Constants.MESSAGE_WRITE, buffer.length ,buffer);
        } catch (IOException e) {
            Log.i(TAG, "Exception during write", e);
        }
    }

    public void setAsProtocol(boolean isProtocol) {
        this.isProtocol = isProtocol;
    }

    abstract void shareToHandler(Handler handler, int messageType, int messageLen, Response response);

    protected void shareToHandler(Handler handler, int messageType, int messageLen, byte[] message) {
        handler.obtainMessage(messageType, Constants.RESPONSE_RPM , messageLen, message).sendToTarget();
    }

    private void readResult(InputStream inputStream) {
        readRawData(inputStream);
    }

    private void readRawData(InputStream in)  {
        byte b = 0;
        StringBuilder res = new StringBuilder();

        // read until '>' arrives OR end of stream reached
        char c;
        // -1 if the end of the stream is reached
        try {
            Log.i(TAG, "Reading in data ... ");
            while (((b = (byte) in.read()) > -1)) {
                c = (char) b;
                if (c == '>') // read until '>' arrives
                {
                    break;
                }
                res.append(c);
            }

            if (isProtocol) {return;}

            Response response = new Response(res, this.pidType);
            shareToHandler(this.handler, Constants.MESSAGE_READ, -1, response);

        } catch (IOException ex) { ex.printStackTrace();}
    }

    protected String replaceAll(Pattern pattern, String input, String replacement) {
        return pattern.matcher(input).replaceAll(replacement);
    }

    protected String removeAll(Pattern pattern, String input) {
        return pattern.matcher(input).replaceAll("");
    }

}
