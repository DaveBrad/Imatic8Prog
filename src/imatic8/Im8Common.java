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

import static imatic8.Im8Io.ErrorKind.ERROR_ARG;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author dbradley
 */
class Im8Common {

    static String[] tokenSingleLine(String readLn) {
        ArrayList<String> tokenList = new ArrayList<>();

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
        return argsArr;
    }

    /**
     * Check if a help/license is provided and output
     *
     * @param arg0LC string of lower-case argument 0
     *
     * @return true if a help or license request
     */
    static boolean checkForHelpOrLicense(Im8Io m8Io, String arg0LC) {
        // check for each help/license command type in the 0 argument
        for (String exitStr : new String[]{"help", "l", "-help", "/?", "?", "license"}) {
            if (arg0LC.equals(exitStr)) {
                if (arg0LC.startsWith("l")) {
                    // user request license 
                    licensePrint(m8Io);
                } else {
                    // user request help (/? is tpyically windows, all others are 
                    // Unix/Linux/Windows)
                    helpPrint(m8Io);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Process the argument if it is a define IP address request.
     *
     * @param m8Io   IO object for message processing
     * @param arg0LC 0th argument in lower case
     * @param args   supporting arguments for the other settings of the IP
     *               definition
     *
     * @return true if processing 'defip-' or 'defip' query argument
     */
    static boolean processCheckForDefineIP(Im8Io m8Io, String arg0LC, String... args) {
        if (!arg0LC.startsWith("d")) {
            return false;
        }
        // is this a query for the defined IP and boards
        if (arg0LC.equals("defip")) {
            Im8BoardIni.printAllBoardIniFiles(m8Io);
            return true;
        }
        // is a define IP set request
        if (!arg0LC.startsWith("defip-")) {
            // not defip-
            return false;
        }
        // have define IP address and board command
        // 'defip-N nnn.nnn.nnn.nnn
        //
        // get the N
        String[] splitArg0Arr = arg0LC.split("-");

        if (splitArg0Arr.length != 2) {
            m8Io.err(-1).sprintf(ERROR_ARG, "not defip-N format, found '%s'.\n", arg0LC);
            return true; // this will be an error condition so stop proceeding forward
        }
        String nPart = splitArg0Arr[1];
        int nBoard;
        try {
            nBoard = Integer.parseInt(nPart);

        } catch (NumberFormatException ex) {
            m8Io.err(-1).sprintf(ERROR_ARG, "not defip-N format N not digit, found '%s': %s.\n",
                    arg0LC, ex.getMessage());
            return true;
        }
        if (nBoard < 1) {
            m8Io.err(-1).sprintf(ERROR_ARG, "not defip-N format N needs to start at 1, found '%s'.\n", arg0LC);
            return true;
        }
        // only two args are allowed so need to process the IP address
        if (args.length != 2) {
            m8Io.err(-1).sprintf(ERROR_ARG, "defip-%d requires IP address argument following.\n", nBoard, arg0LC);
            return true; // this will be an error condition so stop proceeding forward
        }
        // the following parameter needs to be an IPV4 address string
        String ipArg = args[1];

        String[] ipArgArr = ipArg.split("\\.");

        int ipLen = ipArgArr.length;
        if (ipLen != 4 || ipArg.endsWith(".")) {
            m8Io.err(-1).sprintf(ERROR_ARG, "defip-N IP address not nnn.nnn.nnn.nnn (n.n.n.n) format.\n", arg0LC);
            return true; // this will be an error condition so stop proceeding forward
        }
        // 
        boolean iperror = false;

        for (int i = 0; i < ipLen; i++) {
            // validate the string is an IPV4 address
            try {
                int value = Integer.parseInt(ipArgArr[i]);

                if (value < 0 || value > 255) {
                    m8Io.err(-1).sprintf(ERROR_ARG, "defip-N IP field [%d] value not 0-255 error: %s.\n", i, ipArgArr[i]);
                    iperror = true;
                }

            } catch (NumberFormatException ex) {
                m8Io.err(-1).sprintf(ERROR_ARG, "defip-N IP field [%d] not number error: %s.\n", i, ipArgArr[i]);
                iperror = true;
            }
        }
        // if an IP error occured then need to proceed
        if (iperror) {
            return true;
        }
        // create the INI file for the board and the IP address provided, as long
        // as there is not an existing one
        File boardsIniFile = Im8BoardIni.getBoardIniFile(m8Io, nBoard);

        if (boardsIniFile.exists()) {
            m8Io.err(-1).sprintf(ERROR_ARG, "defip-N: N %d already exists.\n"
                    + "      Need to delete file manually to overwrite.\n       %s\n",
                    nBoard,
                    boardsIniFile.getAbsolutePath());
            return true;
        }
        // define the board-N INI file
        Im8BoardIni.defineBoardNIni(m8Io, nBoard, ipArg);

        // this was a success so continue
        return true;
    }

    /** The help brief documentation. */
    private static final String[] helpDocLinesArr = new String[]{
        String.format("Usage: - Command-line mode = '%s.jar [args]'",
        Imatic8Prog.programName),
        
        String.format("          '%s.jar on 1 2 ms:500 on 3 s:10 off 1 s:2 off 2 b-2 on 1'",
        Imatic8Prog.programName),
        
        String.format(" or    - Interactive mode  = '%s.jar'", Imatic8Prog.programName),
        "          I>[ args | exit | quit | q]     [eg. I>'on 1 2 ms:500 on 3 ] b-2 on 4' | 'exit'",
        "          I> ---> next",
        "[args...]",
        "   help | -help | /? | ? | license | l",
        " - setup -",
        "   defIP-N nnn.nnn.nnn.nnn         ( define an IP address to associate with board-N )",
        "   defIP                           ( query the defined board-N to IP addresses  )",
        " - operations -",
        "   b-N                             ( set board N, ~ no b-N defaults to 'b-1' ~)",
        "   on n [n [n...]]] | on all       ( on relays, b-1 if no preceding b-N )",
        "   off n [n [n...]]] | off all     ( off relays, b-1 if no preceding b-N )",
        "   s:N | ms:N                      ( pause N seconds/milliseconds )",
        "   status                          ( 'Status:b-1:12--5---'    b-N=board-N  digit=ON",
        "                                       b-1 if no preceding b-N",
        "                                      > board has no query, so best guess status <  )"
    };

    /** Print the help information. */
    private static void helpPrint(Im8Io m8Io) {
        for (String s : helpDocLinesArr) {
            m8Io.out(0).sprintln(s);
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
        "    responsible to ensure the \"Software\" fits their needs. In no event shall the",
        "    author(s) or copyholder be liable for any claim, damages or other liability",
        "    in connection with the \"Software\".",
        "",
        " b) Permission is hereby granted to modify the \"Software\" with sub-conditions:",
        " b.1) A 'Copyright (c) <year> <copyright-holder>.' is added above the original",
        "      copyright line(s).",
        " b.2) The Main class name is changed to identify a different \"program\" name",
        "      from the original.",
        "",
        " c) The above copyright notice and this permission and license notice shall",
        "    be included in all copies or substantial portions of the \"Software\"."
    };

    /**
     * Print the license information.
     */
    private static void licensePrint(Im8Io m8Io) {
        for (String s : licenseTxtArr) {
            m8Io.out(0).sprintln(s);
        }
    }
}
