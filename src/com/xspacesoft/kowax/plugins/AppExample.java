package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.windowsystem.Window;

public class AppExample extends PluginBase implements KWindow {
	
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

	@Override
	public void onCreateWindow(Window window) {
		window.getContent().append("<h1>:D</h1>");
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
		// TODO Auto-generated method stub
		
	}
	
}
