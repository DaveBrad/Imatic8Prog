/*
 * Copyright (c) 2017 dbradley.
 *
 * License: Imatic8Prog
 *
 * Free to use software and associated documentation (the "Software")
 * without charge
 *
 * Distribution, merge into other programs, copy of the software is
 * permitted with the following a) to c) conditions:
 *
 * a) Software is provided as-is and without warranty of any kind. The user is
 * responsible to ensure the "software" fits their needs. In no event shall the
 * author(s) or copyholder be liable for any claim, damages or other liability
 * in connection with the "Software".
 *
 * b) Permission is hereby granted to modify the "Software" with two sub-conditions:
 *
 * b.1) A 'Copyright (c) <year> <copyright-holder>.' is added above the original
 * copyright line(s).
 *
 * b.2) The Main class name is changed to identify a different "program" name
 * from the original.
 *
 * c) The above copyright notice and this permission/license notice shall
 * be included in all copies or substantial portions of the Software.
 */
package imatic8;

import static imatic8.Imatic8Constants.MAX_RELAY_NUMBER;
import static imatic8.Imatic8Constants.MIN_RELAY_NUMBER;
import static imatic8.Imatic8Constants.RELAY_ALL_OFF_CODE;
import static imatic8.Imatic8Constants.RELAY_ALL_ON_CODE;
import static imatic8.Imatic8Constants.RELAY_OFF_CODE;
import static imatic8.Imatic8Constants.RELAY_ON_CODE;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import static imatic8.Imatic8Constants.RESPONSE_RELAY_NUMBER_BYTE_INDEX;
import static imatic8.Imatic8Constants.RESPONSE_ON_OFF_STATE_BYTE_INDEX;

/**
 * Class that recorder the requests for relay activation in an INI file so a
 * best guess state of the relays is possible&#46; The Imatic8 board does not
 * support a relay state query message, so what is set is best guess.
 *
 * @author dbradley
 */
class Imatic8RelayRecorder {

    private final static String RECORDER_FILE_NAME = "Imatic8Record.ini";

    /**
     * User directory the Imatic8Prog is being run from.
     */
    private final File userDir;
    /**
     * The full path for the properties file.
     */
    private final File propertyFile;

    private Properties propertiesObject = null;

    /**
     * Create the Imatic8 recorder for the relay states. This only represents
     * the state as per command/argument request and not the actual board. The
     * board does not support a relay state query.
     */
    Imatic8RelayRecorder() {
        this.userDir = new File(System.getProperty("user.dir"));
        this.propertyFile = new File(userDir, RECORDER_FILE_NAME);

    }

    /**
     * Get the response action information from a request that succeeded.
     *
     * @param responseByte response bytes
     *
     * @return the response type which should match
     */
    private Imatic8RelayInfo getActionCodeFromResponse(byte responseByte) {
        Imatic8RelayInfo brdAction = null;

        switch (responseByte) {
            case RELAY_ON_CODE:
                brdAction = Imatic8RelayInfo.RELAY_ON;
                break;
            case RELAY_OFF_CODE:
                brdAction = Imatic8RelayInfo.RELAY_OFF;
                break;
            case RELAY_ALL_ON_CODE:
                brdAction = Imatic8RelayInfo.RELAY_ALL_ON;
                break;
            case RELAY_ALL_OFF_CODE:
                brdAction = Imatic8RelayInfo.RELAY_ALL_OFF;
                break;
            default:
            // nothing can be done
        }
        return brdAction;
    }

    /**
     * Set the relay action just processed in the INI file.
     *
     * @param boardResponseByteArr response bytes from an operation request to a
     *                             board
     */
    void setRelayRecord(byte[] boardResponseByteArr) {
        loadProperties();

        // successful action as a response received
        byte relNumResponse = boardResponseByteArr[RESPONSE_RELAY_NUMBER_BYTE_INDEX];
        byte relOnOffResponse = boardResponseByteArr[RESPONSE_ON_OFF_STATE_BYTE_INDEX];

        String state = "???";
        int relayNum = -1;

        Imatic8RelayInfo boardAction = getActionCodeFromResponse(relOnOffResponse);

        switch (boardAction) {
            case RELAY_ON:
                state = "ON";
                relayNum = relNumResponse;
                break;
            case RELAY_OFF:
                state = "off";
                relayNum = relNumResponse;
                break;
            case RELAY_ALL_ON:
                state = "ON";
                relayNum = -1;
                break;
            case RELAY_ALL_OFF:
                state = "off";
                relayNum = -1;
                break;
        }
        // process the properties for all
        if (relayNum == -1) {
            for (int i = MIN_RELAY_NUMBER; i <= MAX_RELAY_NUMBER; i++) {
                propertiesObject.setProperty(String.format("R%d", i), state);
            }
        } else {
            propertiesObject.setProperty(String.format("R%d", relayNum), state);
        }
        // store the property
        storeProperties();
    }

    /** Report the relay states (best guess) from the INI file. */
    void reportRelayStates() {
        loadProperties();

        // propertiesObject.list(System.out);
        // does not output in 1 to 8 order (that its random)
        String lineOfStates = "Status:";
        for (int i = MIN_RELAY_NUMBER; i <= MAX_RELAY_NUMBER; i++) {
            String key = String.format("R%d", i);
            String c = propertiesObject.getProperty(key)
                    .equals("off") ? "-" : String.format("%d", i);

            lineOfStates = String.format("%s%s", lineOfStates, c);
        }
        System.out.printf("%s\n", lineOfStates);
    }

    /**
     * Load the INI properties file that is the best guess of the relay states.
     * Creates the INI at 'off all' if not found.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    private void loadProperties() {
        if (propertiesObject == null) {
            try {
                FileInputStream iStream = new FileInputStream(propertyFile);

                propertiesObject = new Properties();
                try {
                    propertiesObject.load(iStream);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.exit(-97);
                }
                try {
                    iStream.close();
                } catch (IOException ex) {
                    // nothing can be done;
                }

            } catch (FileNotFoundException ex) {
                // need to create the file for the first time
                propertiesObject = new Properties();

                for (int i = MIN_RELAY_NUMBER; i <= MAX_RELAY_NUMBER; i++) {
                    propertiesObject.setProperty(
                            String.format("R%d", i), "OFF");// R1 R2 ......
                }
                storeProperties();
            }
        }
    }

    /** Store the relay states properties to the INI. */
    @SuppressWarnings("CallToPrintStackTrace")
    private void storeProperties() {
        FileOutputStream oStream;
        try {
            oStream = new FileOutputStream(propertyFile);
            try {
                propertiesObject.store(oStream, "Imatic8 relay states (best guess)");

            } catch (IOException ex1) {
                ex1.printStackTrace();
                System.exit(-99);
            }
            try {
                oStream.close();
            } catch (IOException ex) {
                // nothing we can do
            }

        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
            System.exit(-98);
        }
    }
}
