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

/**
 * Class that contains constants for messages and positions of the bytes in the
 * message for on/off operation (send and receive).
 * <p>
 * The Imatic8 board does not support a relay status query.
 *
 * @author dbradley
 */
class Im8Constants {

    /** 1 */
    static int MIN_RELAY_NUMBER = 1;
    /** 8 */
    static int MAX_RELAY_NUMBER = 8;

  
    /** Connection send/receive timeout */
    static final int TIMEOUT_FOR_CONNECTION_SETUP = 2000;

    // send messages indexes and codes 
    /** Relay byte location index in the send message array. */
    static final int RELAY_NUMBER_BYTE_INDEX = 3;

    /** Relay state (on/off) byte location index in the send message array. */
    static final int ON_OFF_STATE_BYTE_INDEX = 4;

    /** Single relay action for on. */
    static final byte RELAY_ON_CODE = 0x01;

    /** Single relay action for off. */
    static final byte RELAY_OFF_CODE = 0x00;

    /** Value for ALL relay operation 'relay number'. */
    static final byte RELAY_ALL_NUMBER = (byte) 0xF8;

    /** All relay action for on. */
    static final byte RELAY_ALL_ON_CODE = (byte) 0x88;

    /** All relay action for off. */
    static final byte RELAY_ALL_OFF_CODE = (byte) 0x80;

    // response message indexes
    /** Relay byte location index in the response message array. */
    static final int RESPONSE_RELAY_NUMBER_BYTE_INDEX = 1;

    /** Relay state (on/off) byte location index in the response message array. */
    static final int RESPONSE_ON_OFF_STATE_BYTE_INDEX = 2;

    /**
     * Byte array of message to be sent to the Imatic8 board where indexes 3 and
     * 4 need to be filled/changed for relay number and on/off state values
     * before sending&#46;
     *
     * <br>FD 02 20 &lt;relayNo&gt; &lt;on/off&gt; 5D
     */
    static final byte[] RELAY_MSG_ARRAY = new byte[]{
        (byte) 0xfd, // [0]
        (byte) 0x02, // [1]
        (byte) 0x20, // [2]
        (byte) 0xff, // [3] relay number
        (byte) 0xff, // [4] on of off state
        (byte) 0x5D // [5]
    };
}
