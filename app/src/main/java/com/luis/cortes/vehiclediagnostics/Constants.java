package com.luis.cortes.vehiclediagnostics;

import java.util.UUID;

/**
 * Created by luis_cortes on 5/19/18.
 */

public interface Constants {
    /** UUIDS **/
    static final UUID ELM_DONGLE = UUID.fromString("ae18f409-9a17-4b05-9941-674ce9066b51");
    static final UUID ELM_DONGLE_SERVICE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    static final UUID ELM_DONGLE_SERVICE_2 = UUID.fromString("00000000-0000-1000-8000-00805f9b34fb");

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    public static final int RESPONSE_RPM = 10;
    public static final int RESPONSE_NA = 11;

    static final String MESSAGE_TEST = "TESTING_MSG";
}
