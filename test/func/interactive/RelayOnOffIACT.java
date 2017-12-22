/* Copyright (c) 2017 dbradley. */
package func.interactive;

import boardemulator.Im8TestShadowBoardSvr;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import func.Imatic8aseTest;
import org.jtestdb.aid.console.CcTerminalInteractive;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author dbradley
 */
public class RelayOnOffIACT extends Imatic8aseTest {

    private Im8TestShadowBoardSvr board1Svr;
    private Im8TestShadowBoardSvr board2Svr;

    private static String terminalImatic8Prog
            = "java -Dtst=\"tst\" -cp ../build/classes;../build/test/classes imatic8.Imatic8Prog";

    private CcTerminalInteractive termI;

    @BeforeClass
    public void allocBoards() {
        // create board emulators for two IP addresses
        this.board1Svr = Im8TestShadowBoardSvr.createEmulatorForIP("192.168.1.4", 30000);
        this.board2Svr = Im8TestShadowBoardSvr.createEmulatorForIP("192.168.1.5", 30000);
    }

    @AfterClass
    public void cleanupAfterClass() {
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
        termI = new CcTerminalInteractive("termI", System.out, System.err);

        termI.runTerm(this.testUserDir);

        termI.errCc.startCapture("*TestNG*");
//        termI.outCc.startCapture("*TestNG*");
        termI.enterText(1500, terminalImatic8Prog);

//        termI.enterText(500, "b-2 on 2 status");
//        termI.errCc.stopCapture();
//
//        termI.errCc.asOrdered("ERROR-arg: no INI file for board 2, need to use 'defip-2 n.n.n.n' to define.")
//                .checkLineNContains(0);
//        termI.errCc.clearBuffer();
    }

    @Test(dependsOnMethods = "noB2IniFile")
    public void b1OnAll() {
//        // there is no output
//        termI.errCc.startCapture("*TestNG*");
        termI.enterText(1500, "");
        termI.enterText(1500, "on 1");

        termI.errCc.stopCapture();
        termI.errCc.printAllLines();
        termI.errCc.clearBuffer();
//
////        termI.enterText(500, "status");
////
////        termI.outCc.stopCapture();
////        termI.outCc.printAllLines();
////        
////        int a = 1;
////        termI.outCc.clearBuffer();
    }

    @Test(dependsOnMethods = "b1OnAll")
    public void b1Status() {
        termI.outCc.startCapture();

        termI.enterText(1500, "status");
//        termI.enterText(500, "exit");
        termI.outCc.stopCapture();
        termI.outCc.printAllLines();

//        termI.outCc.asOrdered("Status:b-1:12345678").checkLineNContains(0);
        termI.outCc.asOrdered("Status:b-1:--------").checkLineNContains(0);
        termI.outCc.clearBuffer();

        //
        termI.outCc.startCapture();

        termI.enterText(1500, "status");
        termI.outCc.stopCapture();
        termI.outCc.printAllLines();

//        termI.outCc.asOrdered("Status:b-1:12345678").checkLineNContains(0);
        termI.outCc.clearBuffer();
    }

//    @Test(dependsOnMethods = "b1Status")
//    public void b1Off3Status() {
//        // there is no output
//        try {
//            Imatic8Prog.main(new String[]{"off", "3"});
//            assertTrue(false, "No exit detected.");
//        } catch (ExitCaptureException x) {
//            assertEquals(x.getExitCode(), 0, "Exit code issue.");
//        }
//        //
//        this.ccOut.startCapture("*TestNG*");
//        try {
//            Imatic8Prog.main(new String[]{"status"});
//            this.ccOut.stopCapture();
//            assertTrue(false, "No exit detected.");
//
//        } catch (ExitCaptureException x) {
//            this.ccOut.stopCapture();
//            assertEquals(x.getExitCode(), 0, "Exit code issue.");
//        }
//        this.ccOut.asOrdered("Status:b-1:12-45678")
//                .checkLineNContains(0);
//        this.ccOut.clearBuffer(CLEAR_ALL);
//    }
//
//    @Test(dependsOnMethods = "b1Off3Status")
//    public void b1Off4AndStatusNoServer() {
//        // with the server down the off action below will fail
//        this.board1Svr.testEndServer(1);
//
//        this.ccErr.startCapture("*TestNG*");
//        try {
//            Imatic8Prog.main(new String[]{"off", "4"});
//            this.ccErr.stopCapture();
//            assertTrue(false, "No exit detected.");
//        } catch (ExitCaptureException x) {
//            this.ccErr.stopCapture();
//            assertEquals(x.getExitCode(), -99, "Exit code issue.");
//        }
//        this.ccErr.asOrdered("ERROR-rt IO: b-1: Connection refused: connect.").checkLineNContains(1);
//        this.ccErr.clearBuffer(CLEAR_ALL);
//
//        // the status should not have changed as the off 4 failed
//        this.ccOut.startCapture("*TestNG*");
//        try {
//            Imatic8Prog.main(new String[]{"status"});
//            this.ccOut.stopCapture();
//            assertTrue(false, "No exit detected.");
//
//        } catch (ExitCaptureException x) {
//            this.ccOut.stopCapture();
//            assertEquals(x.getExitCode(), 0, "Exit code issue.");
//        }
//        this.ccOut.asOrdered("Status:b-1:12-45678")
//                .checkLineNContains(0);
//        this.ccOut.clearBuffer(RETAIN_IGNORE_IF_CONTAINS);
//    }
//
//    @Test(dependsOnMethods = "b1Off4AndStatusNoServer")
//    public void b1Off4AndStatus() {
//        // with the server down the off action below will fail
//        this.board1Svr.testRestartServer(2);
//
//        try {
//            Imatic8Prog.main(new String[]{"off", "4"});
//            assertTrue(false, "No exit detected.");
//
//        } catch (ExitCaptureException x) {
//            assertEquals(x.getExitCode(), 0, "Exit code issue.");
//        }
//
//        // the status should not have changed as the off 4 failed
//        this.ccOut.startCapture("*TestNG*");
//        try {
//            Imatic8Prog.main(new String[]{"status"});
//            this.ccOut.stopCapture();
//            assertTrue(false, "No exit detected.");
//
//        } catch (ExitCaptureException x) {
//            this.ccOut.stopCapture();
//            assertEquals(x.getExitCode(), 0, "Exit code issue.");
//        }
//        this.ccOut.asOrdered("Status:b-1:12--5678")
//                .checkLineNContains(0);
//        this.ccOut.clearBuffer(RETAIN_IGNORE_IF_CONTAINS);
//    }
}
