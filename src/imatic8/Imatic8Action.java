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

import static imatic8.Imatic8RelayInfo.RELAY_ALL_OFF;
import static imatic8.Imatic8RelayInfo.RELAY_ALL_ON;
import static imatic8.Imatic8RelayInfo.RELAY_OFF;
import static imatic8.Imatic8RelayInfo.RELAY_ON;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Class to perform an action with the board, basically connect, send-message,
 * get response, disconnect&#46; Alongside non-board action such as pause/timer
 * or status requests.
 *
 * @author dbradley
 */
class Imatic8Action {

    /** socket for this client to the Imatic8 board */
    private Socket socket4Client = null;

    /** the recorder for the relay states as they are set */
    private final Imatic8RelayRecorder recorder;

    Imatic8Action() {
        //
        recorder = new Imatic8RelayRecorder();
    }

    /**
     * Set the relay number ON or all relays.
     *
     * @param relayNumber                 1-8 relay number, 0 for all relays
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the sequence
     *
     * @return bytes of response or null if an error
     */
    byte[] setRelayOn(int relayNumber, boolean closeConnectionOnCompletion) {
        Imatic8RelayInfo action;
        if (relayNumber == -1) {
            action = RELAY_ALL_ON;
        } else {
            action = RELAY_ON;
        }
        // send the message
        return sendMessage2TheBoard(action, relayNumber, closeConnectionOnCompletion);
    }

    /**
     * Set the relay number OFF or all relays.
     *
     * @param relayNumber                 1-8 relay number, 0 for all relays
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the sequence
     *
     * @return bytes of response or null if an error
     */
    byte[] setRelayOff(int relayNumber, boolean closeConnectionOnCompletion) {
        Imatic8RelayInfo action;
        if (relayNumber == -1) {
            action = RELAY_ALL_OFF;
        } else {
            action = RELAY_OFF;
        }
        // send the message
        return sendMessage2TheBoard(action, relayNumber, closeConnectionOnCompletion);
    }

    /**
     * Method to do a timer action as Java usually reports issues with a timer
     * in a loop.
     *
     * @param millisecond                 the timer/wait action
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the a sequence
     */
    void timerWaitAction(int millisecond, boolean closeConnectionOnCompletion) {
        try {
            // this is a timer delay between other actions
            // all timer values have been changed to milliseconds
            Thread.sleep(millisecond);
        } catch (InterruptedException ex) {
            // nothing we can do
        }
        if (closeConnectionOnCompletion) {
            closeConnection(false);
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
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the a sequence
     */
    void statusReport(boolean closeConnectionOnCompletion) {
        this.recorder.reportRelayStates();

        if (closeConnectionOnCompletion) {
            closeConnection(false);
        }
    }

    /**
     * Send message to the board and return response.
     *
     * @param pMsg                        message to send
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the a sequence
     *
     * @return response in raw-data byte form
     */
    private byte[] sendMessage2TheBoard(Imatic8RelayInfo action,
            int relayNumber, boolean closeConnectionOnCompletion) {
        // get the bytes for the processing
        byte[] pMsg = action.getMessageBytesForRelayAction(relayNumber);

        // the board retains its last settings and as such connectAndSendMsg, 
        // do-action, disconnect will not affect the state-machine on the board
        //  
        // the assumption is that the server is up and running, if not
        // then go for a failure and return null
        //
        // connectAndSendMsg to server
        //    e.g.       $vm = 'PXDEPCSERV:20001';
        //
        // do we have a communication port to the Imatic8 board, if not get one
        if (this.socket4Client == null) {
            try {
                this.socket4Client = new Socket();
                // timeout if no connect within 2 seconds (as this is a local network
                // arrangement)
                this.socket4Client.connect(new InetSocketAddress(
                        Imatic8Constants.IMATIC8_IP_ADDR,
                        Imatic8Constants.IMATIC8_PORT_NO),
                        Imatic8Constants.TIMEOUT_FOR_CONNECTION_SETUP);

            } catch (IOException ex) {
                System.err.println(errorMsg(ex));
                this.socket4Client = null;
                return null;
            }
        }
        // send message to the board-server
        DataOutputStream toSvrData;
        try {
            this.socket4Client.setSoTimeout(1000);
            toSvrData = new DataOutputStream(socket4Client.getOutputStream());
            toSvrData.write(pMsg);

        } catch (IOException ex) {
            try {

                socket4Client.close();
            } catch (IOException ex1) {
                System.err.println(errorMsg(ex1));
            }
            System.err.printf("%s\n%s",
                    action.getFailureString(relayNumber),
                    errorMsg(ex));
            return null;
        }
        // get response from the board-server
        DataInputStream fromSvrData;
        byte[] bufferInputBytesArr = new byte[1024];
        int fromSvrDataNumBytes;
        try {
            fromSvrData = new DataInputStream(socket4Client.getInputStream());
            fromSvrDataNumBytes = fromSvrData.read(bufferInputBytesArr);

        } catch (IOException ex) {
            System.err.println(errorMsg(ex));
            fromSvrDataNumBytes = -1;
        }
        // only if there is a response do we set the relay state
        if (fromSvrDataNumBytes == -1) {
            // no response, so make it known
            System.err.printf("%s", action.getFailureString(relayNumber));
        } else {
            // 
            recorder.setRelayRecord(bufferInputBytesArr);
        }
        // 
        if (closeConnectionOnCompletion) {
            // close the connection port and indicate so by nulling the
            // socket variable
            if (!closeConnection(true)) {
                return null;
            }
        }
        return bufferInputBytesArr;
    }

    /**
     * Close the connection to the Imatic8 board and indicate so.
     *
     * @return false if failed to close
     */
    private boolean closeConnection(boolean requiredSocket) {

        if (!requiredSocket) {
            if (socket4Client == null) {
                return true;
            }
        }
        // close the connection port and indicate so by nulling the
        // socket variable
        try {
            socket4Client.shutdownOutput();
            socket4Client.shutdownInput();

            socket4Client.close();
            socket4Client = null;

        } catch (IOException ex1) {
            System.err.println(errorMsg(ex1));
            socket4Client = null;
            return false;
        }
        return true;
    }

    private String errorMsg(IOException ex) {
        return String.format("ERROR IO: %s", ex.getMessage());
    }
}
