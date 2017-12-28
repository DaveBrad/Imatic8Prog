/* Copyright (c) 2017 dbradley. */
package boardemulator;

import imatic8.Im8BoardIniTest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import static org.fest.reflect.core.Reflection.staticField;

/**
 * Class that is a server and managers the conversion between real-board IPs and
 * shadow-board-server-emulator shadow-IPs.
 * <p>
 * When Imatic8Prog requests are made for a board, a real-IP is assumed and used
 * in any socket-connect invokes to a real board. In the test environment any
 * socket-connect is "shadowed" to a shadow-board-server-emulator:port that is
 * an emulator server of a board (basic software only).
 *
 * @author dbradley
 */
public class Im8TestServerMgr4Emulators {

    /**
     * Chose which
     */
    public enum IpAddressKind {
        /**
         * The local loop (0.0.0.0, 127.0.0.1)
         */
        LOCAL_LOOP,
        /**
         * The best network address that is found to be externally accessible by
         * other computers in a network.
         */
        NETWORK
    }

    /** variable for singleton object */
    private static Im8TestServerMgr4Emulators instance;

    // server-side
    private ServerSocket svrSocket4Mgr;

    private Thread svrMgrThread = null;

    // client-side
    private String serversIP;
    private int serversPort;

    /**
     * key: real-board-ip-key is a board address to shadow (ipAddr:port
     * 192.168.1.4:30000 to shadow 'localhost':port)
     *
     * element: a shadow-board-server IP address
     * <code>Im8TestSocket.connect(real-board-ip, timeout)</code> calls.
     */
    private static HashMap<String, String> mapRealToShadowIpAddressHash = new HashMap<>();

    /**
     * Get the singleton instance of this class as only one manager is needed to
     * manage many/multiple shadow-board-server-emulator.
     *
     * @return Im8TestServerMgr4Emulators object
     */
    public static Im8TestServerMgr4Emulators getInstance() {
        if (instance == null) {
            instance = new Im8TestServerMgr4Emulators();
        }
        return instance;
    }

    private Im8TestServerMgr4Emulators() {
        // this is a singleton
    }

    /**
     * Get the server manager IP address for the use in the
     * -Dtst="n.n.n.n:ppppp" option for interactive testing.
     *
     * @param ipKind
     *
     * @return string 'n.n.n.n:ppppp'
     */
    public String getServerMgrIp(IpAddressKind ipKind) {

        // start the server if it not running
        startTestServerMgr();

        switch (ipKind) {
            case LOCAL_LOOP:
                // get the address for this host
                this.serversIP = this.svrSocket4Mgr.getInetAddress().getHostAddress();
                break;
            case NETWORK:
                String hostname = this.svrSocket4Mgr.getInetAddress().getHostName();
                try {
                    InetAddress some = InetAddress.getByName(hostname);

                    String ip2 = some.getHostAddress();

                    int a = 1;
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }

                this.serversIP = getLocalAddress().getHostAddress();
                break;
        }
        return getServerMgrIp(this.serversIP);
    }

    /**
     * Get the server manager IP address for use in the -Dtst="n.n.n.n:ppppp"
     * option for interactive testing.
     *
     * @param ipAddressString n.n.n.n to explicitly used
     *
     * @return string 'n.n.n.n:ppppp'
     */
    public String getServerMgrIp(String ipAddressString) {
        // start the server if it not running
        startTestServerMgr();

        this.serversPort = this.svrSocket4Mgr.getLocalPort();

        String serverAddressStr = String.format("%s:%s", ipAddressString, this.serversPort);
        System.err.printf("Server mgr address: %s\n", serverAddressStr);

        return serverAddressStr;
    }

    private static InetAddress getLocalAddress() {

        try {
            Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
            while (b.hasMoreElements()) {
                for (InterfaceAddress f : b.nextElement().getInterfaceAddresses()) {
                    if (f.getAddress().isSiteLocalAddress()) {
                        return f.getAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Start the test server manager (only one needed) when a test script
     * invokes (say)
     * <pre>Im8TestShadowBoardSvr.createEmulatorForIP("192.168.1.4", 30000);</pre>
     * <p>
     * Using a different IP address will create multiple boards, but all
     * <b>served</b>
     * from a single server manager.
     */
    synchronized void startTestServerMgr() {

        // svrMgrThread acts as a sema to ensure only one server manager is running
        if (svrMgrThread == null) {

            try {
                this.svrSocket4Mgr = new ServerSocket(0);

            } catch (IOException ex) {
                // too critical error
                throw new RuntimeException("No Server Socket allocated", ex.getCause());
            }

            Runnable svrMgrRunnable = new Runnable() {
                @Override
                public void run() {
                    runTestServerMgr();
                }
            };
            // start the thread for the server manager to run in
            svrMgrThread = new Thread(svrMgrRunnable);
            svrMgrThread.start();
        }
    }

    /**
     * (Server-side) Run the server manager.
     * <p>
     * Incoming messages will be a real-board-IP address and the response will
     * be the shadow-IP-address.
     */
    private void runTestServerMgr() {
        while (true) {
            try {
                Socket clientSocket = this.svrSocket4Mgr.accept();

                // each message coming in requires different actions:
                // 1) respond right away
                // 2) go into a wait-on state on the client
                try (
                        PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {

                    // we expect a single message from the client and need to do the action
                    String inputLine = fromClient.readLine();

                    if (inputLine.startsWith("SETIP>")) {
                        toClient.println(setRealToShadowIP(inputLine));
                    } else {
                        // return the shadowIp information
                        toClient.println(getRealToShadowIP(inputLine));
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } // while true
    }

    /**
     * Set the real to shadow ip:port addresses in the servers table. //99
     *
     * @param realIpKey    n.n.n.n:p
     * @param shadowIpAddr
     */
    void setRealToShadowIp2ServerManager(String realIpKey, String shadowIpAddr) {
        mapRealToShadowIpAddressHash.put(realIpKey, shadowIpAddr);
    }

    /**
     * Server-side interface for setting the table.
     *
     * @param inputLine
     *
     * @return
     */
    private String setRealToShadowIP(String inputLine) {

        // 'SETIP>realIp:port>shadowIp:port
        String[] real2ShadowIpsArr = inputLine.split(">");

        if (real2ShadowIpsArr.length != 3) {
            return String.format("ERROR::%s:", inputLine);
        }
        // not going to validate the information as this is a closed
        // system.
        setRealToShadowIp2ServerManager(real2ShadowIpsArr[1], real2ShadowIpsArr[2]);
        return String.format(inputLine);
    }

    private String getRealToShadowIP(String inputLine) {
        // input should be a board:port address that is the shadowIpKey
        InetSocketAddress shadowIpSocketAddr = getShadowInetSocketAddress(inputLine);

        if (shadowIpSocketAddr == null) {
            return "UNKNOWN";
        }
        byte[] shadowIpByteArr = shadowIpSocketAddr.getAddress().getAddress();

        String ipAddrAsString = Im8TestShadowBoardSvr.getIpAddrString(shadowIpByteArr);

        // return the shadowIp information
        return String.format("%s:%d", ipAddrAsString, shadowIpSocketAddr.getPort());
    }

    /**
     * (Client-side) Get the shadow-IP-address that maps to the real-board-IP
     * address.
     *
     * @param realBoardIpKey a real boards IP address to be shadowed too
     *
     * @return
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public InetSocketAddress getShadowInetSocket2Use(String realBoardIpKey) {

        // this is done once to ebstablish running with
        // the server manage needs to be known and 
        //
        // the following java command string will launch an extera\nal terminal for a 
        // the Imatic8Prog and with the test/classes and -Dim8emulater setting will
        // cause operation to be in 
        //
        // terminalImatic8Prog = String.format(
        //         "java -Dim8emulator=\"%s\" -cp ../build/classes;../build/test/classes imatic8.Imatic8Prog",
        //        Im8TestServerMgr4Emulators.getInstance().getServerMgrIp(LOCAL_LOOP)); // NETWORK LOCAL_LOOP));
        //
        if (serversIP == null) {
            // there are 2 conditions
            // 1) intwractive
            // 2) command-line
            //99
            String tst = System.getProperty("im8emulator");

            if (tst != null) {
                //99          
                String[] tstArr = tst.split(":");
                // ip:port
                serversIP = tstArr[0];
                serversPort = Integer.parseInt(tstArr[1]);
            } else {
                // 2)
                // load the IP and port information
                byte[] serversIPByteArr = this.svrSocket4Mgr.getInetAddress().getAddress();
                serversIP = Im8TestShadowBoardSvr.getIpAddrString(serversIPByteArr);
                serversPort = this.svrSocket4Mgr.getLocalPort();
            }
        }

        try (
                // the assumption is that the server is up and running, if not
                // then go for a failure and return null
                Socket clientToSvrMgrSckt = new Socket(serversIP, serversPort);
                PrintWriter toSvr = new PrintWriter(clientToSvrMgrSckt.getOutputStream(), true);
                BufferedReader fromSvr = new BufferedReader(
                        new InputStreamReader(clientToSvrMgrSckt.getInputStream()));) {

            // send request to server
            toSvr.println(realBoardIpKey);

            // we need a response from the computer name server,
            String fromServerStr = fromSvr.readLine();

            clientToSvrMgrSckt.close();

            if (fromServerStr == null) {
                System.err.println("SERVER disconnect.........");
                return null;
            }
            if (fromServerStr.equals("UNKNOWN")) {
                System.out.println("UNKNOWN shadow, or server not running");
                //99 use stop server to test is a better method and more controlled
                return null;
            }

            // create the InetAddress object
            // n.n.n.n:mmmmm
            String[] fromServerStrArr = fromServerStr.split(":");

            InetSocketAddress shadowInetSckAddr = new InetSocketAddress(fromServerStrArr[0],
                    Integer.parseInt(fromServerStrArr[1]));

            return shadowInetSckAddr;

        } catch (UnknownHostException e) {
            // information for test  environment
            e.printStackTrace();
        } catch (IOException e) {
            // information for test  environment
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the InetSocketAddress for the the realBoardIpKey, this may be an
     * emulator (shadow) or an actual board address (none emulator)
     *
     * @param realBoardIpKey IP address of a board (192.168.1.4:30000) to be
     *                       shadowed
     *
     * @return InetSocketAddress IP address for socket
     */
    private static InetSocketAddress getShadowInetSocketAddress(String realBoardIpKey) {
        if (mapRealToShadowIpAddressHash.containsKey(realBoardIpKey)) {
            String brdSvrStr = mapRealToShadowIpAddressHash.get(realBoardIpKey);
            String[] brdSvrStrArr = brdSvrStr.split(":");

            return new InetSocketAddress(brdSvrStrArr[0],
                    Integer.parseInt(brdSvrStrArr[1]));
        }
        // assume use the real board IP and port addresses
        String[] ipSplitAddrArr = realBoardIpKey.split(":");

        return new InetSocketAddress(
                ipSplitAddrArr[0],
                Integer.parseInt(ipSplitAddrArr[1]));
    }
}
