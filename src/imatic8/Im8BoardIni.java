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

import static imatic8.Im8Constants.MAX_RELAY_NUMBER;
import static imatic8.Im8Constants.MIN_RELAY_NUMBER;
import static imatic8.Im8Io.ErrorKind.CRITICAL;
import static imatic8.Im8Io.ErrorKind.ERROR_INI;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
 * Class that provides board address settings for relay actions.
 * <p>
 * The IP address of a board may be changed via special TTL connections (not
 * ethernet), so some users may wish to use their IP addresses instead.
 *
 * @author dbradley
 */
class Im8BoardIni extends Properties {

    /** IP address default */
    static final String IMATIC8_IP_ADDR = "192.168.1.4";

    /** Imatic8 port number */
    private static final int IMATIC8_PORT_NO = 30000;

    private final static String RECORDER_FILE_NAME_LEAD = "Imatic8Record";
    private static String RECORDER_FILE_NAME = RECORDER_FILE_NAME_LEAD + "%s.ini";

    private final static String PROP_IP_STRING = "ip";
    private final static String PROP_PORT_STRING = "port";

    /** INI off state */
    static String IMATIC8_INI_OFF_STATE = "off";

    /** INI on state */
    static String IMATIC8_INI_ON_STATE = "on";

    private final int boardN;

    //99
    private Im8Io m8Io;

    /**
     * The full path for the properties file.
     */
    private final File propertyFile;

    Im8BoardIni(Im8Io m8Io, int boardN) {
        this(boardN);
        this.m8Io = m8Io;
    }

    private Im8BoardIni(int boardN) {
        this.boardN = boardN;
        this.propertyFile = getBoardIniFile(boardN);
    }

    static boolean defineDefaultBoard1(Im8Io m8Io) {
        return defineBoardNIni(m8Io, 1, IMATIC8_IP_ADDR);
    }

    static boolean defineBoardNIni(Im8Io m8Io, int boardN, String ipV4Adress) {
        Im8BoardIni defineIni = new Im8BoardIni(boardN);
        defineIni.m8Io = m8Io;

        // need to create the file for the first time
        // store the IP address for the board number and its port-no
        defineIni.setProperty(PROP_IP_STRING, ipV4Adress);
        defineIni.setProperty(PROP_PORT_STRING, String.format("%d", IMATIC8_PORT_NO));

        // store the relay states 
        for (int i = MIN_RELAY_NUMBER; i <= MAX_RELAY_NUMBER; i++) {
            defineIni.setProperty(
                    String.format("R%d", i), IMATIC8_INI_OFF_STATE);// R1 R2 ......
        }
        return defineIni.storeProperties();
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

   
    /**
     * Print all INI file settings to out stream.
     *
     * @param m8IoP
     */
    static void printAllBoardIniFiles(Im8Io m8IoP) {

        String userDir = System.getProperty("user.dir");

        File userDirFile = new File(userDir);

        File[] allIniFilesArr = userDirFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                // find all files that are of INI formats for Imatic8Prog.
                String pathnameStr = pathname.getAbsolutePath().replaceAll("\\\\", "/");

                if (pathnameStr.toLowerCase().endsWith(".ini")) {
                    String[] pathPartsArr = pathnameStr.split("/");
                    if (pathPartsArr[pathPartsArr.length - 1].startsWith(RECORDER_FILE_NAME_LEAD)) {
                        return true;
                    }
                }
                return false;
            }
        });
        // display the directory for the INI files
        m8IoP.out(0).sprintf("INI file directory:\n %s\n", userDir);

        if (allIniFilesArr.length == 0) {
            m8IoP.out(0).sprintln("There are no board-N INI files.");
        } else {
            // we have file(s), so we need to get them all
            //
            // there may be none sequential files in the list, so need to extract the 
            // board N value from the file name and then sort into order
            int leadStringLen = RECORDER_FILE_NAME_LEAD.length();

            // array for the board-N number
            ArrayList<Integer> boardNArr = new ArrayList<>();

            for (File iniFile : allIniFilesArr) {
                String pathnameStr = iniFile.getAbsolutePath().replaceAll("\\\\", "/");

                String[] pathPartsArr = pathnameStr.split("/");

                String iniFileStr = pathPartsArr[pathPartsArr.length - 1];
                // remove .ini part, and remove the lead string
                String iniFileNStr = iniFileStr.substring(leadStringLen, iniFileStr.length() - 4);

                int boardN = Integer.parseInt(iniFileNStr);
                boardNArr.add(boardN);
            }
            // sort into order
            Collections.sort(boardNArr);
            // format the defined board-N, IP address and port number being used
            for (Integer boardNInt : boardNArr) {
                Im8BoardIni realIniProperties = new Im8BoardIni(boardNInt);

                String ipAddr = realIniProperties.getIpAddrStr();
                int ipPort = realIniProperties.getPortNo();

                m8IoP.out(0).sprintf("b-%s : %s:%d\n", boardNInt, ipAddr, ipPort);
            }
        }
        // this is a okay return
        m8IoP.out(0);
    }

    /**
     * Get the IP address for the INI properties-file.
     *
     * @return String of nnn.nnn.nnn.nnn of an IP address, NULL if an error
     * processing INI file occurred
     */
    String getIpAddrStr() {
        if (this.loadProperties()) {
            return this.getProperty(PROP_IP_STRING);
        }
        return null;
    }

    /**
     * Get the port number for the INI properties-file.
     *
     * @return integer of port number, -1 an error was detected
     */
    int getPortNo() {
        if (this.loadProperties()) {
            // the properties load was okay
            String portStr = this.getProperty(PROP_PORT_STRING);
            int portNo;
            try {
                portNo = Integer.parseInt(portStr);
                return portNo;

            } catch (NumberFormatException ex) {
                this.m8Io.err(-10).sprintf(ERROR_INI, "board %s INI file corrupt: %s\n   %s\n ",
                        this.boardN, ex.getMessage(),
                        getBoardIniFile(this.boardN).getAbsolutePath());
            }
        }
        return -1;
    }

    /**
     * Load the INI properties file that is the best guess of the relay states.
     * Creates the INI at 'off all' if not found.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    boolean loadProperties() {
        try {
            FileInputStream iStream = new FileInputStream(propertyFile);

            try {
                this.load(iStream);
            } catch (IOException ex) {
                ex.printStackTrace();
                this.m8Io.err(-97).sprintln(CRITICAL, "IO error, see trace.");
                return false;
            }
            try {
                iStream.close();
            } catch (IOException ex) {
                // nothing can be done;
            }

        } catch (FileNotFoundException ex) {
            // need to create the file for the first time
            this.m8Io.err(-96).sprintf(ERROR_INI, "no INI file for board-N: %d", this.boardN);
            return false;
        }
        return true;
    }

    /**
     * Store the relay states properties to the INI.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    boolean storeProperties() {
        FileOutputStream oStream;
        try {
            oStream = new FileOutputStream(propertyFile);
            try {
                this.store(oStream, "Imatic8 relay states (best guess)");

            } catch (IOException ex1) {
                ex1.printStackTrace();
                this.m8Io.err(-99).sprintln(CRITICAL, "IO error, see trace.");
                return false;
            }
            try {
                oStream.close();
            } catch (IOException ex) {
                // nothing we can do
            }

        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
            this.m8Io.err(-98).sprintln(CRITICAL, "IO error, see trace.");
            return false;
        }
        return true;
    }
}
