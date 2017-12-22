/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package func;

import exitcapture.ExitCaptureException;
import imatic8.Im8BoardIniExt;
import java.io.File;
import java.io.FileFilter;
import static org.fest.reflect.core.Reflection.staticField;
import org.jtestdb.aid.console.CcCapture;
import static org.jtestdb.aid.console.CcCapture.ClearBufferKind.RETAIN_IGNORE_IF_CONTAINS;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author dbradley
 */
public abstract class Imatic8aseTest {
    protected String uDir;
    protected String testUserDir;

    /**
     * For testing purposes all classes are initialized for test configuration:
     * <ul>
     * <li>the INI file name variable is changed to represent file names that are test</li>
     * <li>the console capture for out and err are created (will be cleared on exit)</li>
     * <li>all INI files that are test names are cleared, so no hanging INI files 
     * for testing picked up</li>
     * </ul>
     */
    @BeforeClass
    final public void initResourcesTop() {
        String boardFilePath = staticField("RECORDER_FILE_NAME")
                .ofType(java.lang.String.class)
                .in(Im8BoardIniExt.class)
                .get();

        boardFilePath = boardFilePath.replace("%s", "Test%s");

        // need to change a final String
        staticField("RECORDER_FILE_NAME")
                .ofType(java.lang.String.class)
                .in(Im8BoardIniExt.class)
                .set(boardFilePath);

        // the user.dir for testing to run in
        this.uDir = System.getProperty("user.dir");
        this.testUserDir = this.uDir;

        // create the testing directory
        File testUserDirFile = new File(this.testUserDir);
        testUserDirFile.mkdirs();

        // a clean slate is ensured
        clearDefIpIniFile(-1);
    }

    /**
     * Cleanup of console capture streams is done and any INI files that were
     * created during the testing.
     */
    @AfterClass
    final public void cleanupAfterClassTop() {
 
        // ensure no test files are left around (to avoid being
        // included in git commits)
        clearDefIpIniFile(-1);
    }

    @BeforeMethod
    final public void exitCaptureOnTop() {
        ExitCaptureException.setExitCaptureOn();
    }

    @AfterMethod
    final public void exitCpatureOffTop() throws Exception {
        ExitCaptureException.setExitCaptureOff();
    }

    /**
     * Clear the INI file for board-N, or ALL if board-N is lower than 1. Boards
     * are numbered from 1 and are dependent on defip-N requests.
     *
     * @param boardN integer 1 and +, or less than 1 if all
     */
    final public static void clearDefIpIniFile(int boardN) {

        String boardFilePath = staticField("RECORDER_FILE_NAME")
                .ofType(java.lang.String.class)
                .in(Im8BoardIniExt.class)
                .get();
        // get the user-directory where the INI files are likely to be
        File userDirFile = new File(System.getProperty("user.dir"));

        // prepare to delete files
        if (boardN > 0) {
            // delete the single file (if it exists)
            File nIniFile = new File(userDirFile, String.format(boardFilePath, boardN));

            if (nIniFile.isFile()) {
                nIniFile.delete();
            }
            return;
        }
        // have to delete all files, get the list of INI files first
        // from the prefix and suffix of the file names
        String[] boardPartsArr = boardFilePath.split("%s");

        final String prefixPart = boardPartsArr[0];
        final String suffixPart = boardPartsArr[1];

        // get the list of INI files
        File[] allIniFilesArr = userDirFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                // find all files that are of INI formats for Imatic8Prog.
                String pathnameStr = pathname.getAbsolutePath().replaceAll("\\\\", "/");

                if (pathnameStr.toLowerCase().endsWith(suffixPart)) {
                    String[] pathPartsArr = pathnameStr.split("/");
                    if (pathPartsArr[pathPartsArr.length - 1].startsWith(prefixPart)) {
                        return true;
                    }
                }
                return false;
            }
        });
        // delete all the define-IP files.
        for (File f : allIniFilesArr) {
            if (f.isFile()) {
                f.delete();
            }
        }
    }
}
