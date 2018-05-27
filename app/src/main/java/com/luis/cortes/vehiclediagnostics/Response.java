package com.luis.cortes.vehiclediagnostics;

public class Response {
    // Response Header Mode
    public static final String rpm = "0C";

    public enum ResponseType {
        RPM, SPEED
    }

    private String[] validResponseModes = {"41", "42", "43", "44", "45", "46", "47", "48", "49", "4A"};
    private String response;
    private int mode;
    private int A;
    private int B;
    private int C;
    private int D;
    private ResponseType type;

    public Response(String buffer, ResponseType type) {
        this.type = type;
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

    private String cleanResponse(String text) {
        text = text.trim();
        text = text.replace("\t", "");
        text = text.replace(" ", "");
        text = text.replace(">", "");

        if (isValidMode(text)) {
            // Substring starting with mode and pid. Ex: "410D"
            return text.substring(text.indexOf(this.validResponseModes[mode] + getResponsePID(this.type)));
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

    private String getResponsePID(ResponseType type) {
        switch (type) {
            case RPM:
                return rpm;
            case SPEED:
                break;
        }
        return "";
    }

    private boolean isValidMode(String text) {
        for (int i = 0; i < this.validResponseModes.length; i++) {
            if (text.contains(this.validResponseModes[i])) {
                this.mode = i;
                return true;
            }
        }
        return false;
    }
}
