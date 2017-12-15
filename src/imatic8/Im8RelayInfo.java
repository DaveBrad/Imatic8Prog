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

import static imatic8.Im8Constants.RELAY_ALL_NUMBER;
import static imatic8.Im8Constants.RELAY_ALL_OFF_CODE;
import static imatic8.Im8Constants.RELAY_ALL_ON_CODE;
import static imatic8.Im8Constants.RELAY_OFF_CODE;
import static imatic8.Im8Constants.RELAY_ON_CODE;
import static imatic8.Im8Constants.RELAY_NUMBER_BYTE_INDEX;
import static imatic8.Im8Constants.ON_OFF_STATE_BYTE_INDEX;
import static imatic8.Im8Constants.RELAY_MSG_ARRAY;

/**
 * Enum that represents operations information to send to the Imatic8 board
 * to cause relay action.
 * 
 * @author dbradley
 */
 enum Im8RelayInfo {

    RELAY_ON,
    RELAY_OFF,
    RELAY_ALL_ON,
    RELAY_ALL_OFF;

    /**
     * Get the message bytes for the action to be performed.
     *
     * @param relayNumber 1-8 relay to operate on (ALL relay is ignored)
     *
     * @return byte[] of the message to do the action
     */
     byte[] getMessageBytesForRelayAction(int relayNumber) {

        byte[] byteMessageArr = RELAY_MSG_ARRAY.clone();

        switch (this) {
            case RELAY_ON:
                byteMessageArr[RELAY_NUMBER_BYTE_INDEX] = (byte) (relayNumber & 0xFF);
                byteMessageArr[ON_OFF_STATE_BYTE_INDEX] = RELAY_ON_CODE;
                break;
            case RELAY_ALL_ON:
                byteMessageArr[RELAY_NUMBER_BYTE_INDEX] = RELAY_ALL_NUMBER;
                byteMessageArr[ON_OFF_STATE_BYTE_INDEX] = RELAY_ALL_ON_CODE;
                break;
            case RELAY_OFF:
                byteMessageArr[RELAY_NUMBER_BYTE_INDEX] = (byte) (relayNumber & 0xFF);
                byteMessageArr[ON_OFF_STATE_BYTE_INDEX] = RELAY_OFF_CODE;
                break;
            case RELAY_ALL_OFF:
                byteMessageArr[RELAY_NUMBER_BYTE_INDEX] = RELAY_ALL_NUMBER;
                byteMessageArr[ON_OFF_STATE_BYTE_INDEX] = RELAY_ALL_OFF_CODE;
                break;
            default:
                // missing code if this happens
                throw new RuntimeException(String.format("incorrect call on type: %s\n", this));
        }
        return byteMessageArr;
    }

    /**
     * Get a failure message for a relay action&#46; Typically an issue with
     * connection to the Imatic8 boards port, or unavailable network connection.
     *
     * @param relayNumber 1-8 relay number, -1 is all
     *
     * @return the message to display
     */
     String getFailureString(int relayNumber) {
        String[] actionArr = this.toString().split("_");

        // want a "on" or "off" string from the action,and then
        // followed by N (relay number) or "all"
        return String.format("Failed request: %s %s\n",
                actionArr[actionArr.length - 1].toLowerCase(),
                relayNumber == -1 ? "all" : relayNumber);
    }
}
