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
 *
 * @author dbradley
 */
//99
public class Im8Socket {

    // "scktstr.Imatic8TestSocket"
    public static String testSocketObjectName = null;

    static Socket createSocket() {
        if (testSocketObjectName == null) {
            return new Socket();
        }
        // is the test socket class loaded
        Im8Socket self = new Im8Socket();
        ClassLoader cldr = self.getClass().getClassLoader();

        Socket testSocketObject;
        try {
            // is the Imatic8TestSocket class loaded
            Class<?> clzz = cldr.loadClass(testSocketObjectName);

            try {
                // get an instance of this class
                testSocketObject = (Socket) clzz.newInstance();
                System.err.println("creating test socket");
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(String.format("Test socket not working: %s", ex.getMessage()));
            }

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return new Socket();
        }
        return testSocketObject;
    }

    private Im8Socket() {

    }
}
