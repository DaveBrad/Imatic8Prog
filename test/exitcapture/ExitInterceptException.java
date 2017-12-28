/* Copyright (c) 2017 dbradley. */
package exitcapture;

import java.security.Permission;

/**
 * Class that is used for intercepting a <code>System.exit(...):</code> in a
 * testing environment for Imatic8Prog; otherwise, the testing framework will be
 * exited too by the exit from the Imatic8Prog application$#46; Provides
 * the set ON and OFF static methods to support control of capability within the
 * testing scripts (typically applied in BeforeClass and AfterClass methods).
 * 
 * @author dbradley
 */
public class ExitInterceptException extends SecurityException {

    /** the exit code value that the intercept exception is used to capture */
    private final int exitCode;

    /**
     *
     * @param exitCode
     */
    public ExitInterceptException(int exitCode) {
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

    /**
     * Set the System.exit(...) intercept ON while running in a testing
     * framework so an application-under-test exit does not cause an exit of the
     * framework runtime too.
     */
    static public void setExitInterceptOn() {
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if (perm instanceof RuntimePermission && perm.getName().startsWith("exitVM.")) {
                    System.out.printf("TestEnv: EXIT intercepted - code is: %s\n",
                            Integer.parseInt(perm.getName().substring("exitVM.".length())));
                    throw new ExitInterceptException(
                            Integer.parseInt(perm.getName().substring("exitVM.".length())));
                }
            }
        });
    }

    /**
     * Set the System.exit(...) intercept OFF while running in a testing
     * framework&#46; WARNING: if this is not done by the testing framework
     * clean-up capabilities, it is likely that testing will not actually be
     * able to exit.
     */
    static public void setExitInterceptOff() {
        System.setSecurityManager(null);
    }
}
