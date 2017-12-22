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

import java.net.Socket;

/**
 * Class that provides a socket for communication to a board-server port of a
 * specified board IP address.
 * <p>
 * The board-server may be real or testing-emulator depending on availability of
 * an emulator class. As the Imatic8Prog application is simple and explicit,
 * there is a low security risk or need to hack the application as it is client
 * invoked and all controlled by what is in the class-path.
 *
 * @author dbradley
 */
class Im8Socket {

    /** the class of the socket or socket-emulator to create a Socket from. */
    private static Class<?> socketProvidedClass = null;

    private Im8Socket() {
        //
    }

    /**
     * Create a socket (real or emulated) for the Imatic8Prog to use while
     * running.
     *
     * @return Socket object to connect too
     *
     * @throws InstantiationException socket creation issue
     * @throws IllegalAccessException socket creation issue
     */
    static Socket createSocket() throws InstantiationException, IllegalAccessException {
        // once determined to be either Socket or Socket-emulate manner for the create socket
        // Im8Socket will remain working in that manner. (That is, once set is set for
        // JVM's runtime life.
        if (socketProvidedClass != null) {
            // get an instance of this class
            return ((Class<Socket>) socketProvidedClass).newInstance();
        }
        // otherwise this is the first time and need to determine if this is
        // is running in a test-environment for either Socket or Socket-emulate manner.
        //
        // this may be socket of the emulator-socket-test class type
        // (all depends on settings in place for the runtime environment) 
        socketProvidedClass = determineSocketOrSocketEmulate();

        return ((Class<Socket>) socketProvidedClass).newInstance();
    }

    /**
     * Determine if settings for the emulator socket testing environment are in
     * place.
     *
     * @return the a Test-Socket/Socket class depending on emulate/not-emulate
     */
    private static Class<?> determineSocketOrSocketEmulate() {
        // used for testing of the application if the class is present in the
        // class path.
        //
        String emulatorClzzName = "boardemulator.Im8TestSocket";

        try {
            ClassLoader cldr = Im8Socket.class.getClassLoader();

            // is the Imatic8TestSocket class loaded
            Class<?> clzz = cldr.loadClass(emulatorClzzName);

            try {
                // get an instance of this emulator class
                Object concreteSocket = clzz.newInstance();
                return (Class<Socket>) concreteSocket.getClass();

            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(String.format("Test socket not working: %s", ex.getMessage()));
            }
        } catch (ClassNotFoundException ex) {
            // assume the Socket class due to no Socket-emulator class available 
        }
        return Socket.class;
    }
}
