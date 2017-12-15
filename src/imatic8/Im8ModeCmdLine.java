/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imatic8;

/**
 *
 * @author dbradley
 */
public class Im8ModeCmdLine {

    static void processCmdLineMode(String[] args) {
        // command line mode

        Im8Io m8Io = new Im8Io(args);
        m8Io.runAsLib();

        m8Io.printReport();
        System.exit(m8Io.getExitCode());
    }
}
