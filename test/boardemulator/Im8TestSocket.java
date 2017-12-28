/* Copyright (c) 2017 dbradley. All rights reserved. */
package boardemulator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 * Class that is used during test as an alternative to Im8Socket&#46;&nbsp;
 * Provides the means to employ the use of a test-shadow-board-server to emulate
 * a real board.
 * <p>
 * Provides the capability to emulate multiple boards on the network as it
 * interacts via command-line, interactive or library usage.</p>
 *
 *
 * @author dbradley
 */
public class Im8TestSocket extends Socket {

    private boolean testEnvironmentInstalled = false;

    public Im8TestSocket() {
        //
    }

    /**
     * Emulate connect of (Socket) but in the Imatic8Prog test environment, the
     * endpoint is changed to a shadow-endpoint which connects to a
     * shadow-board-server emulator of a 'real' board.
     * <p>
     * NOTE: the test socket CONNECT is the only one allowed.
     * <p>
     * Connects this socket to the server with a specified timeout value. A
     * timeout of zero is interpreted as an infinite timeout. The connection
     * will then block until established or an error occurs.
     * </p>
     *
     * @param endpoint the {@code SocketAddress}
     * @param timeout  the timeout value to be used in milliseconds.
     *
     * @throws IOException if an error occurs during the connection
     * @throws SocketTimeoutException if timeout expires before connecting
     * @throws java.nio.channels.IllegalBlockingModeException if this socket has
     * an associated channel, and the channel is in non-blocking mode
     * @throws IllegalArgumentException if endpoint is null or is a
     * SocketAddress subclass not supported by this socket
     * <p>
     * 'Connect' emulation that takes in a IP address:port of a real board
     * address and converts to a board-emulator as a test-shadow-board-server.
     *
     */
    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (endpoint == null) {
            throw new IllegalArgumentException("connect: The address can't be null");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("connect: timeout can't be negative");
        }
        // get a shadow board controller by translating a real-boardN-IpAddr to
        // a localhost:localport value and using the shadow server as the 
        // connect endPoint.
        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        String addressStr = epoint.getAddress().getHostAddress();
        int portInt = epoint.getPort();

        String brdAddrToShadow = String.format("%s:%s", addressStr, portInt);

        // the manner in which Imatic8Prog runs requires a different architecture
        // or inter-communication (IC) to emulato:
        //  1) interactive - IC requires server to communicate with the emulator server
        //  2) command-line - IC made be direct, but one-solution same as 1) is used
        //  3) Java library - has sub-manners of being used (one-solution same as 1) is used)
        //    .... this is not the perfect solution for the other program 
        //      - sub-manners -- in another interactive program
        //      - - - - - - - -- in anther command-line program
        //      - - - - - - - -- in any other program (IDE, server,......
        //
        InetSocketAddress sckAddr = Im8TestServerMgr4Emulators
                .getInstance().getShadowInetSocket2Use(brdAddrToShadow);

        // debug:       
        // System.err.printf("IP shadow connect request: %s\n", brdAddrToShadow);
        // debug:       
        // System.err.printf("Shadow too: %s\n", sckAddr.getAddress());
        super.connect(sckAddr, timeout);
    }

    /**
     * Connect to endpoint only unavailable for Imatic8Prog.
     *
     * @param endpoint the {@code SocketAddress)
     *
     * @throws RuntimeException if an attempt to use this connection type
     */
    @Override
    public void connect(SocketAddress endpoint) {
        throw new RuntimeException(
                String.format("%s.connect(endpoint); not permitted in test environment.",
                        Im8TestSocket.class.getName()));
    }
}
