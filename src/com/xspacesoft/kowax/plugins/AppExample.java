package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class AppExample extends ShellPlugin {
	
	public AppExample() {
		
	}
	
	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		stdio.println("Ciao mondo!");
		commandRunner.sudo(null);
	}

	@Override
	public String getHint() {
		return "Test hint";
	}

	@Override
	public String getAppletName() {
		return "Test";
	}

	@Override
	public String getAppletVersion() {
		return "1.0";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowa";
	}

	@Override
	public String getDescription() {
		return null;
	}
	
}
