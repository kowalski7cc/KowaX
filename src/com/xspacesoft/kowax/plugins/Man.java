package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class Man extends PluginBase {

	@Override
	public String getAppletName() {
		// TODO Auto-generated method stub
		return null;
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
		if((command==null)||(command.length()<1)) {
			stdio.print(getHint());
		}
	}

	@Override
	public String getDescription() {
		return "Get help about plugins";
	}

	@Override
	public String getHint() {
		return "man (page)";
	}

}
