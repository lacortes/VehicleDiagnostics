package com.luis.cortes.vehiclediagnostics;

import android.util.Log;

import java.util.TreeMap;
import java.util.regex.Pattern;

public class Response {
    public static final String TAG = "Response.class";

    private static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");
    private static Pattern BUSINIT_PATTERN = Pattern.compile("(BUS INIT)|(BUSINIT)|(\\.)");
    private static Pattern SEARCHING_PATTERN = Pattern.compile("SEARCHING");
    private static Pattern DIGITS_LETTERS_PATTERN = Pattern.compile("([0-9A-F])+");

    // Response Header PID
    public static final String NONE = "";
    public static final String RPM = "0C";
    public static final String AUTO = "AUTO";
    public static final String ECHO_OFF = "ECHO_OFF";

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
        extractFields(response);
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

    private String cleanResponse(StringBuilder text) {
        String rawData = removeAll(SEARCHING_PATTERN, text.toString());
        rawData = removeAll(WHITESPACE_PATTERN, rawData); //removes all [ \t\n\x0B\f\r]
        rawData = removeAll(BUSINIT_PATTERN, rawData);

        Log.i(TAG, "FILTERED RESPONSE: "+rawData);

        if (isValidMode(rawData)) {
            // Substring starting with mode and pid. Ex: "410D"
            return text.substring(text.indexOf(this.validResponseModes[mode] + pidType));
        }

        return "";
    }

    private void extractFields(String cleanResponse) {
        if (cleanResponse.length() <= 4) return;

        String field;
        if (cleanResponse.length() == 6) {
            field = cleanResponse.substring(4, 6);
            this.A = Integer.valueOf(field, 16);
        } else {
            this. A = 0;
        }

        if (cleanResponse.length() == 8) {
            field = cleanResponse.substring(6, 8);
            this.B = Integer.valueOf(field, 16);
        } else {
            this.B = 0;
        }

        if (cleanResponse.length() == 10) {
            field = cleanResponse.substring(8, 10);
            this.C = Integer.valueOf(field, 16);
        } else {
            this.C = 0;
        }

        if (cleanResponse.length() == 12) {
            field = cleanResponse.substring(10, 12);
            this.D = Integer.valueOf(field, 16);
        } else {
            this.D = 0;
        }

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
