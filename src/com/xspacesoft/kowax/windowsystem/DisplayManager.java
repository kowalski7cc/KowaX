package com.xspacesoft.kowax.windowsystem;

import java.util.List;

import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.apis.KernelAccess;

public interface DisplayManager extends KernelAccess{

	public void setDisplayManager();

	public List<KWindow> getSupportedApps();
	
	public void logout(String user);
}
