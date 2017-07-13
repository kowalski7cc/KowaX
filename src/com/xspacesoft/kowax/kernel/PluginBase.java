package com.xspacesoft.kowax.kernel;

import com.xspacesoft.kowax.exceptions.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public abstract class PluginBase {
	
	/** Indicates applet name, called by CommandRUnner */
	public abstract String getAppletName();
	/** Indicates applet version */
	public abstract String getAppletVersion();
	/** Indicates applet author */
	public abstract String getAppletAuthor();

	/** Method called to start task */
	public void start(String command, Stdio sockethelper, CommandRunner commandRunner) throws MissingPluginCodeException {
		try {
			runApplet(command, sockethelper, commandRunner);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MissingPluginCodeException("Source not found");
		}
	}
	
	/** Code to be run */
	protected abstract void runApplet(String command, Stdio stdio, CommandRunner commandRunner);
	
	/** Indicates applet description */
	public abstract String getDescription();
	
	/** Indicates applet hint when no command is given (return null to disable) */
	public abstract String getHint();
	
}
