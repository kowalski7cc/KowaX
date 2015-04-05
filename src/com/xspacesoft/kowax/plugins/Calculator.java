package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class Calculator extends ShellPlugin {

	@Override
	public String getAppletName() {
		return "Calc";
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
		stdio.println(String.valueOf(solve(command, stdio)));
	}

	private int solve(String expression, Stdio stdio) {
		if (expression.contains("(")) {
			String expressions[] = expression.split("()");
			for (String string : expressions) {
				stdio.println(string + " hey");
			}
		}
		return 0;
	}

	@Override
	public String getDescription() {
		return "A simple calculator for KowaX";
	}

	@Override
	public String getHint() {
		return "Usage: calc (expression)";
	}

}
