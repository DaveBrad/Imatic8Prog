/* Copyright (c) 2017 dbradley. */
package exitcapture;

import java.security.Permission;

/**
 * Class that is used for capturing a exit in the testing of the Imatic8Prog (as
 * it uses System.exit as a command-line program).
 *
 * @author dbradley
 */
public class ExitCaptureException extends SecurityException {

    /** the exit code value that the exception is used to capture */
    private int exitCode;

    /**
     *
     * @param exitCode
     */
    public ExitCaptureException(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Get the exit code.
     *
     * @return integer
     */
    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * The message for the exception.
     *
     * @return string of message
     */
    @Override
    public String getMessage() {
        return String.format("attempted to exit with status %s", exitCode);
    }

    static public void setExitCaptureOn() {
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if (perm instanceof RuntimePermission && perm.getName().startsWith("exitVM.")) {
                    System.out.printf("EXIT captured code: %s\n",
                            Integer.parseInt(perm.getName().substring("exitVM.".length())));
                    throw new ExitCaptureException(
                            Integer.parseInt(perm.getName().substring("exitVM.".length())));
                }
            }
        });
    }

    static public void setExitCaptureOff() {
        System.setSecurityManager(null);
    }
}
