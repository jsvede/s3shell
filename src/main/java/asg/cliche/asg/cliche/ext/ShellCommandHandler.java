package asg.cliche.asg.cliche.ext;

import asg.cliche.CLIException;
import asg.cliche.ShellManageable;

/**
 * @author jsvede
 */
public interface ShellCommandHandler extends ShellManageable {

    /**
     * Allows the implementer to receive the command line.
     *
     * @throws Exception
     */
    public void processCommand(String cmdLine) throws CLIException;
}
