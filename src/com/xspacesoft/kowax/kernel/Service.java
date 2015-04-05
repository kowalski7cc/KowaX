package com.xspacesoft.kowax.kernel;

public interface Service {
	
	public Boolean isServiceRunning();
	
	public void startService();
	
	public void stopService();

	public String getServiceName();
	
}