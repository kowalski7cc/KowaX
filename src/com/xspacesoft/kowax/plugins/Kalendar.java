package com.xspacesoft.kowax.plugins;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.windowsystem.Window;

public class Kalendar extends PluginBase implements KWindow {

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
	
	private void updateWindow(Window window) {
		window.setContent(new StringBuilder());
		window.getContent().append("<h4>Today is</h4>");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format1 = new SimpleDateFormat("EEE, d MMM yyyy hh 'o''clock' a, zzzz");
		window.getContent().append(format1.format(cal.getTime()));
	}

	@Override
	public void onCreateWindow(Window window) {
		window.setTitle("Calendar");
		updateWindow(window);
	}

	@Override
	public void onDestroyWindow(Window window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWindowHidden(Window window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWindowResume(Window window) {
		updateWindow(window);
	}

}
