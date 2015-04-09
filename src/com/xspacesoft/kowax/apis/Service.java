package com.xspacesoft.kowax.apis;

public interface Service {
	
	public Boolean isServiceRunning();
	
	public void startService();
	
	public void stopService();

	public String getServiceName();
	
}