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

import static imatic8.Im8RelayInfo.RELAY_ALL_OFF;
import static imatic8.Im8RelayInfo.RELAY_ALL_ON;
import static imatic8.Im8RelayInfo.RELAY_OFF;
import static imatic8.Im8RelayInfo.RELAY_ON;

/**
 * Class to perform an action with the board, basically connect, send-message,
 * get response, disconnect&#46; Alongside non-board action such as pause/timer
 * or status requests.
 *
 * @author dbradley
 */
class Im8Action {

    private Im8BoardData boardController = null;

    /**
     * Board being controlled on.
     */
    private int boardN;

    /** ON or OFF action */
    Im8ProcessArgs.ArgType action;

    /** 1-8 or -1 for ALL, or the pause time value in seconds */
    int valueForAction;

    Im8Io m8Io;

    Im8Action(Im8Io m8Io, int boardN, Im8ProcessArgs.ArgType action, int valueForAction) {
        this.boardN = boardN;
        this.action = action;
        this.valueForAction = valueForAction;
        this.m8Io = m8Io;

        // the board controller object have already been set up so
        this.boardController = Im8BoardData.createReuseBoardNFromINI(m8Io, boardN);
    }

    /**
     * Set the relay number ON or all relays.
     *
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the sequence
     *
     * @return bytes of response or null if an error
     */
    byte[] setRelayOn(boolean closeConnectionOnCompletion) {
        int relayNumber = this.valueForAction;

        Im8RelayInfo actionL;
        if (relayNumber == -1) {
            actionL = RELAY_ALL_ON;
        } else {
            actionL = RELAY_ON;
        }
        // send the message
        return boardController.sendMessage2TheBoard(actionL, relayNumber, closeConnectionOnCompletion);
    }

    /**
     * Set the relay number OFF or all relays.
     *
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the sequence
     *
     * @return bytes of response or null if an error
     */
    byte[] setRelayOff(boolean closeConnectionOnCompletion) {
        int relayNumber = this.valueForAction;

        Im8RelayInfo actionL;
        if (relayNumber == -1) {
            actionL = RELAY_ALL_OFF;
        } else {
            actionL = RELAY_OFF;
        }
        // send the message
        return boardController.sendMessage2TheBoard(actionL, relayNumber, closeConnectionOnCompletion);
    }

    /**
     * Method to do a timer action as Java usually reports issues with a timer
     * in a loop.
     *
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the a sequence
     */
    void timerWaitAction(boolean closeConnectionOnCompletion) {
        int millisecond = this.valueForAction;

        try {
            // this is a timer delay between other actions
            // all timer values have been changed to milliseconds
            Thread.sleep(millisecond);
        } catch (InterruptedException ex) {
            // nothing we can do
        }
        if (closeConnectionOnCompletion) {
            this.boardController.closeCommunication(false);
        }
    }

    /**
     * Produce a status report out to the System.out of the relay states as they
     * pertain to the commands input (status from the board is unsupported so
     * any status report is a best-guess state of the relays).
     * <p>
     * The output would look like:
     * <pre>
     * Status:123-5-78     |    Status: --------     |     Status:12345678
     * on/off mixed         off all            on all
     *
     * digit = ON     dash = OFF
     * </pre>
     *
     * @param boardCtrl                   the board the action is for
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the a sequence
     */
    void statusReport(boolean closeConnectionOnCompletion) {
        this.boardController.reportRelayStates();

        if (closeConnectionOnCompletion) {
            this.boardController.closeCommunication(false);
        }
    }
}
