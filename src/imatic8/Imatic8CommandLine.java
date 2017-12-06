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

import static imatic8.Imatic8CommandLine.ArgType.ERROR_ARGUMENT;
import static imatic8.Imatic8CommandLine.ArgType.MS;
import static imatic8.Imatic8CommandLine.ArgType.RELAY_NUMBER;
import static imatic8.Imatic8CommandLine.ArgType.STATUS;
import static imatic8.Imatic8Constants.MAX_RELAY_NUMBER;
import static imatic8.Imatic8Constants.MIN_RELAY_NUMBER;
import java.io.File;
import java.util.ArrayList;

/**
 * Class to process the command line arguments.
 * <p>
 * This is used by command-mode and interactive-mode and will remove unwanted
 * token delimiters first, then validate (producing errors if needed), then
 * storing the operations into an array for action once validated.
 *
 * @author dbradley
 */
class Imatic8CommandLine {

    /**
     * The command-line argument array list of operations to perform in sequence
     * (as provided) once arguments are validated.
     */
    private final ArrayList<Imatic8Action> operationsList = new ArrayList<>();

    /**
     * The arguments that are represented as data units. These units may be
     * grouped and/or co-associated. See the Imatic8Prog help information.
     */
    enum ArgType {
        /** use board IP address for the b-N setting */
        BOARD,
        /** on relay request */
        ON,
        /** off relay request */
        OFF,
        /** this is a timer/pause in milliseconds. Seconds arguments
         * are converted to milliseconds on processing.
         */
        MS,
        /** action is to ALL relays (on
         * or off */
        ALL,
        /** relay number to action upon */
        RELAY_NUMBER,
        /** status request */
        STATUS,
        /** none
         * of the above is being processed, so there is an error */
        ERROR_ARGUMENT
    }

    Imatic8CommandLine() {
        //  
    }

    /**
     * Process the command line arguments so as to operate the relays or group
     * of relays, and any none relay operations.
     *
     * @param args String array of the arguments.
     */
    void processCommandLineArgs(String[] args) {
        operationsList.clear();

        // the board that is being operated upon may be changed
        // during the processing and the following represents which
        // board number is set
        //
        // the default board is '1' 
        int activeBoardN = 1;

        // on n [n [n [n...]]] | on all
        // off n [n [n [n...]]] | off all
        // sN wait seconds
        // msN wait milliseconds
        //
        // operate the relays in order of settings
        boolean operateOn = false;

        boolean timerIntroduced = false;

        boolean firstArgRead = false;
        boolean errorFound = false;

        for (String argI : args) {
            // the first time an ON or OFF is needed
            ArgType type = processArgType(firstArgRead, argI);

            switch (type) {
                case ERROR_ARGUMENT:
                case RELAY_NUMBER:
                    if (!firstArgRead) {
                        // critical error
                        System.err.printf("CRITICAL: missing argument to be 'on | off' for %s.\n", argI);
                        return;
                    }
                    // if a timer has been introduce then it has to be 
                    // explicitly overridden
                    if (timerIntroduced) {
                        System.err.printf("ERROR: Timer has preceded a relay number, not allowed: %s\n", argI);
                    } else {
                        // process the remain stuff

                        if (type == RELAY_NUMBER) {
                            if (!errorFound) {
                                // process the set-relay-action
                                ArgType actionType = operateOn ? ArgType.ON : ArgType.OFF;
                                int relayNum = processNargument(argI);

                                operationsList.add(new Imatic8Action(activeBoardN, actionType, relayNum));
                            }
                        } else {
                            // 
                            errorFound = true;
                            operationsList.clear();
                        }
                    }
                    break;
                case ALL:
                    timerIntroduced = false;
                    if (!errorFound) {
                        // process the set-relay-action for all
                        ArgType actionType = operateOn ? ArgType.ON : ArgType.OFF;
                        operationsList.add(new Imatic8Action(activeBoardN, actionType, -1));
                    }
                    break;
                case MS:
                    firstArgRead = false;

                    timerIntroduced = true;
                    // this is a timer action so store it away
                    if (!errorFound) {
                        // process a timer/pause/delay action
                        int timerInMs = processTimerArgument(argI);
                        operationsList.add(new Imatic8Action(activeBoardN, MS, timerInMs));
                    }
                    break;
                case STATUS:
                    firstArgRead = false;
                    if (!errorFound) {
                        operationsList.add(new Imatic8Action(activeBoardN, STATUS, 0));
                    }
                    break;
                case ON:
                    // this is a setting rather than an action
                    firstArgRead = true;

                    timerIntroduced = false;
                    operateOn = true;

                    break;
                case OFF:
                    // this is a setting rather than an action
                    firstArgRead = true;

                    timerIntroduced = false;
                    operateOn = false;

                    break;
                case BOARD:
                    // this is a setting rather than an action
                    //
                    //99 need to deal with bad board number
                    firstArgRead = true;

                    activeBoardN = processBoardArgument(argI);

                    // need to load the board so active board actions
                    // may be processed
                    if (activeBoardN > 0) {
                        if (!Imatic8BoardData.loadBoardObject(activeBoardN)) {
                            errorFound = true;
                            System.err.printf("ERROR: Board %d not defined: %s\n",
                                    activeBoardN, argI);
                        }
                    }
                    break;
            }
        }
        // if the command has no errors then we can process the 
        // actions
        if (!errorFound) {
            processAction();
        }
    }

    /**
     * Process an action that was from the command line.
     */
    private boolean processAction() {

        int lengthOfActionsToDo = this.operationsList.size();

        int lastItem = lengthOfActionsToDo - 1;
        for (int i = 0; i < lengthOfActionsToDo; i++) {

            Imatic8Action imaticAction = this.operationsList.get(i);

            boolean closeConnectionOnLastItem = (i == lastItem);
            switch (imaticAction.action) {
                case ON:
                    if (imaticAction.setRelayOn(closeConnectionOnLastItem) == null) {
                        // some error occurred which is unrecoverable
                        return false;
                    }
                    break;

                case OFF:
                    if (imaticAction.setRelayOff(closeConnectionOnLastItem) == null) {
                        // some error occurred which is unrecoverable
                        return false;
                    }
                    break;

                case MS:
                    // Java usually warns with a sleep-timer in a loop so
                    // use a method instead.
                    imaticAction.timerWaitAction(closeConnectionOnLastItem);
                    break;

                case STATUS:
                    imaticAction.statusReport(closeConnectionOnLastItem);
                    break;
                default:
                // not an action that is allowed so just ignore
            }
        }
        return true;
    }

    /**
     * Process argument(s) one-by-one through this process which will determine
     * the type of argument it is, as part of the validation process.
     *
     * @param firstOnOrOffArgRead relay-argument types cannot come before a on
     *                            or off argument
     * @param arg                 string of the argument to process
     *
     * @return ArgType the type of the argument
     */
    private ArgType processArgType(boolean firstOnOrOffArgRead, String arg) {

        switch (arg.toLowerCase()) {
            case "on":
                return ArgType.ON;
            case "off":
                return ArgType.OFF;
            case "all":
                return ArgType.ALL;
            case "status":
                return ArgType.STATUS;

            default:
                // one of 'b-', 's:', 'ms:' or 'N' for a relay number
                // that associates with a 'on'/'off' action'
                //
                int boardNValue = processBoardArgument(arg);
                if (boardNValue > 0) {
                    return ArgType.BOARD;
                }
                // not a boardN value, check timer or relay
                if (boardNValue == 0) {
                    // check for timer
                    int timerValue = processTimerArgument(arg);

                    if (timerValue > 0) {
                        return ArgType.MS;
                    }
                    // not a boardN/timer check for a relay N argument
                    if (timerValue == 0) {
                        // but only if the on or off has been read
                        if (firstOnOrOffArgRead) {
                            // expect a number for the relay to operate
                            int relayNum = processNargument(arg);
                            if (relayNum != -1) {
                                return ArgType.RELAY_NUMBER;
                            }
                        }
                    }
                }
            // timer or relay-number check proved to be in error

        }
        return ArgType.ERROR_ARGUMENT;
    }

    /**
     * Process a N argument is valid and real etc.
     *
     * @param arg string of argument
     *
     * @return 1-8 relay number, -1 bad argument not number
     */
    private int processNargument(String argP) {
        String arg = argP.toLowerCase();
        try {
            int value = Integer.parseInt(arg);

            if (value < MIN_RELAY_NUMBER || value > MAX_RELAY_NUMBER) {
                System.err.printf("ERROR: relay number is not in range %d-%d: %s\n",
                        MIN_RELAY_NUMBER, MAX_RELAY_NUMBER, arg);
            } else {
                return value;
            }

        } catch (NumberFormatException nfe) {
            System.err.printf("ERROR: relay number %s: %s\n", nfe.getMessage(), arg);
        }
        return -1;
    }

    /**
     * Process the timer argument.
     *
     * @param arg a timer argument string will be checked for
     *
     * @return > 0 a timer value, 0 not a timer string, -1 error detected
     */
    private int processTimerArgument(String argP) {
        String arg = argP.toLowerCase();

        // this can be a relay number or a timer setting
        String convertSeconds = null;
        boolean timerUnitIsMillisecond = false;

        if (arg.startsWith("s:")) {
            timerUnitIsMillisecond = false;
            convertSeconds = arg.substring(2);
        }
        if (arg.startsWith("ms:")) {
            timerUnitIsMillisecond = true;
            convertSeconds = arg.substring(3);
        }
        // 
        if (convertSeconds == null) {
            // not a timer argument
            return 0;
        }
        // this is a timer request to be processed
        // convert the secomds or milliseconds to an integer value
        try {
            int timerValue = Integer.parseInt(convertSeconds);

            if (timerValue == 0) {
                System.err.printf("ERROR: timer value of zero(0) makes no sense: %s\n", arg);
            } else {
                // there is no limit on the setting, but convert seconds to
                // a milliseconds
                if (!timerUnitIsMillisecond) {
                    timerValue = timerValue * 1000;
                }
                return timerValue;
            }

        } catch (NumberFormatException nfe) {
            System.err.printf("ERROR: timer value conversion: %s: %s\n", nfe.getMessage(), arg);
        }
        return -1;
    }

    /**
     * Process the board argument.
     *
     * @param arg a board argument string will be checked for
     *
     * @return > 0 a board value, -1 error detected
     */
    private int processBoardArgument(String argP) {
        String arg = argP.toLowerCase();

        // this can be a relay number or a timer setting
        String boardNString = null;

        if (arg.startsWith("b-")) {
            boardNString = arg.substring(2);
        }
        // 
        if (boardNString == null) {
            // not a board argument
            return 0;
        }
        // this is a timer request to be processed
        // convert the secomds or milliseconds to an integer value
        try {
            int boardValue = Integer.parseInt(boardNString);

            if (boardValue > 0) {
                // need to check that there is an INI file for this
                // board number, otherwise it is an error

                File ini4Board = Imatic8BoardIni.getBoardIniFile(boardValue);

                if (ini4Board.isFile()) {
                    return boardValue;
                }
                // if the boardN is 1 and there is no INI file for it, automatically
                // create the file, OTHERWISE this is an error
                if (boardValue == 1) {
                    // create the board1 INI file
                    Imatic8BoardData board1 = Imatic8BoardData.defineBoardObject(1, Imatic8Constants.IMATIC8_IP_ADDR);
                    return 1;
                }
                System.err.printf("ERROR: no INI file for board %d, need to use 'defip-%d n.n.n.n' to define.\n",
                        boardValue, boardValue);

            } else {
                System.err.printf("ERROR: board value should be greater than 0: %s\n", arg);
            }

        } catch (NumberFormatException nfe) {
            System.err.printf("ERROR: board value conversion: %s: %s\n", nfe.getMessage(), arg);
        }
        return -1;
    }
}
