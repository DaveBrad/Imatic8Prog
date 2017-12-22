/* Copyright (c) 2017 dbradley. */
package func.cmdline;

import boardemulator.Im8TestShadowBoardSvr;
import imatic8.Imatic8Prog;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import exitcapture.ExitCaptureException;
import func.Imatic8aseTest;
import org.jtestdb.aid.console.CcCapture;
import static org.jtestdb.aid.console.CcCapture.ClearBufferKind.RETAIN_IGNORE_IF_CONTAINS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author dbradley
 */
public class RelayOnOffCLI extends Imatic8aseTest {

    protected CcCapture ccOut;
    protected CcCapture ccErr;

    private Im8TestShadowBoardSvr board1Svr;
    private Im8TestShadowBoardSvr board2Svr;

    @BeforeClass
    public void allocBoards() {
        // create board emulators for two IP addresses
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
        this.ccErr.startCapture();
        try {
            Imatic8Prog.main(new String[]{"b-2", "on", "2", "status"});
            this.ccErr.stopCapture();
            assertTrue(false, "No exit detected.");

        } catch (ExitCaptureException x) {
            this.ccErr.stopCapture();
            assertEquals(x.getExitCode(), -1, "Exit code issue:");
        }
        //
        this.ccErr.asOrdered("ERROR-arg: no INI file for board 2, need to use 'defip-2 n.n.n.n' to define.")
                .checkLineNContains(0);
        this.ccErr.clearBuffer();
    }

    @Test(dependsOnMethods = "noB2IniFile")
    public void b1OnAll() {
        // there is no output
        try {
            Imatic8Prog.main(new String[]{"on", "all"});
            assertTrue(false, "No exit detected.");
        } catch (ExitCaptureException x) {
            assertEquals(x.getExitCode(), 0, "Exit code issue.");
        }
    }

    @Test(dependsOnMethods = "b1OnAll")
    public void b1Status() {

        this.ccOut.startCapture();
        try {
            Imatic8Prog.main(new String[]{"status"});
            this.ccOut.stopCapture();
            assertTrue(false, "No exit detected.");

        } catch (ExitCaptureException x) {
            this.ccOut.stopCapture();
            assertEquals(x.getExitCode(), 0, "Exit code issue.");
        }
        this.ccOut.asOrdered("Status:b-1:12345678")
                .checkLineNContains(0);

        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "b1Status")
    public void b1Off3Status() {
        // there is no output
        try {
            Imatic8Prog.main(new String[]{"off", "3"});
            assertTrue(false, "No exit detected.");
        } catch (ExitCaptureException x) {
            assertEquals(x.getExitCode(), 0, "Exit code issue.");
        }
        //
        this.ccOut.startCapture();
        try {
            Imatic8Prog.main(new String[]{"status"});
            this.ccOut.stopCapture();
            assertTrue(false, "No exit detected.");

        } catch (ExitCaptureException x) {
            this.ccOut.stopCapture();
            assertEquals(x.getExitCode(), 0, "Exit code issue.");
        }
        this.ccOut.asOrdered("Status:b-1:12-45678")
                .checkLineNContains(0);
        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "b1Off3Status")
    public void b1Off4AndStatusNoServer() {
        // with the server down the off action below will fail
        this.board1Svr.testEndServer(1);

        this.ccErr.startCapture();
        try {
            Imatic8Prog.main(new String[]{"off", "4"});
            this.ccErr.stopCapture();
            assertTrue(false, "No exit detected.");
        } catch (ExitCaptureException x) {
            this.ccErr.stopCapture();
            assertEquals(x.getExitCode(), -99, "Exit code issue.");
        }
        this.ccErr.asOrdered("ERROR-rt IO: open comm: b-1").checkLineNContains(0);
        this.ccErr.clearBuffer();

        // the status should not have changed as the off 4 failed
        this.ccOut.startCapture();
        try {
            Imatic8Prog.main(new String[]{"status"});
            this.ccOut.stopCapture();
            assertTrue(false, "No exit detected.");

        } catch (ExitCaptureException x) {
            this.ccOut.stopCapture();
            assertEquals(x.getExitCode(), 0, "Exit code issue.");
        }
        this.ccOut.asOrdered("Status:b-1:12-45678")
                .checkLineNContains(0);
        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "b1Off4AndStatusNoServer")
    public void b1Off4AndStatus() {
        // with the server down the off action below will fail
        this.board1Svr.testRestartServer(2);

        try {
            Imatic8Prog.main(new String[]{"off", "4"});
            assertTrue(false, "No exit detected.");

        } catch (ExitCaptureException x) {
            assertEquals(x.getExitCode(), 0, "Exit code issue.");
        }

        // the status should not have changed as the off 4 failed
        this.ccOut.startCapture();
        try {
            Imatic8Prog.main(new String[]{"status"});
            this.ccOut.stopCapture();
            assertTrue(false, "No exit detected.");

        } catch (ExitCaptureException x) {
            this.ccOut.stopCapture();
            assertEquals(x.getExitCode(), 0, "Exit code issue.");
        }
        this.ccOut.asOrdered("Status:b-1:12--5678")
                .checkLineNContains(0);
        this.ccOut.clearBuffer();
    }
}
