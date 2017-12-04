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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * Class that knows about boards data.
 *
 * @author dbradley
 */
class Imatic8BoardData {

     final static HashMap<Integer, Imatic8BoardData> boardNDataHash = new HashMap<>();

    private int boardNumber;
    private String boardIpAddr;
    private int portNo;

    private Socket socket4Client;

    private Imatic8RelayRecorder recorder;

    Imatic8BoardIni propIni;
    
    
    static Imatic8BoardData getBoardNInstance(Integer boardN){
        // reuse tge board controller
        if(boardNDataHash.containsKey(boardN)){
            return boardNDataHash.get(boardN);
        }
        // need to create a board controller, so tell the originator
        return createBoardNFromINI(boardN);
    }

    /**
     * A board is a real-world object.
     *
     * @param boardNumberP the board number to have
     *
     * @return the board object, null if there is no board INI file and thus
     * needs to be created
     */
    static Imatic8BoardData createBoardNFromINI(int boardNumberP) {
        // an INI file needs to exist for a board object to be created
        Imatic8BoardData nuBoarddata = new Imatic8BoardData(boardNumberP);
        
        nuBoarddata.boardNumber = boardNumberP;
        nuBoarddata.portNo = 30000;

        nuBoarddata.propIni = new Imatic8BoardIni(nuBoarddata);

        // get the IP address for the board from the INI file
        nuBoarddata.boardIpAddr = nuBoarddata.propIni.getIpAddrStr();
        
        return nuBoarddata;
    }

    static Imatic8BoardData defineBoardObject(int boardNumberP, String boardIpAddrP) {
        // it is known that a file does not exist, so it needs to be
        // created
        Imatic8BoardData nuBoarddata = new Imatic8BoardData(boardNumberP);

        nuBoarddata.boardNumber = boardNumberP;
        nuBoarddata.boardIpAddr = boardIpAddrP;
        nuBoarddata.portNo = 30000;

        nuBoarddata.recorder = new Imatic8RelayRecorder(nuBoarddata);
        nuBoarddata.propIni = new Imatic8BoardIni(nuBoarddata);

        // load properties will create the INI file if it does not exist
        nuBoarddata.propIni.loadProperties();

        return nuBoarddata;
    }

    private Imatic8BoardData(int boardNumberP) {
        //
        boardNDataHash.put(boardNumberP, this);
    }

    int getBoardNumber() {
        return this.boardNumber;
    }

    String getIpAddr() {
        return this.boardIpAddr;
    }

    String getPortString() {
        return String.format("%d", this.portNo);
    }

    int getPortNo() {
        return this.portNo;
    }

    Socket openCommunication() {

        if (socket4Client == null) {
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
        return this.socket4Client;
    }

    /**
     * Close the connection to the Imatic8 board and indicate so.
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
            System.err.println(errorMsg(ex1));
            socket4Client = null;
            return false;
        }
        return true;
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
    byte[] sendMessage2TheBoard(Imatic8RelayInfo action,
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
            if (!closeCommunication(true)) {
                return null;
            }
        }
        return bufferInputBytesArr;
    }

    private String errorMsg(IOException ex) {
        return String.format("ERROR IO: %s", ex.getMessage());
    }
    
    void reportRelayStates(){
        this.recorder.reportRelayStates(); 
    }

}
