/* Copyright (c) 2017 dbradley. */
package func.cmdline;

import exitcapture.ExitInterceptException;
import func.Imatic8aseTest;
import imatic8.Imatic8Prog;
import org.jtestdb.aid.console.CcCapture;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 *
 * @author dbradley
 */
abstract  public class CliTestClass extends Imatic8aseTest{

    public static void cliEnter(CcCapture ccAbc, int exitCodeEpected, String... args) {
        ccAbc.startCapture();
        try {
            Imatic8Prog.main(args);
            
            ccAbc.stopCapture();
            assertTrue(false, "No exit detected.");

        } catch (ExitInterceptException x) {
            ccAbc.stopCapture();
            assertEquals(x.getExitCode(), exitCodeEpected, "Exit code issue:");
        }
    }

    static void cliEnter(int exitCodeEpected, String... args) {

        try {
            Imatic8Prog.main(args);
            assertTrue(false, "No exit detected.");

        } catch (ExitInterceptException x) {
            assertEquals(x.getExitCode(), exitCodeEpected, "Exit code issue:");
        }
    }
}
