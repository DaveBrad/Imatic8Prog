/* Copyright (c) 2017 dbradley. */
package func;

import org.jtestdb.aid.console.CcCapture;
import static org.jtestdb.aid.console.CcCapture.ClearBufferKind.CLEAR_ALL;
import org.jtestdb.aid.console.CcTerminalInteractive;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
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

    private String uDir;
    private CcCapture ccOut;

    @BeforeClass
    public void allocCapture() {
        this.uDir = System.getProperty("user.dir");

        this.ccOut = new CcCapture(System.out);
    }

    @AfterClass
    public void cleanupAfterClass() {
        if (this.ccOut != null) {
            this.ccOut.cleanupOnExit();
            this.ccOut = null;
        }
    }

    @Test
    public void HelpInteractiveHelp() {
        CcTerminalInteractive termI = new CcTerminalInteractive("termI", System.out, System.err);

        termI.runTerm(this.uDir);
        termI.enterText(1000, "java -jar dist\\Imatic8Prog.jar");

        this.ccOut.startCapture("*TestNG*");
        termI.enterText(1000, "help");
        termI.enterText(1000, "exit");

        this.ccOut.stopCapture();

        this.ccOut.asOrdered("Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);

        this.ccOut.clearBuffer(CLEAR_ALL);
    }

    @Test(dependsOnMethods = "HelpInteractiveHelp")
    public void HelpInteractiveDashH() {
        CcTerminalInteractive termI = new CcTerminalInteractive("termI2", System.out, System.err);

        termI.runTerm(this.uDir);
        termI.enterText(1000, "java -jar dist\\Imatic8Prog.jar");

        this.ccOut.startCapture("*TestNG*");
        termI.enterText(1000, "-help");
        termI.enterText(1000, "exit");

        this.ccOut.stopCapture();

        this.ccOut.asOrdered("Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);
        this.ccOut.clearBuffer(CLEAR_ALL);
    }

    @Test(dependsOnMethods = "HelpInteractiveDashH")
    public void HelpInteractiveQues() {
        CcTerminalInteractive termI = new CcTerminalInteractive("termI", System.out, System.err);

        termI.runTerm(this.uDir);
        termI.enterText(1000, "java -jar dist\\Imatic8Prog.jar");

        this.ccOut.startCapture("*TestNG*");
        termI.enterText(1000, "?");
        termI.enterText(1000, "exit");

        this.ccOut.stopCapture();

        this.ccOut.asOrdered("Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);
        this.ccOut.clearBuffer(CLEAR_ALL);
    }

    @Test(dependsOnMethods = "HelpInteractiveQues")
    public void HelpInteractiveSlashQues() {
        CcTerminalInteractive termI = new CcTerminalInteractive("termI", System.out, System.err);

        termI.runTerm(this.uDir);
        termI.enterText(1000, "java -jar dist\\Imatic8Prog.jar");

        this.ccOut.startCapture("*TestNG*");
        termI.enterText(1000, "/?");
        termI.enterText(1000, "exit");

        this.ccOut.stopCapture();

        this.ccOut.asOrdered("Usage: - Command-line mode = 'Imatic8Prog.jar [args]'")
                .checkLineNContains(0);
        this.ccOut.clearBuffer(CLEAR_ALL);
    }

    @Test(dependsOnMethods = "HelpInteractiveSlashQues")
    public void LicenseInteractiveL() {
        CcTerminalInteractive termI = new CcTerminalInteractive("termI", System.out, System.err);

        termI.runTerm(this.uDir);
        termI.enterText(1000, "java -jar dist\\Imatic8Prog.jar");

        this.ccOut.startCapture("*TestNG*");
        termI.enterText(1000, "l");
        termI.enterText(1000, "exit");

        this.ccOut.stopCapture();

        this.ccOut.asOrdered("License: Imatic8Prog")
                .checkLineNContains(2);
        this.ccOut.clearBuffer(CLEAR_ALL);
    }

    @Test(dependsOnMethods = "LicenseInteractiveL")
    public void LicenseInteractiveLicense() {
        CcTerminalInteractive termI = new CcTerminalInteractive("termI", System.out, System.err);

        termI.runTerm(this.uDir);
        termI.enterText(1000, "java -jar dist\\Imatic8Prog.jar");

        this.ccOut.startCapture("*TestNG*");
        termI.enterText(1000, "license");
        termI.enterText(1000, "exit");

        this.ccOut.stopCapture();

        this.ccOut.asOrdered("License: Imatic8Prog")
                .checkLineNContains(2);
        this.ccOut.clearBuffer(CLEAR_ALL);
    }
}
