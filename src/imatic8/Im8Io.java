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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Class that provides a JAR library interface to use the Imatic8Prog program.
 * <p>
 * Create an Im8Io object with an array of string arguments, as would be used in
 * command-line or interactive mode, then perform runAsLib.
 * <p>
 * There is an {Im8Io}.exitCode to determine success or error conditions.
 * {Im8Io}.getReportIntoArray() provides an array of the responses (either
 * System.out or System.err equivalent).
 * <pre>
 * :
 * :
 * Im8Io myMatic8 =  new Im8Io("on", "2");
 * if(myMatic8.exitCode == 0){
 * return;
 * }
 * // have errors
 * ArrayList&lt;String&gt; errMsgsArr = myMatic8.getReportIntoArray();
 *
 *   :  process the messages appropriately
 * </pre>
 *
 *
 * @author dbradley
 */
class Im8Io {

    private static boolean run1stTime = false;

    static String userDir = null;

    /**
     * Error messages from argument processing or during runtime are of these
     * kinds.
     */
    enum ErrorKind {
        /**
         * error is critical and is none recover-able. */
        CRITICAL("CRITICAL"),
        /**
         * error has been found at runtime during IO to the board. */
        ERROR_RT_IO("ERROR-rt IO"),
        /** the error has been found during */
        ERROR_ARG("ERROR-arg"),
        /** the
         * error has been found during processing of a board-N ini file */
        ERROR_INI("ERROR-ini");

        /** String that follows */
        final private String errorString;

        /**
         * Create the kind and its error string text
         *
         * @param errorStringP string of error kind description
         */
        ErrorKind(String errorStringP) {
            this.errorString = errorStringP;
        }

        /**
         * Get the prefix part to the message.
         *
         * @return string of prefix for error message
         */
        String getErrMsgPrefix() {
            return this.errorString;
        }
    }

    /** Copy of the argument strings */
    private String[] args;

    /**
     * The exit code value after processing has been completed. Use this to
     * determine any action in library use.
     */
    private int exitCode;

    private final Im8PseudoStreamOut out;
    private final Im8PseudoStreamErr err;

    /**
     * Create an instance that will process Imatic8Prog arguments and perform
     * actions.
     *
     * @param argsP String array of arguments
     */
    public Im8Io(String... argsP) {
        this.args = argsP;

        this.out = new Im8PseudoStreamOut();
        this.err = new Im8PseudoStreamErr();
    }
//99

    final Im8PseudoStreamErr err(int exitCodeP) {
        this.exitCode = exitCodeP;
        return this.err;
    }

    final Im8PseudoStreamOut out(int exitCodeP) {
        this.exitCode = exitCodeP;
        return this.out;
    }

    //99
    final static void setUserDir(String userDirP) {
        userDir = userDirP;
    }

    /**
     * Get the system userDir for programs running command-line or interactive
     * mode, or the stored user.dir set by the Imatic8LibMode (library mode).
     * <p>
     * The library mode may need to be run based on a specified different
     * 'user-directory' so control is required.
     * 
     * @return String of the user-dir to use 
     */
    final static String getUserDir() {
        if (userDir == null) {
            return System.getProperty("user.dir");
        }
        // the user directory has been explicitly set
        return userDir;
    }

    final int getExitCode() {
        return this.exitCode;
    }

    /**
     * Get the report(s) into an String array list depending on the exitCode
     * value&#46; If the exitCode is 0 getReportIntoArray provides 'System.out'
     * messages (queries: status, defip-?) or nothing&#46; If exitCode != 0 is
     * and error and getReportIntoArray provide 'System.err' messages.
     *
     * @return ArrayList&lt;String&gt; of 'err' or 'out' messages, as per the
     * exitCode
     */
    final public ArrayList<String> getReportIntoArray() {
        if (this.exitCode == 0) {
            return this.out.getBufferArray();
        }
        return this.err.getBufferArray();
    }

    /**
     * Prints the report appropriate to the exitCode to the appropriate
     * PrintStream. This is for internal interactive or command-line modes.
     */
    final void printReport() {
        if (this.exitCode == 0) {
            this.out.printOut(System.out);
        } else {
            this.err.printOut(System.err);
        }
    }

    /**
     * Run the Imatic8Prog as a library accessed object.
     * <p>
     * The run will fill in the exitCode variable 0= okay, !=0 is some error
     * found. Use getReportIntoArray to process the out or err messages within
     * the library call arrangement.
     */
    final public void runAsLib() {
        // flush out any old settings
        this.exitCode = -9999;

        this.err.clear();
        this.out.clear();

        // for testing purposes need to overlay a test environment
        checkTestEnvironment();

        //
        // command line mode
        //
        // determine if help or define ip first
        //
        String arg0 = args[0].toLowerCase();

        if (Im8Common.checkForHelpOrLicense(this, arg0)) {
            // this.exitCode = 0;
            return;
        }
        if (Im8Common.processCheckForDefineIP(this, arg0, args)) {
            // this.exitCode = 0 or -1;
            return;
        }
        // command mode operation
        new Im8ProcessArgs(this).doProcessArgs(args);
    }

    private void checkTestEnvironment() {
        if (!run1stTime) {
            determineSocketOrSocketEmulate();
            run1stTime = true;
        }
    }

    /**
     * Determine if settings for the emulator socket testing environment are in
     * place.
     *
     * @return the a Test-Socket/Socket class depending on emulate/not-emulate
     */
    private static void determineSocketOrSocketEmulate() {
        // used for testing of the application if the class is present in the
        // class path.
        //
        String emulatorClzzName = "imatic8.Im8BoardIniTest";

        try {
            // is the Imatic8TestSocket class loaded
            ClassLoader cldr = Im8Socket.class.getClassLoader();
            Class<?> clzz = cldr.loadClass(emulatorClzzName);
            // do the test emulate action to set the environment
            Object concreteSocket;
            try {
                // get an instance of this emulator class
                concreteSocket = clzz.newInstance();

            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(String.format("Test socket-emulator not working: %s", ex.getMessage()));
            }
            try {
                Method method = concreteSocket.getClass().getMethod("testEmulate");
                method.invoke(null);

            } catch (NoSuchMethodException | SecurityException | IllegalArgumentException
                    | InvocationTargetException | IllegalAccessException ex) {
                throw new RuntimeException(String.format("Test socket-emulator not working: %s", ex.getMessage()));
            }
            return;
        } catch (ClassNotFoundException ex) {
            // assume the Socket class due to Socket-emulator class unavailable 
            int a = 1;
        }
    }

    static String errorMsg(String frontMsg, IOException ex) {
        String constructMsg = "";

        if (frontMsg != null) {
            constructMsg = String.format("%s%s", constructMsg, frontMsg);
        }
        if (ex != null) {
            constructMsg = String.format("%s: %s", constructMsg, ex.getMessage());
        }
        constructMsg += ".";

        return constructMsg;
    }
}
