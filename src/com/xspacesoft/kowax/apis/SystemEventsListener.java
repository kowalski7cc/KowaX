package com.xspacesoft.kowax.apis;

import com.xspacesoft.kowax.shell.CommandRunner;

/** Intent hooks for applications */
public interface SystemEventsListener {

	/** Returns intents supported by app */
	public SystemEvent[] getEvents();
	/** Runs specific intent for the app */
	public void runIntent(SystemEvent event, String extraValue, CommandRunner commandRunner);
	
}
