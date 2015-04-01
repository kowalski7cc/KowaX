package com.xspacesoft.kowax.kernel;

public interface Service {
	
	public Boolean isRunning();
	
	public void init();
	
	public void halt();
	
}