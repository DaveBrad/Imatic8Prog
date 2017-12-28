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

import java.util.ArrayList;

/**
 * Class to use as a library executor of the Imatic8Prog.
 * <p>
 * The user needs to do something like the following:
 * <pre>
 *      Imatic8LibMode libObject = new Imatic8LibMode();
 *      libObject.execute("off 4");
 *      assertTrue(libObject.getExitCode() &gt;= 0);
 *
 *      // the status should have changed with  4 off showing (- instead of 4)
 *      libObject.execute("status");
 *      assertEquals("Status:b-1:12--5678", libObject.getResponses().get(0));
 * </pre> The above example is test-case script that uses
 * <code>getExitCode()</code> and <code>getResponses()</code> to check a valid
 * exit-code and the message response from any requests.
 *
 * @author dbradley
 */
public class Imatic8LibMode {

    /**
     * Response on a platform will have cr-lf or lf at the end of the line
     */
    private static final String crLf = String.format("\n");

    /**
     *
     */
    private String userDirOverride = null;

    /**
     * The exitCode that will be returned from the processing.
     */
    private int exitCode;

    /**
     * An array list of any responses, error or success forms.
     */
    private ArrayList<String> responseArr;

    public Imatic8LibMode() {
        // 
    }

    /**
     *
     */
    public void setUserDir(String userDirOverride) {
        this.userDirOverride = userDirOverride;
    }

    /**
     * Execute the argument (internally parsed) as requests to the Imatic8Prog
     * (as if in command-mode).
     *
     * @param argsInOneString string of a line of arguments which will be parsed
     */
    public void execute(String argsInOneString) {
        execute(Im8Common.tokenSingleLine(argsInOneString));
    }

    /**
     * Execute an arguments list as requests to the Imatic8Prog (as if in
     * command mode).
     *
     * @param argsArr string list of arguments that make requests
     */
    public void execute(String... argsArr) {
        exitCode = 0;
        responseArr = null;

        //  process the arguments
        Im8Io m8Io = new Im8Io(argsArr);
        m8Io.setUserDir(this.userDirOverride);
        
        m8Io.runAsLib();

        // store the response away so the user may use them
        exitCode = m8Io.getExitCode();

        ArrayList<String> responseFromM8IoArr = m8Io.getReportIntoArray();

        if (responseFromM8IoArr == null) {
            this.responseArr = null;
        } else if (responseFromM8IoArr.isEmpty()) {
            this.responseArr = null;
        } else {
            // remove all the end-of-line line-feeds/carriage-returns from the strings
            this.responseArr = new ArrayList<>();

            for (String s : responseFromM8IoArr) {
                if (s.endsWith(crLf)) {
                    // remove the crLf (windows cr&lf, *nix lf)
                    s = s.substring(0, s.lastIndexOf(crLf));

                    this.responseArr.add(s);
                }
            }
        }
    }

    /**
     * Get the exit-code from the processing of the <code>execute</code>
     * request&#46; Zero 0 or positive is a successful execution, while less
     * than 0 (&lt;0) an error was detected: the response of which will be found
     * in the <code>getResponses();</code> array list of strings.
     *
     * @return integer of the exit code
     */
    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * Get an array list of strings that are any responses&#46; No responses is
     * possible when a successful exit-code is provide (&gt;= 0).
     *
     * @return array list of string(s), or null if no response stored
     */
    public ArrayList<String> getResponses() {
        return this.responseArr;
    }
}
