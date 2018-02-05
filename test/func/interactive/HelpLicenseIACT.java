/* Copyright (c) 2017 dbradley. */
package func.interactive;

import boardemulator.Im8TestServerMgr4Emulators;
import static boardemulator.Im8TestServerMgr4Emulators.IpAddressKind.LOCAL_LOOP;
import static org.jtestdb.aid.console.CcCapture.ClearBufferKind.CLEAR_ALL;
import org.jtestdb.aid.console.CcTerminalInteractive;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * <p>
 * Due to fact that responses need to be tested via the console, and only one
 * console per test run is allowed test are written as sequential using TestNG.
 * <p>
 * Tests involve the three modes of command-line, interactive and library
 * execution.
 *
 * @author dbradley
 */
@Test
public class HelpLicenseIACT extends IactTestClass{

    private String terminalImatic8Prog;

    @BeforeClass
    public void allocCapture() {
        // construct the terminal java run time environment
        //
        // -Dim8emulator provides the ipAddr"port for the terminal to communicate
        // with the board-emulator manage (as the terminal is an independent 
        // process on the computer doing the testing)
        //
        terminalImatic8Prog = String.format(
                "java -Dim8emulator=\"%s\" -cp ./build/classes;./build/test/classes imatic8.Imatic8Prog",
                Im8TestServerMgr4Emulators.getInstance().getServerMgrIp(LOCAL_LOOP)); // NETWORK LOCAL_LOOP));
    }

    @AfterClass
    public void cleanupAfterClass() {
//        if (this.ccOut != null) {
//            this.ccOut.cleanupOnExit();
//            this.ccOut = null;
//        }
    }

    @Test
    public void HelpInteractiveHelp() {
        CcTerminalInteractive termI1 = new CcTerminalInteractive("termI1", System.out, System.err);

        termI1.runTerm(this.testUserDir);
        termI1.enterText(1000, terminalImatic8Prog);

        termI1.outCc.startCapture("*TestNG*", "termI1-in:");
        termI1.enterText(1000, "help");
        termI1.enterText(1000, "exit");

        termI1.outCc.stopCapture();
        termI1.outCc.printAllLines();

        termI1.outCc.asOrdered(">Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);

        termI1.outCc.clearBuffer(CLEAR_ALL);
    }

    @Test
    public void HelpInteractiveQues() {
        CcTerminalInteractive termI2 = new CcTerminalInteractive("termI2", System.out, System.err);

        termI2.runTerm(this.testUserDir);
        termI2.enterText(1000, terminalImatic8Prog);

        termI2.outCc.startCapture("*TestNG*", "termI2-in:");
        termI2.enterText(1000, "?");
        termI2.enterText(1000, "exit");

        termI2.outCc.stopCapture();
        termI2.outCc.printAllLines();

        termI2.outCc.asOrdered(">Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);

        termI2.outCc.clearBuffer(CLEAR_ALL);
    }

    @Test
    public void HelpInteractiveSlashQues() {
        CcTerminalInteractive termI3 = new CcTerminalInteractive("termI3", System.out, System.err);

        termI3.runTerm(this.testUserDir);
        termI3.enterText(1000, terminalImatic8Prog);

        termI3.outCc.startCapture("*TestNG*", "termI3-in:");
        termI3.enterText(1000, "/?");
        termI3.enterText(1000, "exit");

        termI3.outCc.stopCapture();
        termI3.outCc.printAllLines();

        termI3.outCc.asOrdered(">Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);

        termI3.outCc.clearBuffer(CLEAR_ALL);
    }

    @Test
    public void HelpInteractiveLicense() {
        CcTerminalInteractive termI4 = new CcTerminalInteractive("termI4", System.out, System.err);

        termI4.runTerm(this.testUserDir);
        termI4.enterText(1000, terminalImatic8Prog);

        termI4.outCc.startCapture("*TestNG*", "termI4-in:");
        termI4.enterText(1000, "LICENSE");
        termI4.enterText(1000, "exit");

        termI4.outCc.stopCapture();
        termI4.outCc.printAllLines();

        termI4.outCc.asOrdered("License: Imatic8Prog")
                .checkLineNContains(2);

        termI4.outCc.clearBuffer(CLEAR_ALL);
    }

    @Test
    public void HelpInteractiveL() {
        CcTerminalInteractive termI5 = new CcTerminalInteractive("termI5", System.out, System.err);

        termI5.runTerm(this.testUserDir);
        termI5.enterText(1000, terminalImatic8Prog);

        termI5.outCc.startCapture("*TestNG*", "termI5-in:");
        termI5.enterText(1000, "l");
        termI5.enterText(1000, "exit");

        termI5.outCc.stopCapture();
        termI5.outCc.printAllLines();

        termI5.outCc.asOrdered("License: Imatic8Prog")
                .checkLineNContains(2);

        termI5.outCc.clearBuffer(CLEAR_ALL);
    }

}
