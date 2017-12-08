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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Program (main) that will interact with a Version 1.1 Imatic8 board to set
 * relays on or off on a compatible 8-channel 5V relay board&#46; The Imatic8
 * board does not support a relay status query, so this program simulates a best
 * guess 'status' via an INI file.
 * <p>
 * The Imatic8 control and relay board are separate boards that connect to allow
 * relays to be controlled. This program allows a user to control the relays via
 * command-line mode or in an interactive mode (or as java library). As a Java
 * application, provides OS platform independent operation of the relays. (Most
 * other S/W is C, C++ which needs to be compiled for specific OS platforms,
 * this Java program requires an installed JRE arrangement.)
 * <p>
 * IMPORTANT: The Imatic8 board lacks a means to query the relay states so this
 * program records the states as a best guest. It will be out of sync if there
 * are power outages, disconnects of either board or this controlling program.
 * <h1>Usage</h1>
 * <table class="mode">
 * <caption>operation modes</caption>
 * <tr>
 * <th style="background: #e6ffff;">Command line mode</th>
 * <th  style="background: #c6f3ed;">Interactive mode</th>
 * <th style="background: #ffffcc;">Library mode (Java)</th>
 * <tr>
 * <td style="background: #e6ffff;">
 * <pre>
 * Imatic8Prog.jar [arguments]&nbsp;
 * </pre>
 * </td>
 * <td style="background: #c6f3ed">
 * <pre>
 * Imatic8Prog.jar&nbsp;
 * I&gt;[arguments]
 * </pre>
 * </td>
 * <td style="background: #ffffcc;">
 * <pre>
 * Imatic8Prog.main(new String[](arg0, arg1, arg2........);&nbsp;
 * </pre>
 * </td>
 * </tr></table>
 * <p>
 * The [arguments] represent action(s) for processing relay on or off states,
 * with the ability to group actions, delay an action.
 * <table >
 * <caption>[arguments] (case insensitive)</caption>
 * <tr>
 * <td style="vertical-align:top">single relay</td>
 * <td style="vertical-align:top">
 * <pre> <span style="color: green;">ON 1</span>
 * <span style="color: magenta;">OFF 1</span>
 * </pre>
 * </td>
 * <td style="vertical-align:top"><pre>
 *ON n | off n
 *on n | OFF n
 * </pre>
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align:top">grouped relays</td>
 * <td style="vertical-align:top">
 * <pre> <span style="color: green;">on 1 8 7</span>
 * <span style="color: magenta;">off 2 3 5</span>
 * </pre>
 * </td>
 * <td style="vertical-align:top">on n [n [n...]]] | off n [n[n...]]]
 * </td>
 * </tr>
 *
 * <tr>
 * <td style="vertical-align:top">mix and match configuration</td>
 * <td style="vertical-align:top">
 * <pre> <span style="color: green;">on 1 8</span> <span style="color: magenta;">off 2 3 5</span>
 * <span style="color: green;">on 1</span> <span style="color: magenta;">off 2 3</span> <span style="color: green;">on 8</span> <span style="color: magenta;">off 5</span></pre>
 * </td>
 * <td style="vertical-align:top">mixed allows on or off in different ways
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align:top">pause between operations</td>
 * <td style="vertical-align:top">
 * <pre> <span style="color: green;">on 1</span> <span style="background: lime;font-style: bold;">s:10</span> <span style="color: green;">on 8</span> <span style="color: magenta;">off 2 3 5</span>
 * <span style="color: green;">on 1</span> <span style="color: magenta;">off 2</span> <span style="background: lime;font-style: bold;">ms:100</span> <span style="color: magenta;">off 3</span> <span style="color: green;">on 8</span> <span style="color: magenta;">off 5</span></pre>
 * </td>
 * <td style="vertical-align:top">
 * s:N is N seconds pause, ms:N is N milliseconds pause<br>
 * <div style="font-size: 0.75em; margin-left: 15px;">Note: time is approximate
 * as the time needed to operate physical should be added (so best time).</div>
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align:top">best guess relay status query</td>
 * <td style="vertical-align:top"><pre> status</pre></td>
 * <td style="vertical-align:top"><pre style="margin-bottom: 3px;">Status:123-5---</pre>
 * <div style="font-size: 0.75em; margin-left: 15px;">ON=digit (is output to the
 * 'System.out' stream)</div>
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align:top">help/license</td>
 * <td style="vertical-align:top"><pre> help | -help | /? | ? | license | l
 * </pre></td>
 * <td style="vertical-align:top"> <td>
 * </tr>
 * <tr>
 * <td style="background: #c6f3ed;vertical-align:top">interactive exit</td>
 * <td style="background: #c6f3ed;vertical-align:top"><pre> exit | quit | q</pre></td>
 * <td style="background: #c6f3ed;vertical-align:top"><i>only needed in
 * interactive mode</i></td>
 * </tr>
 * </table>
 *
 * @author dbradley
 */
public class Imatic8Prog {

    private Imatic8Prog() {
        //
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        if (args.length == 0) {
            // interactive mode
            processInteractiveMode();
        } else {
            // command line mode
            //
            // determine if help or define ip first
            //
            String arg0 = args[0].toLowerCase();

            if (checkForHelpOrLicense(arg0)) {
                System.exit(0);
            }
            if (processCheckForDefineIP(arg0, args)) {
                System.exit(0);
            }
            // command mode operation
            new Imatic8CommandLine().processCommandLineArgs(args);
        }
    }

    /**
     * Process with the input from console in interactive mode.
     */
    private static void processInteractiveMode() {
        // interactive mode

        // launch a window frame for processing the relays
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ArrayList<String> tokenList = new ArrayList<>();

        Imatic8CommandLine imaticCommandLine = new Imatic8CommandLine();

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
                boolean noneOperationCmds = false;

                if (!tokenList.isEmpty()) {
                    String arg0 = tokenList.get(0).toLowerCase();

                    checkForExit(arg0);
                    // if we get a 
                    noneOperationCmds |= checkForHelpOrLicense(arg0);
                    noneOperationCmds |= processCheckForDefineIP(arg0, argsArr);
                }
                if (!noneOperationCmds) {

                    imaticCommandLine.processCommandLineArgs(argsArr);
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
     */
    private static void checkForExit(String arg0) {
        // check for each exit command type in the 0 argument
        for (String exitStr : new String[]{"exit", "quit", "q"}) {
            if (arg0.equals(exitStr)) {
                System.exit(0); // user is quitting exit
            }
        }
    }

    /**
     * Check if a help/license is provided and output
     *
     * @param arg0LC string of lower-case argument 0
     *
     * @return true if a help or license request
     */
    private static boolean checkForHelpOrLicense(String arg0LC) {
        // check for each help/license command type in the 0 argument
        for (String exitStr : new String[]{"help", "l", "-help", "/?", "?", "license"}) {
            if (arg0LC.equals(exitStr)) {
                if (arg0LC.startsWith("l")) {
                    // user request license 
                    licensePrint();
                } else {
                    // user request help (/? is tpyically windows, all others are 
                    // Unix/Linux/Windows)
                    helpPrint();
                }
                return true;
            }
        }
        return false;
    }

    private static boolean processCheckForDefineIP(String arg0LC, String... args) {
        if (!arg0LC.startsWith("defip-")) {
            // none operation command in the queue is false
            return false;
        }
        // only two args are allowed
        if (args.length != 2) {
            System.err.printf("ERROR: defip-N has one argument following.\n", arg0LC);
            return true; // this will be an error condition so stop proceeding forward
        }
        // have define IP address and board command
        // 'defip-N nnn.nnn.nnn.nnn
        //
        // get the N
        String[] splitArg0Arr = arg0LC.split("-");

        if (splitArg0Arr.length != 2) {
            System.err.printf("ERROR: not defip-N format, found '%s'\n", arg0LC);
            return true; // this will be an error condition so stop proceeding forward
        }
        String nPart = splitArg0Arr[1];
        int nBoard;
        try {
            nBoard = Integer.parseInt(nPart);

        } catch (NumberFormatException ex) {
            System.err.printf("ERROR: not defip-N format N not digit, found '%s': %s \n",
                    arg0LC, ex.getMessage());
            return true;
        }
        if (nBoard < 1) {
            System.err.printf("ERROR: not defip-N format N needs to start at 1, found '%s'\n", arg0LC);
            return true;
        }
        // the following parameter needs to be an IPV4 address string
        String ipArg = args[1];

        String[] ipArgArr = ipArg.split("\\.");

        int ipLen = ipArgArr.length;
        if (ipLen != 4) {
            System.err.printf("ERROR: defip-N IP address not nnn.nnn.nnn.nnn (n.n.n.n) format\n", arg0LC);
            return true; // this will be an error condition so stop proceeding forward
        }
        // 
        boolean iperror = false;

        for (int i = 0; i < ipLen; i++) {
            // validate the string is an IPV4 address
            try {
                int value = Integer.parseInt(ipArgArr[i]);

                if (value < 0 || value > 255) {
                    System.err.printf("ERROR: defip-N IP field [%d] value not 0-255 error: %s\n", i, ipArgArr[i]);
                    iperror = true;
                }

            } catch (NumberFormatException ex) {
                System.err.printf("ERROR: defip-N IP field [%d] not number error: %s\n", i, ipArgArr[i]);
                iperror = true;
            }
        }
        // if an IP error occured then need to proceed
        if (iperror) {
            return true;
        }
        // create the INI file for the board and the IP address provided, as long
        // as there is not an existing one
        File boardsIniFile = Imatic8BoardIni.getBoardIniFile(nBoard);

        if (boardsIniFile.exists()) {
            System.err.printf("ERROR: defip-N: N %d already exists.\n"
                    + "      Need to delete file manually to overwrite.\n       %s\n",
                    nBoard,
                    boardsIniFile.getAbsolutePath());
            return true;
        }
        Imatic8BoardData definedBoardData = Imatic8BoardData.defineBoardObject(nBoard, ipArg);

        new Imatic8BoardIni(definedBoardData).loadProperties();

        // this was a success so continue
        return true;
    }

    /** The help brief documentation. */
    private static final String[] helpDocLinesArr = new String[]{
        "Usage: - Command-line mode   eg. Imatic8Prog.jar on 1 2 ms:500 on 3 s:10 off 1 s:2 off 2 3",
        " or    - Interactive mode    eg. Imatic8Prog.jar",
        "                                 I>b-1 on 1 2 ms:500 on 3 b-2 on 1",
        "[args...]",
        "   help | -help | /? | ? | license | l   [ exit | quit | q   - interactive only -]",
        " - setup -",
        "   defIP-N nnn.nnn.nnn.nnn         ( define an IP address to associate",
        "                                     with N used in b-N operation     )",
        " - operations -",
        "   b-N                             ( board N context of on/off/status",
        "                                     defaults b-1 if no b-N in each arguments line  )",
        "   on n [n [n...]]] | on all       ( on relays, b-1 if no preceding b-N )",
        "   off n [n [n...]]] | off all     ( off relays, b-1 if no preceding b-N )",
        "   s:N | ms:N                      ( pause N seconds/milliseconds )",
        "   status                          ( 'Status:b-1:12--5---'    b-N=board-N  digit=ON",
        "                                     b-1 if no preceding b-N",
        "                                      > board has no query, so best guess status <  )"
    };

    /** Print the help information. */
    private static void helpPrint() {
        for (String s : helpDocLinesArr) {
            System.out.println(s);
        }
    }

    private static final String[] licenseTxtArr = new String[]{
        "Copyright (c) 2017 dbradley.",
        "",
        " License: Imatic8Prog",
        "",
        " Free to use software and associated documentation (the \"Software\")",
        " without charge.",
        "",
        " Distribution, merge into other programs, copy of the software is",
        " permitted with the following a) to c) conditions:",
        "",
        " a) Software is provided as-is and without warranty of any kind. The user is",
        " responsible to ensure the \"Software\" fits their needs. In no event shall the",
        " author(s) or copyholder be liable for any claim, damages or other liability",
        " in connection with the \"Software\".",
        "",
        " b) Permission is hereby granted to modify the \"Software\" with sub-conditions:",
        " b.1) A 'Copyright (c) <year> <copyright-holder>.' is added above the original",
        " copyright line(s).",
        " b.2) The Main class name is changed to identify a different \"program\" name",
        " from the original.",
        "",
        " c) The above copyright notice and this permission and license notice shall",
        " be included in all copies or substantial portions of the \"Software\"."
    };

    /**
     * Print the license information.
     */
    private static void licensePrint() {
        for (String s : licenseTxtArr) {
            System.out.println(s);
        }
    }
}
