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

import imatic8.Im8Io.ErrorKind;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Class that represent an IO print stream with some basic methods (not the same
 * a PrintStream) which allows an internal System.out and System.err equivalent.
 * <p>
 * As arguments and processing-actions is done there are standard and error
 * messages as appropriate. These messages are stored in this class and then
 * output or processed by one of the modes (command-line, interactive or
 * library).
 *
 * @author dbradley
 */
class Im8PseudoStream {

    private static String END_OF_LINE = String.format("\n");
    private static int END_OF_LINE_LENGTH = END_OF_LINE.length();

    protected final ArrayList<String> arrBuffer4String;

    /** Create the IO stream for basic processing of messages. */
    protected Im8PseudoStream() {
        this.arrBuffer4String = new ArrayList<>();
    }

    /** Clear the messages buffer array. */
    final public void clear() {
        this.arrBuffer4String.clear();
    }

    /**
     * Print out the messages buffer array to the PrintStream provided
     * (typically System.out or System.err) as per invokers requirements.
     *
     * @param pStream PrintStream object to print too
     */
    final public void printOut(PrintStream pStream) {

        for (String str : this.arrBuffer4String) {
            // assumption is the lines have been formatted with newline at the end
            pStream.printf("%s", str);
        }
    }

    /**
     * Get array buffer for independent processing by an invoker, any cr-lf/lf
     * at the end-of-a-line are removed.
     * <p>
     * The end-of-line kind cr-lf/lf  depends on the running platform. 
     *
     * @return array list of string
     */
    final public ArrayList<String> getBufferArray() {
        // need to return a buffer copy, so the user array-list is
        // unaffected
        ArrayList<String> copyOfBuffer = new ArrayList<>();

        for (String s : this.arrBuffer4String) {
            if (s.endsWith(END_OF_LINE)) {
                s = s.substring(0, s.length() - END_OF_LINE_LENGTH);
            }
            copyOfBuffer.add(s);
        }
        return copyOfBuffer;
    }
}

class Im8PseudoStreamOut extends Im8PseudoStream {

    Im8PseudoStreamOut() {
        super();
    }

    /**
     * Pseudo print format message into messages buffer array.
     *
     * @param format String that is a format string (as defined by
     *               String.format)
     * @param args   arguments to be used in format
     */
    final void sprintf(String format, Object... args) {
        String formatted = String.format(format, args);

        this.arrBuffer4String.add(formatted);
    }

    /**
     * Pseudo print line message into messages buffer array.
     *
     * @param str String add to buffer with new-line end)
     */
    final void sprintln(String str) {
        String formatted = String.format("%s\n", str);
        this.arrBuffer4String.add(formatted);
    }
}

class Im8PseudoStreamErr extends Im8PseudoStream {

    Im8PseudoStreamErr() {
        super();
    }

    /**
     * Pseudo print format message into messages buffer array.
     *
     * @param format String that is a format string (as defined by
     *               String.format)
     * @param args   arguments to be used in format
     */
    final void sprintf(ErrorKind errorKind, String format, Object... args) {
        String formatted = String.format(format, args);

        // prefix the error-kind
        if (errorKind != null) {
            formatted = String.format("%s: %s", errorKind.getErrMsgPrefix(), formatted);
        }
        this.arrBuffer4String.add(formatted);
    }

    /**
     * Pseudo print line message into messages buffer array.
     *
     * @param str String add to buffer with new-line end)
     */
    final void sprintln(ErrorKind errorKind, String str) {
        String formatted = String.format("%s\n", str);

        // prefix the error-kind
        if (errorKind != null) {
            formatted = String.format("%s: %s", errorKind.getErrMsgPrefix(), formatted);
        }
        this.arrBuffer4String.add(formatted);
    }
}
