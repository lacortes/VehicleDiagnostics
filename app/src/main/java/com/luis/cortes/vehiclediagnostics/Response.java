package com.luis.cortes.vehiclediagnostics;

import android.util.Log;

import java.util.TreeMap;
import java.util.regex.Pattern;

public class Response {
    public static final String TAG = "Response.class";

    private static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");
    private static Pattern EMPTY_SPACES = Pattern.compile(" ");
    private static Pattern BUSINIT_PATTERN = Pattern.compile("(BUS INIT)|(BUSINIT)|(\\.)");
    private static Pattern SEARCHING_PATTERN = Pattern.compile("SEARCHING");
    private static Pattern DIGITS_LETTERS_PATTERN = Pattern.compile("([0-9A-F])+");

    // Response Header PID
    public static final String NONE = "";
    public static final String RPM = "0C";
    public static final String AUTO = "AUTO";
    public static final String ECHO_OFF = "ECHO_OFF";
    public static final String SPEED = "0D";
    public static final String THROTTLE = "11";
    public static final String RESET = "RESET";

    private String[] validResponseModes = {"41"};
    private String response;
    private int mode;
    private int A = 0;
    private int B = 0;
    private int C = 0;
    private int D = 0;
    private String pidType;

    public Response(StringBuilder buffer, String pidType) {
        this.pidType = pidType;
        this.response = cleanResponse(buffer);
        extractFields(this.response);
    }

    public int getA() {
        return A;
    }

    public int getB() {
        return B;
    }

    public int getC() {
        return C;
    }

    public int getD() {
        return D;
    }

    public byte[] getByteResponse() {
        return this.response.getBytes();
    }

    public int respByteLen() {
        return this.response.getBytes().length;
    }

    public String getResponse() {
        return this.response;
    }

    private String cleanResponse(StringBuilder text) {
        String rawData = removeAll(SEARCHING_PATTERN, text.toString());
        rawData = removeAll(WHITESPACE_PATTERN, rawData); //removes all [ \t\n\x0B\f\r]
        rawData = removeAll(BUSINIT_PATTERN, rawData);
        rawData = removeAll(EMPTY_SPACES, rawData);

        Log.i(TAG, "FILTERED RESPONSE: "+rawData);

        if (isValidMode(rawData)) {
            // Substring starting with mode and pid. Ex: "410D"
            Log.i(TAG, "PID TYPE: "+pidType);

            int startIndex =  rawData.indexOf(this.validResponseModes[mode] + pidType);

            // Check startIndex (wanted substring) is not -1 or at the beginning
            if (startIndex < 0) {
                Log.i(TAG, "Returning: " + rawData);
                return rawData;
            } else {
                Log.i(TAG, "Returning: " + rawData.substring(startIndex));
                return rawData.substring(startIndex);
            }
        }

        return "";
    }

    private void extractFields(String cleanResponse) {
        Log.i(TAG, "EXTRACTING : "+cleanResponse);
        if (cleanResponse.length() <= 4) return;

        int[] buffer = {0, 0, 0, 0};  // Hold A, B, C, D

        for (int i = 0, start = 4; start <= cleanResponse.length() - 2; i++, start += 2) {
            String field = cleanResponse.substring(start, start + 2);
            buffer[i] = Integer.valueOf(field, 16);
        }

        this.A = buffer[0];
        this.B = buffer[1];
        this.C = buffer[2];
        this.D = buffer[3];

    }

    private boolean isValidMode(String text) {
        for (int i = 0; i < this.validResponseModes.length; i++) {
            if (text.contains(this.validResponseModes[i])) {
                this.mode = i;
                Log.i(TAG, "Found Mode: " + validResponseModes[i]);
                return true;
            }
        }
        return false;
    }

    private String replaceAll(Pattern pattern, String input, String replacement) {
        return pattern.matcher(input).replaceAll(replacement);
    }

    private String removeAll(Pattern pattern, String input) {
        return pattern.matcher(input).replaceAll("");
    }
}
