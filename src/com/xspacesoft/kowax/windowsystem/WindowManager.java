package com.xspacesoft.kowax.windowsystem;

import java.util.Map;

public interface WindowManager {
	public boolean isAppOpen(String name);
	public Window getApplication(String name, Map<String, String> params);
	public Window runApplication(String name, Map<String, String> params);
	public void closeApplication(Window myWindow);
	public void close();
}
