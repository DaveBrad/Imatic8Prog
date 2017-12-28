/* Copyright (c) 2017 dbradley. */
package imatic8;

import boardemulator.Im8TestSocket;
import java.io.File;
import java.io.FileFilter;
import static org.fest.reflect.core.Reflection.staticField;

/**
 * Class that extends a (declared) class within the Imatic8XXXX program
 * structure to provide access to data fields necessary for processing the INI
 * files when performing tests.
 *
 * @author dbradley
 */
public class Im8BoardIniTest extends Im8BoardIni {

    private static boolean runOnce = true;

    public Im8BoardIniTest() {
        //  
        super();
    }

    Im8BoardIniTest(Im8Io m8Io, int boardN) {
        super(m8Io, boardN);
    }

    public static File getBoardIniFile4Test(int boardNumberP) {
        testEmulate();
        
        // the test environment is different
        String uDir4Test = Im8Io.getUserDir();
        return new File(uDir4Test, String.format(RECORDER_FILE_NAME, boardNumberP));
    }

    public static void testEmulate() {
        if (runOnce) {
            // use an alternative Socket.class so as to employ the use of a
            // board-emulator that is a board-server
            Im8Socket.socketProvidedClass = Im8TestSocket.class;

            // the INI file needs to be a different name for testing, so as to
            // avoid overwriting none test INI file
            RECORDER_FILE_NAME = RECORDER_FILE_NAME + ".Test";

            runOnce = false;
        }
    }

    /**
     * Clear the INI file for board-N, or ALL if board-N is lower than 1. Boards
     * are numbered from 1 and are dependent on defip-N requests.
     *
     * @param boardN integer 1 and +, or less than 1 if all
     */
    final public static void clearDefIpIniFile(int boardN) {
        testEmulate();

        // get the user-directory where the INI files are likely to be
        File userDirFile = new File(getTestUserDir());

        // prepare to delete files
        if (boardN > 0) {
            // delete the single file (if it exists)
            File nIniFile = new File(String.format(RECORDER_FILE_NAME, boardN));

            if (nIniFile.isFile()) {
                nIniFile.delete();
            }
            return;
        }
        // have to delete all files, get the list of INI files first
        // from the prefix and suffix of the file names
        String[] boardPartsArr = RECORDER_FILE_NAME.split("%s");

        final String prefixPart = boardPartsArr[0];
        final String suffixPart = boardPartsArr[1];

        // get the list of INI files
        File[] allIniFilesArr = userDirFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                // find all files that are of INI formats for Imatic8Prog.
                String pathnameStr = pathname.getAbsolutePath().replaceAll("\\\\", "/");

                if (pathnameStr.endsWith(suffixPart)) {
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
    //99  

    public static String getTestUserDir() {
        return Im8Io.getUserDir();
    }
    
    public static void setTestUserDirInApplication(String testUserDir){
        // set the user dir for//99
         staticField("userDir").ofType(String.class).in (Im8Io.class).set(testUserDir);
    }
}
