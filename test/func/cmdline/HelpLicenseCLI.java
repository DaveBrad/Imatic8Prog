/* Copyright (c) 2017 dbradley. */
package func.cmdline;

import org.jtestdb.aid.console.CcCapture;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * <p>
 * Due to fact that responses need to be tested via the console, and only one
 * console per test run is allowed tests are written as sequential using TestNG.
 * <p>
 * Tests involve the three modes of command-line, interactive and library
 * execution.
 *
 * @author dbradley
 */
@Test
public class HelpLicenseCLI extends CliTestClass {

    private CcCapture ccOut;

    private String terminalImatic8Prog;

    @BeforeClass
    public void allocCapture() {
        this.ccOut = new CcCapture(System.out);

        this.ccOut.startCapture("*TestNG*");
        this.ccOut.stopCapture();
        this.ccOut.clearBuffer();

    }

    @AfterClass
    public void cleanupAfterClass() {
        if (this.ccOut != null) {
            this.ccOut.cleanupOnExit();
            this.ccOut = null;
        }
    }

    @Test
    public void HelpInteractiveHelpCli() {

        cliEnter(ccOut, 0, "help");

        this.ccOut.asOrdered("Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);
        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "HelpInteractiveHelpCli")
    public void HelpInteractiveQuesCli() {
        cliEnter(ccOut, 0, "?");

        this.ccOut.asOrdered("Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);
        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "HelpInteractiveQuesCli")
    public void HelpInteractiveSlashQuesCli() {
        cliEnter(ccOut, 0, "/?");

        this.ccOut.asOrdered("Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);
        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "HelpInteractiveSlashQuesCli")
    public void HelpInteractiveLicenseCli() {
        cliEnter(ccOut, 0, "LICENSE");

        this.ccOut.asOrdered("License: Imatic8Prog").checkLineNContains(2);
        this.ccOut.clearBuffer();
    }

    @Test(dependsOnMethods = "HelpInteractiveLicenseCli")
    public void HelpInteractiveLCli() {
        cliEnter(ccOut, 0, "l");

        this.ccOut.asOrdered("License: Imatic8Prog").checkLineNContains(2);
        this.ccOut.clearBuffer();
    }
}
