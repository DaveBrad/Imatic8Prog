/* Copyright (c) 2017 dbradley. */
package func;

import imatic8.Imatic8Prog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.jtestdb.aid.console.CcCapture;
import org.testng.annotations.Test;

/**
 *
 * <p>
 * Due to fact that responses need to be tested via the console, and only one
 * console per test run is allowed test are written as sequential using TestNG.
 * <p>
 * Tests involve the three modes of command-line, interactive and library
 * execution.
 *
 * @author dbradley
 */
@Test
public class HelpLicenseTest {

    @Test
    public void HelpInteractive() {
        CcCapture captureUnit = new CcCapture(System.out);
        captureUnit.startCapture();

        InputStream origSystemIn = System.in;
        
//         System.setIn(new BufferedInputStream());

        String input = String.format("help\nh");
        
//        BufferedOutputStream out2InStream = new BufferedOutputStream(out);
//
//        ByteArrayOutputStream outToInStream = new ByteArrayOutputStream();
//
//       
//
        // run the main in its own thread so it starts
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                 Imatic8Prog.main(new String[]{});
            }
        };
        // in interactive mode main will wait on the input stream before
        // preceding to process, so the setIn below acts as an input
        Thread t = new Thread(r);
        t.start();

        System.setIn(new ByteArrayInputStream(input.getBytes()));

        captureUnit.stopCapture();
        captureUnit.printAllLines();

    }
}
