package com.xspacesoft.kowax;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PomParser {
	
	private final static String path = "META-INF/maven/com.xspacesoft.kowalski7cc.kowax/KowaX/pom.properties";
	
	public static Properties load() {
		Properties properties = new Properties();
		try {
			InputStream inputStream =Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
			properties.load(inputStream);
		} catch (IOException | NullPointerException e) { }
		return properties;
	}

}
