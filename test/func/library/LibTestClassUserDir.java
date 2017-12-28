/* Copyright (c) 2017 dbradley. */
package func.library;

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
abstract public class LibTestClassUserDir {
    
    /**
     * Provide a String directory path on the local system that
     * will be the working/user directory for library-mode execution
     * of the IMatic8Prog.
     * <p>
     * Library-mode provides a means to select its directory for the
     * storage of work file (INI's) which may be different from the
     * System.gerProperty("user.dir");
     * <p>
     * Interactive and command-line modes always use the user.dir that
     * the Imatic8Prog is started in.
     * 
     * @return String of a path
     */
    abstract String provideUserDirForLibraryTest();

    @BeforeClass
    public void initResourcesTop() {
        String specialLibUserDir;
        File specialLibUserDirFile;

        // test with a special directory for the directory
        specialLibUserDir = provideUserDirForLibraryTest();

        // create the specific directory for running the library in
        specialLibUserDirFile = new File(specialLibUserDir);
        specialLibUserDirFile.mkdirs();

        specialLibUserDirFile.deleteOnExit();

        // the user directory needs to be set before the testing begins
        Im8BoardIniTest.setTestUserDirInApplication(specialLibUserDir);

        Im8BoardIniTest.testEmulate();

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
    final public void exitCpatureOffTop() throws Exception {
        ExitInterceptException.setExitInterceptOff();
    }
}
