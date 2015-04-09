package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class Kalendar extends ShellPlugin {

	@Override
	public String getAppletName() {
		return "cal";
	}

	@Override
	public String getAppletVersion() {
		return "1.0A";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDescription() {
		return "A simple calendar for KowaX";
	}

	@Override
	public String getHint() {
		return null;
	}

}
