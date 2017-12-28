/* Copyright (c) 2017 dbradley. */
package func;

import exitcapture.ExitInterceptException;
import imatic8.Im8BoardIniTest;
import java.io.File;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author dbradley
 */
public abstract class Imatic8aseTest {

    protected String testUserDir;

    /**
     * For testing purposes all classes are initialized for test configuration:
     * <ul>
     * <li>the INI file name variable is changed to represent file names that
     * are test</li>
     * <li>the console capture for out and err are created (will be cleared on
     * exit)</li>
     * <li>all INI files that are test names are cleared, so no hanging INI
     * files for testing picked up</li>
     * </ul>
     */
    @BeforeClass
    public void initResourcesTop() {
        Im8BoardIniTest.testEmulate();

        // the user.dir for testing to run in
        this.testUserDir = Im8BoardIniTest.getTestUserDir();

        // create the testing directory
        File testUserDirFile = new File(this.testUserDir);
        testUserDirFile.mkdirs();

        // a clean slate is ensured
        Im8BoardIniTest.clearDefIpIniFile(-1);
    }

    /**
     * Cleanup of console capture streams is done and any INI files that were
     * created during the testing.
     */
    @AfterClass
    final public void cleanupAfterClassTop() {

        // ensure no test files are left around (to avoid being
        // included in git commits)
        Im8BoardIniTest.clearDefIpIniFile(-1);
    }

    @BeforeMethod
    final public void exitCaptureOnTop() {
        ExitInterceptException.setExitInterceptOn();
    }

    @AfterMethod
    final public void exitCaptureOffTop() throws Exception {
        ExitInterceptException.setExitInterceptOff();
    }
}
