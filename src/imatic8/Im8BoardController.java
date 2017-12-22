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

import static imatic8.Im8Io.ErrorKind.ERROR_RT_IO;
import static imatic8.Im8Io.errorMsg;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that knows about boards data.
 *
 * @author dbradley
 */
class Im8BoardController {

    private final static HashMap<Integer, Im8BoardController> boardNDataHash = new HashMap<>();

    private int boardN;
    private String boardIpAddr;
    private int boardPortNo;

    private Socket socket4Client;

    private Im8RelayRecorder recorder;

    Im8Io m8Io;

    Im8BoardIni propIni;

    /**
     * A board is a real-world object.
     *
     * @param boardNumberP the board number to have
     *
     * @return the board object, null if there is no board INI file and thus
     * needs to be created
     */
    static Im8BoardController createReuseBoardNFromINI(Im8Io m8IoP, int boardNumberP) {
        if (boardNDataHash.containsKey(boardNumberP)) {

            // modify the m8Io setting as it is different for reuse
            Im8BoardController nuBoardData = boardNDataHash.get(boardNumberP);
            nuBoardData.m8Io = m8IoP;

            return nuBoardData;
        } else {
            // the board has not been defined so likely also not
            // have a default board-1 INI setup either
            if (boardNumberP == 1) {
                Im8BoardIni.defineDefaultBoard1(m8IoP);
            }
        }
        // an INI file needs to exist for a board object to be created
        Im8BoardController nuBoarddata = new Im8BoardController(m8IoP, boardNumberP);

        nuBoarddata.boardN = boardNumberP;
        nuBoarddata.boardPortNo = 30000;

        nuBoarddata.propIni = new Im8BoardIni(m8IoP, boardNumberP);

        // get the IP address for the board from the INI file
        nuBoarddata.boardIpAddr = nuBoarddata.propIni.getIpAddrStr();

        return nuBoarddata;
    }

    static Im8BoardController defineBoardObject(Im8Io m8Io, int boardNumberP, String boardIpAddrP) {
        // it is known that a file does not exist, so it needs to be
        // created
        Im8BoardController nuBoarddata = new Im8BoardController(m8Io, boardNumberP);

        nuBoarddata.boardN = boardNumberP;
        nuBoarddata.boardIpAddr = boardIpAddrP;
        nuBoarddata.boardPortNo = 30000;

        nuBoarddata.recorder = new Im8RelayRecorder(nuBoarddata);
        nuBoarddata.propIni = new Im8BoardIni(m8Io, nuBoarddata.getBoardNumber());

        // load properties will create the INI file if it does not exist
        nuBoarddata.propIni.loadProperties();

        return nuBoarddata;
    }

    static boolean loadBoardObject(Im8Io m8IoP, int boardNumberP) {
        if (!boardNDataHash.containsKey(boardNumberP)) {
            // the board data object has not been loaded, so load it
            Im8BoardController nuBoardData = createReuseBoardNFromINI(m8IoP, boardNumberP);

            if (nuBoardData == null) {
                return false;
            }
        } else {
            // modify the m8Io setting as it is different
            Im8BoardController nuBoardData = boardNDataHash.get(boardNumberP);
            nuBoardData.m8Io = m8IoP;
        }
        return true; //  means exists
    }

    /**
     * Create a board data object for board N.
     *
     * @param m8IoP        the IO object when processing messages
     * @param boardNumberP N board number
     */
    @SuppressWarnings("LeakingThisInConstructor")
    private Im8BoardController(Im8Io m8IoP, int boardNumberP) {
        //
        this.m8Io = m8IoP;
        boardNDataHash.put(boardNumberP, this);
        this.recorder = new Im8RelayRecorder(this);
    }

    /**
     * Get the board number.
     *
     * @return integer board N value
     */
    int getBoardNumber() {
        return this.boardN;
    }

    /**
     * Get the IPV4 address string for board N.
     *
     * @return string of IPV4 address
     */
    String getIpV4Addr() {
        return this.boardIpAddr;
    }

    /**
     * Get the port number for board N as a string.
     *
     * @return string of a port number
     */
    String getPortString() {
        return String.format("%d", this.boardPortNo);
    }

    /**
     * Get the port number for board N as an integer.
     *
     * @return integer of a port number
     */
    int getPortNo() {
        return this.boardPortNo;
    }

    /**
     * Open the communication socket to board N if the socket is already closed.
     *
     * @return Socket if open, null if an error occurs
     */
    @SuppressWarnings("CallToPrintStackTrace")
    private void openCommunication() {

        if (socket4Client == null) {
            try {
                try {
                    this.socket4Client = Im8Socket.createSocket();
                    
                } catch (InstantiationException | IllegalAccessException ex) {
                    ex.printStackTrace();
                    
                    this.m8Io.err(-99).sprintln(ERROR_RT_IO,
                            errorMsg(String.format("create sck fail: b-%d  \n%s:%s", 
                                    this.getBoardNumber(),
                                    this.boardIpAddr, this.boardPortNo),
                                    null));
                }

                // timeout if no connect within 2 seconds (as this is a local network
                // arrangement)
                this.socket4Client.connect(new InetSocketAddress(
                        this.boardIpAddr,
                        this.boardPortNo),
                        Im8Constants.TIMEOUT_FOR_CONNECTION_SETUP);

            } catch (IOException ex) {
                this.m8Io.err(-99).sprintln(ERROR_RT_IO,
                        errorMsg(String.format("open comm: b-%d  \n%s:%s", this.getBoardNumber(),
                                this.boardIpAddr, this.boardPortNo),
                                ex));
                this.socket4Client = null;
            }
        }
    }

    /**
     * Close the connection to board N and indicate so.
     *
     * @return false if failed to close
     */
    boolean closeCommunication(boolean requiredSocket) {

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
            m8Io.err(-99).sprintln(ERROR_RT_IO,
                    errorMsg(String.format("close comm: b-%d", this.getBoardNumber()
                    ), ex1));
            socket4Client = null;
            return false;
        }
        return true;
    }

    /**
     * Send message to board N and return response.
     *
     * @param pMsg                        message to send
     * @param closeConnectionOnCompletion true if this is the last request in
     *                                    the a sequence
     *
     * @return response in raw-data byte form
     */
    byte[] sendMessage2TheBoard(Im8RelayInfo action,
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
        openCommunication();
        if (this.socket4Client == null) {
            return null;
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
                this.m8Io.err(-96).sprintln(ERROR_RT_IO,
                        errorMsg(String.format("close: b-%d %d", this.getBoardNumber(), relayNumber),
                                ex1));
                return null;
            }
            this.m8Io.err(-97).sprintln(ERROR_RT_IO,
                    errorMsg(String.format("write: b-%d %d", this.getBoardNumber(), relayNumber),
                            ex));
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
            this.m8Io.err(-96).sprintln(ERROR_RT_IO,
                    errorMsg(String.format("read: b-%d %d", this.getBoardNumber(), relayNumber),
                            ex));
            fromSvrDataNumBytes = -2;
        }
        // only if there is a response do we set the relay state
        if (fromSvrDataNumBytes == -1) {

            // no response, so make it known
            this.m8Io.err(-92).sprintf(ERROR_RT_IO,
                    errorMsg(String.format("response: b-%d %d", this.getBoardNumber(), relayNumber),
                            null));

        }
        if (fromSvrDataNumBytes > 0) {
            recorder.setRelayRecord(bufferInputBytesArr);
        }
        // 
        if (closeConnectionOnCompletion) {
            // close the connection port and indicate so by nulling the
            // socket variable
            if (!closeCommunication(true)) {
                return null;
            }
        }
        if (fromSvrDataNumBytes > 0) {
            return bufferInputBytesArr;
        }
        return null;
    }

    /**
     * Report the states of the relays of board N.
     */
    void reportRelayStates() {
        this.recorder.reportRelayStates();
    }
}
