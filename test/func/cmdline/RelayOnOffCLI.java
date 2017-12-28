/* Copyright (c) 2017 dbradley. */
package func.cmdline;

import boardemulator.Im8TestShadowBoardSvr;
import static imatic8.Im8BoardIniTest.getBoardIniFile4Test;

import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.jtestdb.aid.console.CcCapture;
import static org.jtestdb.aid.console.CcCapture.ClearBufferKind.RETAIN_IGNORE_IF_CONTAINS;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author dbradley
 */
public class RelayOnOffCLI extends CliTestClass {

    protected CcCapture ccOut;
    protected CcCapture ccErr;

    private Im8TestShadowBoardSvr board1Svr;
    private Im8TestShadowBoardSvr board2Svr;

    @BeforeClass
    public void allocBoards() {
        // create board emulators for two IP addresses and the expexted INI file
        // that will be created
        this.board1Svr = Im8TestShadowBoardSvr.createEmulatorForIP("192.168.1.4", 30000);
        this.board2Svr = Im8TestShadowBoardSvr.createEmulatorForIP("192.168.1.5", 30000);

        // will capture both out and err streams during the tests
        this.ccOut = new CcCapture(System.out);
        this.ccOut.startCapture("*TestNG*");
        this.ccOut.stopCapture();
        this.ccOut.clearBuffer(RETAIN_IGNORE_IF_CONTAINS);

        this.ccErr = new CcCapture(System.err);
        this.ccErr.startCapture("*TestNG*");
        this.ccErr.stopCapture();
        this.ccErr.clearBuffer(RETAIN_IGNORE_IF_CONTAINS);
    }

    @AfterClass
    public void cleanupAfterClass() {
        if (this.ccOut != null) {
            this.ccOut.cleanupOnExit();
            this.ccOut = null;
        }
        if (this.ccErr != null) {
            this.ccErr.cleanupOnExit();
            this.ccErr = null;
        }
        this.board1Svr.testEndServer(0);
        this.board2Svr.testEndServer(0);
    }

    @BeforeMethod
    public void setUp() {
    }

    @AfterMethod
    public void tearDown() {
    }

    @Test
    public void noB2IniFile() {
        cliEnter(ccErr, -1, "b-2", "on", "2", "status");
        //
        // THE ABOVE LINE performs the commented actions below which enters the
        //                         text and checks exit code
        //
        //        this.ccErr.startCapture();
        //        try {
        //            Imatic8Prog.main(new String[]{"status"});
        //            this.ccErr.stopCapture();
        //            assertTrue(false, "No exit detected.");
        //
        //        } catch (ExitCaptureException x) {
        //            this.ccErr.stopCapture();
        //            assertEquals(x.getExitCode(), -1, "Exit code issue.");
        //        }
        //
        this.ccErr.asOrdered("ERROR-arg: no INI file for board 2, need to use 'defip-2 n.n.n.n' to define.")
                .checkLineNContains(0);
        this.ccErr.clearBuffer();
    }

    @Test(dependsOnMethods = "noB2IniFile")
    public void b1OnAll() {
        // ini file for b-1 is not present
        assertFalse(getBoardIniFile4Test(1).isFile(), "present issue, for b-1 INI");

        // there is no output
        cliEnter(0, "on", "all");

        // ini file for b-1 created automatically
        assertTrue(getBoardIniFile4Test(1).isFile(), "NOT present issue, for b-1 INI");
    }

    @Test(dependsOnMethods = "b1OnAll")
    public void b1IniFileCorrectDefaultIP() {
        // process the board-1 INI file and look for the default IP address
        // 192.168.1.4 is set used within the file
        try {
            List<String> b1IniFileContentList = FileUtils.readLines(getBoardIniFile4Test(1), "utf-8");

            boolean foundDefaultIP = false;
            for (String s : b1IniFileContentList) {
                // looking for the default IP address
                foundDefaultIP |= s.contains("192.168.1.4");
            }
            assertTrue(foundDefaultIP, "Default IP for b-1 not created");

        } catch (IOException ex) {
            assertFalse(true,
                    String.format("IO error: unable to process INI file\n"
                            + "External impact condition/control to testing evironment?\n%s", 
                            ex.getCause()));
        }
    }

    @Test(dependsOnMethods = "b1IniFileCorrectDefaultIP")
    public void b1Status() {
        // all the relays should be on
        cliEnter(ccOut, 0, "status");
        this.ccOut.asOrdered("Status:b-1:12345678").checkLineNContains(0);

        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "b1Status")
    public void b1Off3AndStatus() {
        // turn off 3, there is no output expected

        // suppress console-capture progress messages so as not to
        // collect these into the buffers
        this.ccOut.suppressCcMessages();
        this.ccErr.suppressCcMessages();

        // in the test environment, any System.exit(...) are intercepted 
        // so the testing framework does not exit too
        this.ccOut.startCapture("TestEnv: EXIT intercepted");
        this.ccErr.startCapture();

        // perform the off 3 action (the exit code is checked in cliEnter)
        cliEnter(0, "off", "3");

        // check that the buffers contain nothing (as expected)
        this.ccOut.stopCapture();
        this.ccErr.stopCapture();

        assertTrue(this.ccOut.checkNoLines());
        assertTrue(this.ccErr.checkNoLines());

        this.ccOut.clearBuffer();
        this.ccErr.clearBuffer();

        // revert the filter for empty buffer by scrubing the test enviroment
        // exit interceptor message for any future use
        this.ccOut.scrubCaptureIgnoreFilter("TestEnv: EXIT intercepted");

        // the status of 3 should be off, will produce output (along with the
        // test env: Exit intercept which is not part of the application)
        cliEnter(ccOut, 0, "status");

        this.ccOut.asOrdered("Status:b-1:12-45678").checkLineNContains(0);
        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "b1Off3AndStatus")
    public void b1Off4AndStatusNoServer() {
        // with the server down the off action below will fail
        this.board1Svr.testEndServer(1);

        cliEnter(ccErr, -99, "off", "4");

        this.ccErr.asOrdered("ERROR-rt IO: open comm: b-1").checkLineNContains(0);
        this.ccErr.clearBuffer();

        // the status should NOT have changed as the off 4 failed
        cliEnter(ccOut, 0, "status");
        this.ccOut.asOrdered("Status:b-1:12-45678").checkLineNContains(0);
        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "b1Off4AndStatusNoServer")
    public void b1Off4AndStatusServerBackOn() {
        // check when the server is back, the off works
        this.board1Svr.testRestartServer(2000);

        cliEnter(0, "off", "4");

        // the status should have changed with  4 off showing (- instead of 4)
        cliEnter(ccOut, 0, "status");
        this.ccOut.asOrdered("Status:b-1:12--5678").checkLineNContains(0);
        this.ccOut.clearBuffer();
    }
}
