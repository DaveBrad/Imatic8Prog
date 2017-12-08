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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Class that provides board address settings for relay actions.
 * <p>
 * The IP address of a board may be changed via special TTL connections (not
 * ethernet), so some users may wish to use their IP addresses instead.
 *
 * @author dbradley
 */
public class Imatic8BoardIni extends Properties {

    private final static String RECORDER_FILE_NAME = "Imatic8Record%s.ini";

    private final static String PROP_IP_STRING = "ip";
    private final static String PROP_PORT_STRING = "port";
    
      /** INI off state */
    public static String IMATIC8_INI_OFF_STATE = "off";
    
      /** INI on state */
    public static String IMATIC8_INI_ON_STATE = "on";

    private final Imatic8BoardData boardData;

    /**
     * The full path for the properties file.
     */
    private final File propertyFile;

    Imatic8BoardIni(Imatic8BoardData boardData) {
        this.boardData = boardData;
        this.propertyFile = getBoardIniFile(this.boardData.getBoardNumber());
    }

    /**
     * Get the File object for the file-path of a board number INI file.
     *
     * @param boardNumberP board number
     *
     * @return board number File object
     */
    static File getBoardIniFile(int boardNumberP) {
        return new File(new File(System.getProperty("user.dir")),
                String.format(RECORDER_FILE_NAME, boardNumberP));
    }

    String getIpAddrStr() {
        this.loadProperties();

        return this.getProperty(PROP_IP_STRING);
    }

    int getPortNo() {
        this.loadProperties();

        String portStr = this.getProperty(PROP_PORT_STRING);
        int portNo;
        try {
            portNo = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            int brdNo = this.boardData.getBoardNumber();

            System.err.printf("ERROR: INI fail: board %s INI file corrupt: %s\n   %s\n ",
                    brdNo, ex.getMessage(),
                    getBoardIniFile(brdNo).getAbsolutePath());
            portNo = Imatic8Constants.IMATIC8_PORT_NO;
        }
        return portNo;
    }

    /**
     * Load the INI properties file that is the best guess of the relay states.
     * Creates the INI at 'off all' if not found.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    void loadProperties() {
        try {
            FileInputStream iStream = new FileInputStream(propertyFile);

            try {
                this.load(iStream);
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
            if (this.boardData.getBoardNumber() == 1) {
                // store the IP address for the board number and its port-no
                this.setProperty(PROP_IP_STRING, Imatic8Constants.IMATIC8_IP_ADDR);
            } else {
                // store the IP address for the board number and its port-no
                this.setProperty(PROP_IP_STRING, this.boardData.getIpAddr());
            }
            this.setProperty(PROP_PORT_STRING, this.boardData.getPortString());

            // store the relay states 
            for (int i = MIN_RELAY_NUMBER; i <= MAX_RELAY_NUMBER; i++) {
                this.setProperty(
                        String.format("R%d", i), IMATIC8_INI_OFF_STATE);// R1 R2 ......
            }
            this.storeProperties();
        }
    }

    /** Store the relay states properties to the INI. */
    @SuppressWarnings("CallToPrintStackTrace")
    void storeProperties() {
        FileOutputStream oStream;
        try {
            oStream = new FileOutputStream(propertyFile);
            try {
                this.store(oStream, "Imatic8 relay states (best guess)");

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
