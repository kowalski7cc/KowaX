package com.xspacesoft.kowax.plugins;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class Kalendar extends PluginBase {

	@Override
	public String getAppletName() {
		return "Cal";
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
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		SimpleDateFormat format1 = new SimpleDateFormat("EEE, d MMM yyyy hh 'o''clock' a, zzzz");
		stdio.println("Today is " + format1.format(cal.getTime()));
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
