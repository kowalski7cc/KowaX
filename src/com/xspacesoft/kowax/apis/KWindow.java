package com.xspacesoft.kowax.apis;

import com.xspacesoft.kowax.windowsystem.windows.Window;

public interface KWindow {

	public void onCreateWindow(Window window);
	
	public void onDestroyWindow(Window window);
	
	public void onWindowHidden(Window window);
	
	public void onWindowResume(Window window);
	
	public String getAppletName();
	
	public String getAppletAuthor();
	
	public String getAppletVersion();
	
	public String getDescription();
}
