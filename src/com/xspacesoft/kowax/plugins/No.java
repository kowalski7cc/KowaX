package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class No extends PluginBase {

	@Override
	public String getAppletName() {
		return "no";
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
		stdio.println("no");
	}

	@Override
	public String getDescription() {
		return "Prints no on the standard output";
	}

	@Override
	public String getHint() {
		return "Prints no on the standard output";
	}

}
