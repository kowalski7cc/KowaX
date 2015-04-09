package com.xspacesoft.kowax.apis;

import com.xspacesoft.kowax.kernel.ShellPlugin.Intent;
import com.xspacesoft.kowax.shell.CommandRunner;

/** Intent hooks for applications */
public interface IntentListener {

	/** Returns intents supported by app */
	public Intent[] getIntents();
	/** Runs specific intent for the app */
	public void runIntent(Intent intent, CommandRunner commandRunner);
	
}
