/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imatic8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author dbradley
 */
public class Im8ModeInteractive {

    /**
     * Process with the input from console in interactive mode.
     */
    static void processInteractiveMode() {
        // interactive mode

        // launch a window frame for processing the relays
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ArrayList<String> tokenList = new ArrayList<>();

        // interactive mode will loop until an explicit exit/quit
        while (true) {
            try {
                // processing a line by line input until 'exit' is entered
                System.out.printf("I>");
                String readLn = reader.readLine();

                // convert the input line into tokens which represent
                // arguments
                String[] tokenArr = readLn.split(" ");
                tokenList.clear();

                for (String s : tokenArr) {
                    if (!s.isEmpty()) {
                        tokenList.add(s);
                    }
                }
                // change to an args String[] form
                String[] argsArr = tokenList.toArray(new String[tokenList.size()]);

                // have a basic tokenized, blank lines ignore
                if (!tokenList.isEmpty()) {
                    String arg0LC = argsArr[0].toLowerCase();

                    // this will system exit 
                    if (checkForExit(arg0LC)) {
                        System.exit(0);
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
