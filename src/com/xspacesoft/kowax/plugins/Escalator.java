package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class Escalator extends CronTab {

	@Override
	public String getAppletName() {
		return "Escalate";
	}

	@Override
	protected void runApplet(String command, Stdio stdio,
			CommandRunner commandRunner) {
		if(commandRunner.isSudo()) {
			Initrfs.getLogwolf().e("SYSTEM EXPLOITED!!!!");
		}
		stdio.println("Exploiting " + super.getAppletName() + " V" + super.getAppletVersion());
		stdio.println("Ready to escalate!!!");
		stdio.println("Press ENTER to continue (c to cancel)");
		if(stdio.scan().equalsIgnoreCase("c"))
			return;
		stdio.println("Escalating...");
		try {
			
		} catch (Exception e) {
			stdio.println("Escalation failed: " + e);
		}
		stdio.println("Check log");
	}

	
}
