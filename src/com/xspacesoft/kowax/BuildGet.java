package com.xspacesoft.kowax;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BuildGet {
	
	private final static String BUILD_DATA = "com.xspacesoft.kowax.build"; //$NON-NLS-1$
	
	private final static ResourceBundle BUILD_RESOURCE = ResourceBundle.getBundle(BUILD_DATA);
	
	public static String getString(String key) {
		try {
			return BUILD_RESOURCE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
			
	}
	
	public static Boolean stringToBoolean(String text) {
		if(text.equalsIgnoreCase("true"))
			return true;
		else if(text.equalsIgnoreCase("false"))
			return false;
		else
			return null;
	}

}
