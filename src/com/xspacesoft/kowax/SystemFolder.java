package com.xspacesoft.kowax;

public enum SystemFolder {
	USER_HOME("Home"),
	CONFIGURATIONS("Config"),
	TEMPORARY("Temp"),
	APPLICATIONS("Apps"),
	SYSTEM("System"),
	ROOT("");
	
	private String pathName;

	private SystemFolder(String pathName) {
		this.pathName = pathName;
	}

	public String getPathName() {
		return pathName;
	}
	
}
