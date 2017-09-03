package com.xspacesoft.kowax.plugins;

import java.util.Arrays;

import com.xspacesoft.kowax.engine.PluginBase;
import com.xspacesoft.kowax.engine.io.Stdio;
import com.xspacesoft.kowax.engine.shell.CommandRunner;

public class Echo extends PluginBase {

	@Override
	public String getAppletName() {
		return "echo";
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
	protected void runApplet(String[] command, Stdio stdio, CommandRunner commandRunner) {
		StringBuilder stringBuilder = new StringBuilder();
		if(command!=null)
			Arrays.asList(command).forEach(s -> stringBuilder.append(s + " "));
		stdio.println(stringBuilder.length()>0
				?stringBuilder.toString().substring(0,stringBuilder.length())
						:stringBuilder.toString());
	}

	@Override
	public String getDescription() {
		return "Prints a given text on the standard output";
	}

	@Override
	public String getHint() {
		return "Prints a given text on the standard output";
	}

}
