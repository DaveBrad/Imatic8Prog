/* Copyright (c) 2017 dbradley. */
package func.library;

import boardemulator.Im8TestShadowBoardSvr;
import imatic8.Im8BoardIniTest;
import imatic8.Imatic8LibMode;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author dbradley
 */
public class RelayOnOffLib extends LibTestClass {

    private Im8TestShadowBoardSvr board1Svr;
    private Im8TestShadowBoardSvr board2Svr;

    private File b1IniFile;
    private File b2IniFile;

    private Imatic8LibMode libObject;

    @BeforeClass
    public void allocBoards() {
        // create board emulators for two IP addresses
        this.board1Svr = Im8TestShadowBoardSvr.createEmulatorForIP("192.168.1.4", 30000);
        this.b1IniFile = Im8BoardIniTest.getBoardIniFile4Test(1);

        this.board2Svr = Im8TestShadowBoardSvr.createEmulatorForIP("192.168.1.5", 30000);
        this.b2IniFile = Im8BoardIniTest.getBoardIniFile4Test(2);
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

        libObject = new Imatic8LibMode();

        libObject.execute("b-2", "on", "2", "status");

        assertEquals(-1, libObject.getExitCode());
        assertEquals("ERROR-arg: no INI file for board 2, need to use 'defip-2 n.n.n.n' to define.",
                libObject.getResponses().get(0));
    }

    @Test(dependsOnMethods = "noB2IniFile")
    public void b1OnAll() {
        assertFalse(this.b1IniFile.isFile(), "present issue, b-1 INI");

        // there is no output
        libObject.execute("on", "all");
        assertEquals(0, libObject.getExitCode());

        libObject.execute("status");
        assertEquals("Status:b-1:12345678", libObject.getResponses().get(0));

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
        // perform the off 3 action (the exit code is checked in cliEnter)
        libObject.execute("off", "3");

        assertEquals(0, libObject.getExitCode());
        assertNull(libObject.getResponses(), "Should be no response");

        // the status of 3 should be off
        libObject.execute("status");
        assertEquals("Status:b-1:12-45678", libObject.getResponses().get(0));
    }

    @Test(dependsOnMethods = "b1Off3AndStatus")
    public void b1Off4AndStatusNoServerSingleLineOnLibraryArgsInput() {
        // with the server down the off action below will fail
        this.board1Svr.testEndServer(1);

        // provide the line of the args as a single string with spaces
        // (this requires the lib-interface to tokenize into a string array
        // of separate arguments)
        libObject.execute("off 4");
        // intrested in the beginning of the open communination connect fails
        assertTrue(libObject.getResponses().get(0).contains("ERROR-rt IO: open comm: b-1"));

        libObject.execute("status");
        assertEquals("Status:b-1:12-45678", libObject.getResponses().get(0));
    }

    @Test(dependsOnMethods = "b1Off4AndStatusNoServerSingleLineOnLibraryArgsInput")
    public void b1Off4AndStatusServerBackOn() {
        // check when the server is back, the interactive off works
        this.board1Svr.testRestartServer(2000);

        libObject.execute("off 4");
        assertTrue(libObject.getExitCode() >= 0);

        // the status should have changed with  4 off showing (- instead of 4)
        libObject.execute("status");
        assertEquals("Status:b-1:12--5678", libObject.getResponses().get(0));
    }
    @Test(dependsOnMethods = "b1Off4AndStatusServerBackOn")
    public void b1Off4AndStatusServerBackOnss() {
        // check when the server is back, the interactive off works
       
        libObject.execute("help");

        // the status should have changed with  4 off showing (- instead of 4)
        libObject.execute("status");
        assertEquals("Status:b-1:12--5678", libObject.getResponses().get(0));
    }
}
