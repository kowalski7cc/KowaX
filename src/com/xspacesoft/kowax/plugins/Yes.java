package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class Yes extends PluginBase {

	@Override
	public String getAppletName() {
		return "yes";
	}

	@Override
	public String getAppletVersion() {
		return "1.0";
	}

	@Override
	public String getAppletAuthor() {
		return "kowalski7cc";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		stdio.println("yes");
	}

	@Override
	public String getDescription() {
		return "Prints yes on the standard output";
	}

	@Override
	public String getHint() {
		return "Prints yes on the standard output";
	}

}
