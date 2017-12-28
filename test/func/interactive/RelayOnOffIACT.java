/* Copyright (c) 2017 dbradley. */
package func.interactive;

import boardemulator.Im8TestServerMgr4Emulators;
import static boardemulator.Im8TestServerMgr4Emulators.IpAddressKind.LOCAL_LOOP;
import boardemulator.Im8TestShadowBoardSvr;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import func.Imatic8aseTest;
import imatic8.Im8BoardIniTest;
import static imatic8.Im8BoardIniTest.getBoardIniFile4Test;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.jtestdb.aid.console.CcTerminalInteractive;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author dbradley
 */
public class RelayOnOffIACT extends Imatic8aseTest {

    private Im8TestShadowBoardSvr board1Svr;
    private Im8TestShadowBoardSvr board2Svr;

    private File b1IniFile;
    private File b2IniFile;

    private static String terminalImatic8Prog = null;

    private CcTerminalInteractive termI;

    @BeforeClass
    public void allocBoards() {
        // prepare terminal
        termI = new CcTerminalInteractive("termI", System.out, System.err);
        termI.runTerm(this.testUserDir);

        termI.outCc.startCapture("*TestNG*");
        termI.outCc.stopCapture();
        termI.outCc.clearBuffer();

        termI.errCc.startCapture("*TestNG*");
        termI.errCc.stopCapture();
        termI.errCc.clearBuffer();

        // create board emulators for two IP addresses
        this.board1Svr = Im8TestShadowBoardSvr.createEmulatorForIP("192.168.1.4", 30000);
        this.b1IniFile = Im8BoardIniTest.getBoardIniFile4Test(1);

        this.board2Svr = Im8TestShadowBoardSvr.createEmulatorForIP("192.168.1.5", 30000);
        this.b2IniFile = Im8BoardIniTest.getBoardIniFile4Test(2);

        // the test cases are only run on a local host thus local_loop is required for
        // the commication of inter-process communication
        terminalImatic8Prog = String.format(
                "java -Dim8emulator=\"%s\" -cp ../build/classes;../build/test/classes imatic8.Imatic8Prog",
                Im8TestServerMgr4Emulators.getInstance().getServerMgrIp(LOCAL_LOOP)); // NETWORK LOCAL_LOOP));
    }

    @AfterClass
    public void cleanupAfterClass() {
        // close of the terminal window
        // (not perfect if the terminal hangs for some reason)
        this.termI.enterText(500, "exit");

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

        termI.errCc.startCapture();

        termI.enterText(1500, terminalImatic8Prog);
        termI.enterText(500, "b-2 on 2 status");
        termI.errCc.stopCapture();

        termI.errCc.asOrdered("ERROR-arg: no INI file for board 2, need to use 'defip-2 n.n.n.n' to define.")
                .checkLineNContains(0);
        termI.errCc.clearBuffer();
    }

    @Test(dependsOnMethods = "noB2IniFile")
    public void b1OnAll() {
        assertFalse(this.b1IniFile.isFile(), "present issue, b-1 INI");

        // there is no output
        termI.enterText(500, "on all");

        termI.outCc.startCapture();
        termI.enterText(500, "status");
        termI.outCc.stopCapture();

        termI.outCc.asOrdered("Status:b-1:12345678").checkLineNContains(0);
        termI.outCc.clearBuffer();

        // ini file for b-1 created automatically
        assertTrue(this.b1IniFile.isFile(), "NOT present issue, b-1 INI");
    }

    @Test(dependsOnMethods = "b1OnAll")
    public void b1IniFileCorrect() {
        // 
        try {
            List<String> b1IniFileContentList = FileUtils.readLines(this.b1IniFile, "utf-8");

            boolean foundDefaultIP = false;
            for (String s : b1IniFileContentList) {
                // looking for the default IP address
                foundDefaultIP |= s.contains("192.168.1.4");
            }
            assertTrue(foundDefaultIP, "Default IP for b-1 not created");

        } catch (IOException ex) {
            assertFalse(true, "IO error: unable to process INI file");
        }
    }

    @Test(dependsOnMethods = "b1IniFileCorrect")
    public void b1Off3AndStatus() {
        // there is no output for the off 3 request
       
        // start the capture of both streams (as this is an interactive
        // terminal there are no console-capture progress messages)
        termI.outCc.startCapture();
        termI.errCc.startCapture();
        
        // perform the off 3 action (the exit code is checked in cliEnter)
        termI.enterText(500, "off 3");
        
        // check that the buffers contain nothing (as expected)
        termI.outCc.stopCapture();
        termI.errCc.stopCapture();
        
        assertTrue(termI.outCc.checkNoLines());
        assertTrue(termI.errCc.checkNoLines());

        termI.outCc.clearBuffer();
        termI.errCc.clearBuffer(); 

        // the status of 3 should be off
        termI.outCc.startCapture();
        termI.enterText(500, "status");
        termI.outCc.stopCapture();

        termI.outCc.asOrdered("Status:b-1:12-45678").checkLineNContains(0);
        termI.outCc.clearBuffer();
    }

    @Test(dependsOnMethods = "b1Off3AndStatus")
    public void b1Off4AndStatusNoServer() {
        // with the server down the off action below will fail
        this.board1Svr.testEndServer(1);

        termI.errCc.startCapture();
        termI.enterText(2500, "off 4");
        termI.errCc.stopCapture();

        termI.errCc.printAllLines();

        termI.errCc.asOrdered("ERROR-rt IO: open comm: b-1").checkLineNContains(0);
        termI.errCc.clearBuffer();

        // the status should not have changed as the off 4 failed
        termI.outCc.startCapture();
        termI.enterText(500, "status");
        termI.outCc.stopCapture();

        termI.outCc.asOrdered("Status:b-1:12-45678").checkLineNContains(0);
        termI.outCc.clearBuffer();
    }

    @Test(dependsOnMethods = "b1Off4AndStatusNoServer")
    public void b1Off4AndStatusServerBackOn() {
        // check when the server is back, the interactive off works
        this.board1Svr.testRestartServer(2000);

        termI.enterText(500, "off 4");

        // the status should have changed with  4 off showing (- instead of 4)
        termI.outCc.startCapture();
        termI.enterText(500, "status");
        termI.outCc.stopCapture();

        termI.outCc.asOrdered("Status:b-1:12--5678").checkLineNContains(0);
        termI.outCc.clearBuffer();
    }
}
