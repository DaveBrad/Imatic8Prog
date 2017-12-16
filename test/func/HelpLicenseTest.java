/* Copyright (c) 2017 dbradley. */
package func;

import imatic8.Imatic8Prog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
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
//        CcCapture captureUnit = new CcCapture(System.out);
//        captureUnit.startCapture();

        InputStream origSystemIn = System.in;

        String input = String.format("help\n");
//        System.setIn(new ByteArrayInputStream(input.getBytes()));

        BufferedOutputStream outPipe = null;

        PipedInputStream pipeIn = new PipedInputStream();
        BufferedInputStream in = new BufferedInputStream(pipeIn);

//         System.setIn(pipeIn);
        PipedOutputStream ppp = new PipedOutputStream();

        try {
            ppp.connect(pipeIn);

            ppp.write(input.getBytes());
//            ppp.flush();
//            ppp.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.setIn(in);

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
        
         try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(HelpLicenseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        captureUnit.stopCapture();
//        captureUnit.printAllLines();

        System.setIn(origSystemIn);

    }
    
    
//    @Test (dependsOnMethods="HelpInteractive")
//    public void HelpInteractive2() {
////        CcCapture captureUnit = new CcCapture(System.out);
////        captureUnit.startCapture();
//
//        InputStream origSystemIn = System.in;
//        
//        String input = String.format("help\n");
//        System.setIn(new ByteArrayInputStream(input.getBytes()));
//
//        // run the main in its own thread so it starts  
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                 Imatic8Prog.main(new String[]{});
//            }
//        };
//        // in interactive mode main will wait on the input stream before
//        // preceding to process, so the setIn below acts as an input
//        Thread t = new Thread(r);
//        t.start();
//        
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(HelpLicenseTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
////        String input = String.format("help");
////        System.setIn(new ByteArrayInputStream(input.getBytes()));
//   
////        captureUnit.stopCapture();
////        captureUnit.printAllLines();
//        
//        System.setIn(origSystemIn);
//
//    }
}
