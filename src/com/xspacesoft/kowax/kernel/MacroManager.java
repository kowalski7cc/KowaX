package com.xspacesoft.kowax.kernel;

import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class MacroManager extends PluginBase {

	@Override
	public String getAppletName() {
		return "Macro";
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
	protected void runApplet(String command, Stdio stdio,
			CommandRunner commandRunner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescription() {
		return "Run macros";
	}

	@Override
	public String getHint() {
		return "Usage: Macro (run|list|delete)";
	}

}
