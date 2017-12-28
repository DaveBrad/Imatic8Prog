/* Copyright (c) 2017 dbradley */
package func.library;

import func.Imatic8aseTest;
import imatic8.Im8BoardIniTest;
import java.io.File;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author dbradley
 */
abstract public class LibTestClass extends Imatic8aseTest {

//    @Override
    @BeforeClass
    public void initResourcesTop() {
        String specialLibUserDir;
        File specialLibUserDirFile;

        // test with a special directory for the directory
        specialLibUserDir = System.getProperty("user.dir") + "/innerTestDir";

        // create the specific directory for running the library in
        specialLibUserDirFile = new File(specialLibUserDir);
        specialLibUserDirFile.mkdirs();

        specialLibUserDirFile.deleteOnExit();

        // the user directory needs to be set before the testing begins
        Im8BoardIniTest.setTestUserDirInApplication(specialLibUserDir);

//        super.initResourcesTop();
        
         Im8BoardIniTest.testEmulate();

        // the user.dir for testing to run in
//        this.testUserDir = Im8BoardIniTest.getTestUserDir();

        // create the testing directory
        File testUserDirFile = new File(Im8BoardIniTest.getTestUserDir());
        testUserDirFile.mkdirs();

        // a clean slate is ensured
        Im8BoardIniTest.clearDefIpIniFile(-1);
    }
}
