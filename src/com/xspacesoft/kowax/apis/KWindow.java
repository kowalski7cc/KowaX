package com.xspacesoft.kowax.apis;

import com.xspacesoft.kowax.windowsystem.Window;

public interface KWindow {

	public void onCreateWindow(Window window);
	
	public void onDestroyWindow(Window window);
	
	public void onWindowHidden(Window window);
	
	public String getAppletName();
	
}
