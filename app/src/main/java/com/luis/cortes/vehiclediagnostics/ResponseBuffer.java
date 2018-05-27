package com.luis.cortes.vehiclediagnostics;

public class ResponseBuffer {
    private static ResponseBuffer respBuffer = new ResponseBuffer();
    private String[] response;
    private int index;
    private boolean isComplete;

    private ResponseBuffer() {
        this.isComplete = false;
        this.index = 0;
        this.response = new String[100];
    }

    public static synchronized ResponseBuffer getInstance() {
        return respBuffer;
    }

    public void addResponse(String response) {
        if (!this.isComplete) {
            this.response[index++] = response;
            if (response.contains(">")) this.isComplete = true;
        }
    }

    public boolean isComplete() {
        return this.isComplete;
    }

    public String getResponse() {
        if (!isComplete) return "";

        // Iterate up until where '>' is found
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.index; i++) {
            builder.append(this.response[i]);
        }

        // Reset
        this.isComplete = false;
        this.index = 0;

        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < index; i++) {
            builder.append(response[i]);
        }
        return builder.toString();
    }

}
