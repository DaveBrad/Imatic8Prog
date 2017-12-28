/* Copyright (c) 2017 dbradley. */
package imatic8;

import static imatic8.Im8Io.ErrorKind.ERROR_ARG;
import java.io.File;
import java.util.ArrayList;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 *
 * @author dbradley
 */
@Test
public class Im8CommonTest {
    
    @AfterMethod
    public void teardown(){
      Im8BoardIniTest.clearDefIpIniFile(-1);  
    }

    @Test
    public void testDefIpAlreadyPresent() {
        Im8BoardIniTest.clearDefIpIniFile(-1);
        
        invokeProcessCheckForDefineIP(0, "defip-4 192.168.1.5", null);
        
        // the message contains the path and name of the INI just created
        File ini4File = Im8BoardIniTest.getBoardIniFile(new Im8Io(), 4);
        
        String alreadyErrMsg = String.format("ERROR-arg: defip-N: N %d already exists.\n"
                    + "      Need to delete file manually to overwrite.\n       %s\n",
                    4,
                    ini4File.getAbsolutePath());
        // remove the last cr/lf or lf (Windows, *nix) as the Im8Io process does
        // (NB: the line does not have any spaces at the end)
        alreadyErrMsg = alreadyErrMsg.trim();
        
        invokeProcessCheckForDefineIP(-1, "defip-4 192.168.1.5", alreadyErrMsg);
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpMissingN() {
        String expectedErrMsg = "ERROR-arg: not defip-N format, found 'defip-'.";
        invokeProcessCheckForDefineIP(-1, "defip-", expectedErrMsg);
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpMissingNotDigits() {
        String expectedErrMsg = "ERROR-arg: not defip-N format N not digit, found 'defip-a': For input string: \"a\".";
        invokeProcessCheckForDefineIP(-1, "defip-a", expectedErrMsg);
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpMissingNotDigits1() {
        String expectedErrMsg = "ERROR-arg: not defip-N format N not digit, found 'defip-a1': For input string: \"a1\".";
        invokeProcessCheckForDefineIP(-1, "defip-a1", expectedErrMsg);
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpMissingNotDigits2() {
        String expectedErrMsg = "ERROR-arg: not defip-N format N not digit, found 'defip-2a': For input string: \"2a\".";
        invokeProcessCheckForDefineIP(-1, "defip-2a", expectedErrMsg);
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpMissingNotDigits3() {
        String expectedErrMsg = "ERROR-arg: not defip-N format N not digit, found 'defip-/': For input string: \"/\".";
        invokeProcessCheckForDefineIP(-1, "defip-/", expectedErrMsg);
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpRequiresIpAddr() {
        String expectedErrMsg = "ERROR-arg: defip-N IP field [0] value not 0-255 error: 256.";
        invokeProcessCheckForDefineIP(-1, "defip-4 256.eee.fff.ggg", expectedErrMsg);
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpRequiresInvalidIpAddr() {
        String expectedErrMsg = "ERROR-arg: defip-N IP address not nnn.nnn.nnn.nnn (n.n.n.n) format.";
        invokeProcessCheckForDefineIP(-1, "defip-4 ddd", expectedErrMsg);

        invokeProcessCheckForDefineIP(-1, "defip-4 256", expectedErrMsg);
        invokeProcessCheckForDefineIP(-1, "defip-4 256.", expectedErrMsg);
        invokeProcessCheckForDefineIP(-1, "defip-4 192.", expectedErrMsg);
        invokeProcessCheckForDefineIP(-1, "defip-4 192.256", expectedErrMsg);
        invokeProcessCheckForDefineIP(-1, "defip-4 192.256.", expectedErrMsg);
        invokeProcessCheckForDefineIP(-1, "defip-4 192.256.1", expectedErrMsg);
        invokeProcessCheckForDefineIP(-1, "defip-4 192.256.1.", expectedErrMsg);
        invokeProcessCheckForDefineIP(-1, "defip-4 192.168.1.0.", expectedErrMsg);
        invokeProcessCheckForDefineIP(-1, "defip-4 192.168.1.0. ", expectedErrMsg);

    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpRequiresInvalidIpAddrNegativeN() {
        String expectedErrMsg = "ERROR-arg: defip-N IP field [3] value not 0-255 error: -1.";
        invokeProcessCheckForDefineIP(-1, "defip-4 192.168.1.-1", expectedErrMsg);

        expectedErrMsg = "ERROR-arg: defip-N IP field [2] value not 0-255 error: -3.";
        invokeProcessCheckForDefineIP(-1, "defip-4 192.168.-3.0", expectedErrMsg);

        expectedErrMsg = "ERROR-arg: defip-N IP field [1] value not 0-255 error: -168.";
        invokeProcessCheckForDefineIP(-1, "defip-4 192.-168.1.0", expectedErrMsg);

        expectedErrMsg = "ERROR-arg: defip-N IP field [0] value not 0-255 error: -192.";
        invokeProcessCheckForDefineIP(-1, "defip-4 -192.168.1.0", expectedErrMsg);
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpRequiresInvalidIpAddr2() {
        String expectedErrMsg0 = "ERROR-arg: defip-N IP field [0] value not 0-255 error: 256.";
        invokeProcessCheckForDefineIP(-1, "defip-4 256.eee.fff.ggg", expectedErrMsg0);

        String expectedErrMsg1 = "ERROR-arg: defip-N IP field [1] value not 0-255 error: 256.";
        invokeProcessCheckForDefineIP(-1, "defip-4 192.256.fff.ggg", expectedErrMsg1);

        String expectedErrMsg2 = "ERROR-arg: defip-N IP field [2] value not 0-255 error: 256.";
        invokeProcessCheckForDefineIP(-1, "defip-4 192.168.256.ggg", expectedErrMsg2);

        String expectedErrMsg3 = "ERROR-arg: defip-N IP field [3] value not 0-255 error: 256.";
        invokeProcessCheckForDefineIP(-1, "defip-4 192.168.0.256", expectedErrMsg3);

        //
        String expectedErrMsg4 = "ERROR-arg: defip-N IP field [3] not number error: eee.";
        invokeProcessCheckForDefineIP(-1, "defip-4 192.168.0.eee", expectedErrMsg4);
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpRequiresInvalidIpAddrIpNs() {
        invokeProcessCheckForDefineIP(-1, "defip-4 256.eee.fff.ggg",
                "ERROR-arg: defip-N IP field [0] value not 0-255 error: 256.");
    }

    @Test(dependsOnMethods = "testDefIpAlreadyPresent")
    public void testDefIpRequiresInvalidIpAddrIpNs2() {
        String expectedErrMsg = "ERROR-arg: defip-N IP field [0] value not 0-255 error: 256.";
        invokeProcessCheckForDefineIP(-1, "defip-4 256.eee.fff.ggg", expectedErrMsg);
    }

    // - - -  untility methods - - -
    /**
     * Invoke the processCheckDefIP method with the parameters expected for exit
     * code, the arguments to process and the expected response message from the
     * processing.
     *
     * @param exitCodeExpected integer value for an expected Exit code value
     * @param fullArgs         A single string of all the arguments (will be
     *                         tokenized internally)
     * @param responseExpected String of the expected error message (0th item
     *                         only), or NULL if no response expected
     */
    private void invokeProcessCheckForDefineIP(int exitCodeExpected, String fullArgs, String responseExpected) {
        Im8Io im8Io = new Im8Io();

        Im8Common.processCheckForDefineIP(im8Io, lc1StArg(fullArgs), allArgs(fullArgs));
        verifyResponse(im8Io, exitCodeExpected, responseExpected);
    }

    private void verifyResponse(Im8Io im8Io, int exitCodeExpected, String responseExpected) {
        ArrayList<String> output = im8Io.getReportIntoArray();
        assertEquals(im8Io.getExitCode(), exitCodeExpected, String.format("Exit code not as expected."));

        if (responseExpected == null) {
            assertEquals(output.size(), 0, "Some response found,none expected");
        } else {
            assertEquals(output.get(0), responseExpected, "Message does not match.");
        }
    }

    private String lc1StArg(String fullString) {
        String[] argsArr = allArgs(fullString);

        return argsArr[0].toLowerCase();
    }

    String[] allArgs(String fullString) {
        String[] argsArr = Im8Common.tokenSingleLine(fullString);
        return argsArr;
    }

}

//        // the following parameter needs to be an IPV4 address string
//        String ipArg = args[1];
//
//        String[] ipArgArr = ipArg.split("\\.");
//
//        int ipLen = ipArgArr.length;
//        if (ipLen != 4) {
//            m8Io.err(-1).sprintf(ERROR_ARG, "defip-N IP address not nnn.nnn.nnn.nnn (n.n.n.n) format.\n", arg0LC);
//            return true; // this will be an error condition so stop proceeding forward
//        }
//        // 
//        boolean iperror = false;
//
//        for (int i = 0; i < ipLen; i++) {
//            // validate the string is an IPV4 address
//            try {
//                int value = Integer.parseInt(ipArgArr[i]);
//
//                if (value < 0 || value > 255) {
//                    m8Io.err(-1).sprintf(ERROR_ARG, "defip-N IP field [%d] value not 0-255 error: %s.\n", i, ipArgArr[i]);
//                    iperror = true;
//                }
//
//            } catch (NumberFormatException ex) {
//                m8Io.err(-1).sprintf(ERROR_ARG, "defip-N IP field [%d] not number error: %s.\n", i, ipArgArr[i]);
//                iperror = true;
//            }
//        }
//        // if an IP error occured then need to proceed
//        if (iperror) {
//            return true;
//        }
//        // create the INI file for the board and the IP address provided, as long
//        // as there is not an existing one
//        File boardsIniFile = Im8BoardIni.getBoardIniFile(m8Io, nBoard);
//
//        if (boardsIniFile.exists()) {
//            m8Io.err(-1).sprintf(ERROR_ARG, "defip-N: N %d already exists.\n"
//                    + "      Need to delete file manually to overwrite.\n       %s\n",
//                    nBoard,
//                    boardsIniFile.getAbsolutePath());
//            return true;
