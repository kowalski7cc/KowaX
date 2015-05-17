package com.xspacesoft.kowax.windowsystem;

public interface WindowManager {
	public boolean isAppOpen(String name);
	public Window getApplication(String name);
	public Window runApplication(String name);
	public void closeApplication(Window myWindow);
}
