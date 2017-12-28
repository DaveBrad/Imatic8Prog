/* Copyright (c) 2017 dbradley. */
package func.defip;

import boardemulator.Im8TestShadowBoardSvr;
import exitcapture.ExitInterceptException;
import func.cmdline.CliTestClass;
import imatic8.Im8BoardIniTest;
import java.io.File;
import org.jtestdb.aid.console.CcCapture;
import static org.jtestdb.aid.console.CcCapture.ClearBufferKind.RETAIN_IGNORE_IF_CONTAINS;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Class to test that defip-N n.n.n.n works for all modes of operation for
 * Imatic8Prog.
 * <p>
 * Each test performs the same set of tests:
 * <ol>
 * <li></li>
 * <li></li>
 * <li></li>
 * <li></li>
 * <li></li>
 * <li></li>
 *
 * </ol>
 *
 * @author dbradley
 */
@Test
public class DefIpAllModes {

    protected CcCapture ccOut;
    protected CcCapture ccErr;

    @BeforeClass
    public void allocResoruces() {
        // create the testing directory
        File testUserDirFile = new File(Im8BoardIniTest.getTestUserDir());
        testUserDirFile.mkdirs();

        // CLI -----------
        // will capture both out and err streams during the tests
        this.ccOut = new CcCapture(System.out);
        this.ccOut.startCapture("*TestNG*");
        this.ccOut.stopCapture();
        this.ccOut.clearBuffer(RETAIN_IGNORE_IF_CONTAINS);

        this.ccErr = new CcCapture(System.err);
        this.ccErr.startCapture("*TestNG*");
        this.ccErr.stopCapture();
        this.ccErr.clearBuffer(RETAIN_IGNORE_IF_CONTAINS);

        // IACT ------------
        // LIB -------------
    }

    /**
     * Cleanup of console capture streams is done and any INI files that were
     * created during the testing.
     */
    @AfterClass
    final public void cleanupAfter() {
        // CLI ------------
        if (this.ccOut != null) {
            this.ccOut.cleanupOnExit();
            this.ccOut = null;
        }
        if (this.ccErr != null) {
            this.ccErr.cleanupOnExit();
            this.ccErr = null;
        }
        // IACT ------------

        // LIB -------------
    }

    @BeforeMethod
    final public void exitCaptureOnTop() {
        ExitInterceptException.setExitInterceptOn();

        // a clean slate is ensured
        Im8BoardIniTest.clearDefIpIniFile(-1);
    }

    @AfterMethod
    final public void exitCaptureOffTop() throws Exception {
        ExitInterceptException.setExitInterceptOff();

        // ensure no test files are left around (to avoid being
        // included in git commits)
        Im8BoardIniTest.clearDefIpIniFile(-1);
    }

    @Test
    public void defipValidator() {

        CliTestClass.cliEnter(ccErr, -1, "defip");
        CliTestClass.cliEnter(ccErr, -1, "defip-");
        CliTestClass.cliEnter(ccErr, -1, "defip-?");
        CliTestClass.cliEnter(ccErr, -1, "defip-0");
        CliTestClass.cliEnter(ccErr, -1, "defip-10");
        CliTestClass.cliEnter(ccErr, -1, "defip-9");

        CliTestClass.cliEnter(ccErr, -1, "defip-2 a");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 256");

        CliTestClass.cliEnter(ccErr, -1, "defip-2 256.168.1.5");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.256.1.5");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.168.256.5");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.168.1.256");

        CliTestClass.cliEnter(ccErr, -1, "defip-2 -1.168.1.5");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.-1.1.5");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.-1.5");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.1.-1");

        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.168.1.5.10");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.168");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.168.1");

        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.168.");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.168.1.");
        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.168.1.5.");

        CliTestClass.cliEnter(ccErr, -1, "defip-2 192.168.1.5");

    }
    
    @Test(dependsOnMethods = "defipValidator")
    public void defipCli() {
        
    }

    @Test(dependsOnMethods = "defipCli")
    public void defipCliEnded() {

    }

    @Test(dependsOnMethods = "defipCliEnded")
    public void defipIact() {

    }

    @Test(dependsOnMethods = "defipIact")
    public void defipIactEnded() {

    }

    @Test(dependsOnMethods = "defipIactEnded")
    public void defipLib() {

    }

}
