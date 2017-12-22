/* Copyright (c) 2017 dbradley. All rights reserved. */
package boardemulator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used as an emulation of an Imatic8 board for testing purposes&#46; The
 * class acts as a server on the network (local host) in the same manner as a
 * board is a server on the network.
 *
 * @author dbradley
 */
public class Im8TestShadowBoardSvr {

    /**
     * key: real-IP-key
     *
     * element: the shadow-board-server associated with the real-IP-key
     *
     * This map represents the emulator server and should not be confused with
     * Im8TestServerMgr4Emulators map of real-IP to shadow-IP used for
     * inter-process connection.
     */
    private static HashMap<String, Im8TestShadowBoardSvr> mapForShadowServer = new HashMap<>();

    private ServerSocket svrSocket;

    private String shadowIpKey;

    /** 1 */
    static int MIN_RELAY_NUMBER = 1;
    /** 8 */
    static int MAX_RELAY_NUMBER = 8;

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

    // 
    // process error conditions to affect the Imatic8Prog
    //
    private Runnable runableSvr = null;
    private Thread boardSvrThreadAsEmulator = null;

    private boolean testNoResponseToOnOrOff;

    private boolean testBadResponse;
    private final static byte[] badResponseBytes = new byte[]{10, 3, 5, 27};

    private int testAlteredRelayNumber = -1;

    private int testAlternateOnOffValue = -1;

    private int testTimeoutOfResponse = -1;

    private final static int endMsgByteCount = 20;

    private final static int[] OnOrOffMsgBytesArr = new int[]{0, 0xfd, 1, 0x02, 2, 0x20, 5, 0x5d};

    private Im8TestShadowBoardSvr() {
        //
        clearTestConditions();
    }

    /**
     * Create a shadow board emulator as a server for the IP of a real board's
     * IP address.
     * <p>
     * The server will be used instead of a real board during testing. //99
     *
     * @param realBoardIp string n.n.n.n of a real board IP address
     * @param portNo      integer of boards port
     *
     * @return the shadow-board-server emulator object
     */
    @SuppressWarnings("CallToPrintStackTrace")
    static public Im8TestShadowBoardSvr createEmulatorForIP(String realBoardIp, int portNo) {

        // the test server manager needs to be running, only one is needed on the
        // local host (dealt with by Im8TestServerMgr4Emulators).
        Im8TestServerMgr4Emulators.getInstance().startTestServerMgr();

        try {
            //
            String realIpKey = String.format("%s:%s", realBoardIp, portNo);
            return Im8TestShadowBoardSvr.runBoardServer(realIpKey);

        } catch (IOException ex) {
            // in the test environment this is useful
            ex.printStackTrace();
        }
        throw new RuntimeException("Unable to create a Imatic8BoardServer emulator.");
    }

    /**
     * Clear the test conditions that emulate various failure or success states
     * for a server. Typically conditions are all cleared after any one
     * condition has been used, that is, the user needs to set the condition
     * when required in a test.
     */
    private void clearTestConditions() {
        this.testAlteredRelayNumber = -1;
        this.testAlternateOnOffValue = -1;
        this.testTimeoutOfResponse = -1;

        this.testBadResponse = false;
        testNoResponseToOnOrOff = false;

    }

    /**
     * Create and run a thread for a shadow-board-server as an emulator of a
     * real board.
     *
     * @param realBoardIpKey the board IP and port address to be shadowed
     *
     * @return the board server object for the test environment to process too
     *
     * @throws IOException unable to process a thread/server-socket
     */
    static private Im8TestShadowBoardSvr runBoardServer(String realBoardIpKey) throws IOException {

        Im8TestShadowBoardSvr brdSrver;

        if (!mapForShadowServer.containsKey(realBoardIpKey)) {
            // create an Imatic8 board server
            brdSrver = new Im8TestShadowBoardSvr();
            brdSrver.shadowIpKey = realBoardIpKey;

            brdSrver.svrSocket = new ServerSocket(0);
            mapForShadowServer.put(realBoardIpKey, brdSrver);

            //
            Im8TestServerMgr4Emulators.getInstance()
                    .setRealToShadowIp2ServerManager(realBoardIpKey, brdSrver.getShadowIpKey());
        }
        brdSrver = mapForShadowServer.get(realBoardIpKey);

        brdSrver.startServer();

        return brdSrver;
    }
    
    

    private void startServer() {
        // prepare to start a thread to emulate the board server
        final Im8TestShadowBoardSvr brdSrverFinal = this;

        brdSrverFinal.runableSvr = new Runnable() {
            @Override
            public void run() {
                brdSrverFinal.boardServerEmulate();
            }
        };
        //
        brdSrverFinal.boardSvrThreadAsEmulator = new Thread(brdSrverFinal.runableSvr);
        brdSrverFinal.boardSvrThreadAsEmulator.start();
    }

    private String getShadowIpKey() {
        return String.format("%s:%d", getSvrIpAddrStr(), getSvrPortNo());
    }

    private String getSvrIpAddrStr() {
        byte[] ipAddrByteArr = this.svrSocket.getInetAddress().getAddress();
        return getIpAddrString(ipAddrByteArr);
    }

    public static String getIpAddrString(byte[] ipAddrByteArr) {
        return String.format("%d.%d.%d.%d",
                (int) ipAddrByteArr[0] & 0xFF,
                (int) ipAddrByteArr[1] & 0xFF,
                (int) ipAddrByteArr[2] & 0xFF,
                (int) ipAddrByteArr[3] & 0xFF);
    }

    private int getSvrPortNo() {
        return this.svrSocket.getLocalPort();
    }

    /**
     * The board server emulator that process incoming messages.
     */
    private void boardServerEmulate() {
        System.err.printf("Board svr emulate on port: %s to %s\n",
                this.svrSocket.getLocalPort(),
                this.shadowIpKey);

        try {
            // wait on a message for ever

            while (true) {
                Socket clientSocket = this.svrSocket.accept();

                // each message coming in requires different actions:
                // 1) respond right away
                // 2) go into a wait-on state on the client
                DataOutputStream toClient
                        = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                DataInputStream fromClient
                        = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

                byte[] bytesArr = new byte[1024];
                int bytesRead = fromClient.read(bytesArr);

                if (processsEndServer(bytesArr, bytesRead)) {
                    toClient.close();
                    this.svrSocket.close();

                    return;
                }
                // 
                byte[] response = performAction(bytesArr, bytesRead);

                toClient.write(response);
                toClient.flush();
            }
        } catch (IOException e) {
            String reason = e.getMessage();

            // do nothing but go back for processing more is the case
            // for all reason and unknowns
            switch (reason) {
                case "client Socket is closed":
                    System.out.printf("\n%s\nBye", reason);
                    break;

                case "socket closed":
                    // do nothing but go back for processing more
                    break;

                case "Connection reset":
                    System.out.printf("\n%s\nBye", reason);
                    break;
                default:
                    // more critical condition
                    System.err.printf("Unknown: listen on port termination %s\n", reason);
                    break;
            } // end switch
        }
    }

    /**
     * Test end the server as if there is no board.
     *
     * * @param timerSeconds time seconds to pause/wait for server to end
     */
    public void testEndServer(int timerSeconds) {
        if (this.boardSvrThreadAsEmulator == null) {
            return;
        }
        // build the end message
        byte[] endMessageArr = new byte[endMsgByteCount];
        for (int i = 0; i < endMsgByteCount; i++) {
            endMessageArr[i] = (byte) 0xEF;
        }
        // send the message
        try {
            Socket endSocketClient = new Socket(this.getSvrIpAddrStr(), this.getSvrPortNo());

            endSocketClient.setSoTimeout(1000);
            DataOutputStream toSvrData = new DataOutputStream(endSocketClient.getOutputStream());
            toSvrData.write(endMessageArr);

        } catch (IOException ex) {
            Logger.getLogger(Im8TestShadowBoardSvr.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        this.boardSvrThreadAsEmulator = null;

        try {
            Thread.sleep(timerSeconds * 1000);
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Test restart the server.
     *
     * @param timerSeconds time seconds to pause/wait for server to restart
     */
    public void testRestartServer(int timerSeconds) {
        if (this.boardSvrThreadAsEmulator != null) {
            return;
        }
        try {
            //  int reusePort = this.svrSocket.getLocalPort()
            //  this.svrSocket = new ServerSocket(reusePort);
            //
            // but the above the port may have been reused for another program/thread
            this.svrSocket = new ServerSocket(0);
            Im8TestServerMgr4Emulators.getInstance().setRealToShadowIp2ServerManager(shadowIpKey, getShadowIpKey());

        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
        // 
        this.startServer();

        try {
            Thread.sleep(timerSeconds * 1000);
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Test response with different relay-value.
     *
     * @param alternateRelayN integer 0-255 for setting an alternate relay-N
     */
    public void testAlternateRelayN(int alternateRelayN) {

        this.testAlteredRelayNumber = alternateRelayN;
    }

    /**
     * Test response with different relay-actions.
     *
     * @param alternateOnOffValue integer
     */
    public void testAlternateOnOffValue(int alternateOnOffValue) {

        this.testAlternateOnOffValue = alternateOnOffValue;
    }

    /**
     * Test response with a timeout timer before message response.
     *
     * @param timeoutOfResponse integer timer in milliseconds
     */
    public void testTimeout(int timeoutOfResponse) {

        this.testTimeoutOfResponse = testTimeoutOfResponse;
    }

    /**
     * Process incoming message to determine if this is end-server request so as
     * to emulate no-board condition.
     *
     * @param byteArrIn the message in bytes
     * @param bytesRead number of bytes in message
     *
     * @return
     */
    private boolean processsEndServer(byte[] byteArrIn, int bytesRead) {
        if (bytesRead != endMsgByteCount) {
            return false;
        }
        byte efByte = (byte) 0xEF;
        // 
        for (int i = 0; i < endMsgByteCount; i++) {
            if (byteArrIn[i] != efByte) {
                return false;
            }
        }
        return true;
    }

    private byte[] performAction(byte[] byteArrIn, int bytesRead) {
        byte[] outBytesArr;

        if (byteArrIn[0] == (byte) 0xfd) {
            outBytesArr = processMessage2Board(byteArrIn, bytesRead);
            if (this.testBadResponse) {
                outBytesArr = badResponseBytes;

            } else if (this.testNoResponseToOnOrOff) {
                outBytesArr = new byte[0];
            } else if (testAlteredRelayNumber != -1) {
                // override the relay number
                int concateInt = testAlteredRelayNumber & 0xff;

                outBytesArr[RESPONSE_RELAY_NUMBER_BYTE_INDEX] = (byte) concateInt;

            } else if (testAlternateOnOffValue != -1) {
                // override the relay actions
                int concateInt = testAlternateOnOffValue & 0xff;

                outBytesArr[RESPONSE_ON_OFF_STATE_BYTE_INDEX] = (byte) concateInt;

            } else if (testTimeoutOfResponse != -1) {
                try {
                    // override the relay number
                    Thread.sleep(testTimeoutOfResponse);
                } catch (InterruptedException ex) {
                    //
                }
            }
            //
            clearTestConditions();
            return outBytesArr;
        }
        return null;
    }

    private byte[] processMessage2Board(byte[] byteArrIn, int bytesRead) {
        // depending on the in bytes some form of response is necessary
        // for the most parts only
        // (byte) 0xfd, // [0]
        // (byte) 0x02, // [1]
        // (byte) 0x20, // [2]
        // (byte) 0xff, // [3] relay number
        // (byte) 0xff, // [4] on of off state
        // (byte) 0x5D // [5]
        //
        // ON:
        // OFF: 

        // this is a on or off request so just check the basics
        for (int i = 0; i < OnOrOffMsgBytesArr.length; i += 2) {
            int inIndexByte = OnOrOffMsgBytesArr[i];
            int inExpectedByteValueInt = OnOrOffMsgBytesArr[i + 1];

            int inByteInt = (int) (byteArrIn[inIndexByte] & 0xff);

            if (inByteInt != inExpectedByteValueInt) {
                // we have an error in the input message, so need to report
                // this somehow to the test-case
                throw new RuntimeException("Invalid message from client to board");
            }
        }
        // verify the relay and on-off state are valid ranges
        int relayN = (int) byteArrIn[3] & 0xff;
        byte relayAction = byteArrIn[4];

        if ((relayN < MIN_RELAY_NUMBER && relayN > MAX_RELAY_NUMBER)
                && relayN != ((int) RELAY_ALL_NUMBER & 0xff)) {
            // we have an error in the input message, so need to report
            // this somehow to the test-case
            throw new RuntimeException(
                    String.format("Invalid relay-N from client to board:%s", relayN));
        }
        // has to equal one of the types of action, and if not is an error
        boolean actionValid = false;

        actionValid |= relayAction == RELAY_OFF_CODE;
        actionValid |= relayAction == RELAY_ON_CODE;
        actionValid |= relayAction == RELAY_ALL_OFF_CODE;
        actionValid |= relayAction == RELAY_ALL_ON_CODE;

        if (!actionValid) {
            // we have an error in the input message, so need to report
            // this somehow to the test-case
            throw new RuntimeException(
                    String.format("Invalid relay-N action provided from client to board:%s", relayAction));

        }
        // need to construct the response to send back for a successful action
        int[] bytePatternResponse = new int[]{0, 3, 4, 5};

        byte[] byteResponseArr = new byte[bytePatternResponse.length];
        for (int i = 0; i < bytePatternResponse.length; i++) {
            byteResponseArr[i] = byteArrIn[bytePatternResponse[i]];
        }

        return byteResponseArr;
    }

}
