/*
 * Copyright (c) 2017 dbradley.
 *
 * License: Imatic8Prog
 *
 * Free to use software and associated documentation (the "Software")
 * without charge.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author dbradley
 */
class Im8ModeInteractive {

    /**
     * Process with the input from console in interactive mode.
     */
    static void processInteractiveMode() {
        // interactive mode

        // launch a window frame for processing the relays
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // interactive mode will loop until an explicit exit/quit
        while (true) {
            try {
                // processing a line by line input until 'exit' is entered
                System.out.printf("I>");
                System.out.flush();
                String readLn = reader.readLine();

                // change to an args String[] form
                String[] argsArr = Im8Common.tokenSingleLine(readLn);

                // have a basic tokenized, blank lines ignore
                if (argsArr.length != 0) {
                    String arg0LC = argsArr[0].toLowerCase();

                    // this will system exit 
                    if (checkForExit(arg0LC)) {
                        System.out.println("Exiting program.");
                        return;
                    }
                    //  process the arguments
                    Im8Io m8Io = new Im8Io(argsArr);
                    m8Io.runAsLib();

                    m8Io.printReport();
                }
            } catch (IOException ex) {
                System.err.println("Unable to read from input/STDIN system.");
                System.exit(-1);
            }
        } // while true loop
    }

    /**
     * Check if an exit is provided in interactive mode and system exit
     *
     * @param arg0 string of lower-case argument 0
     *
     * @return true if exit/quit/q entered
     */
    private static boolean checkForExit(String arg0) {
        // check for each exit command type in the 0 argument
        for (String exitStr : new String[]{"exit", "quit", "q"}) {
            if (arg0.equals(exitStr)) {

                return true;
            }
        }
        return false;
    }
}
