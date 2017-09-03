package com.xspacesoft.kowax;

import java.util.Properties;

public class PropertiesParser {
	
	private Properties properties;
	
	public PropertiesParser() {
		properties = new Properties();
	}

	public PropertiesParser(Properties properties) {
		this.properties = properties;
	}
	
	public Integer getInteger(String key) {
		String string = properties.getProperty(key);
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}
	
	public String getString(String key) {
		return properties.getProperty(key);
	}
	
	public Long getLong(String key) {
		String string = properties.getProperty(key);
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}
	
	public Float getFloat(String key) {
		String string = properties.getProperty(key);
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}
	
	public Double getDouble(String key) {
		String string = properties.getProperty(key);
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}
	
	
	public Boolean getBoolean(String key) {
		String string = properties.getProperty(key);
		try {
			return Boolean.parseBoolean(string);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}
	
	public Integer getInteger(String key, int defaultValue) {
		Integer i = getInteger(key);
		return i!=null?i:defaultValue;
	}
	
	public String getString(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
	
	public Double getDouble(String key, double defaultValue) {
		Double d = getDouble(key);
		return d!=null?d:defaultValue;
	}
	
	public Long getLong(String key, long defaultValue) {
		Long l = getLong(key);
		return l!=null?l:defaultValue;
	}
	
	public Float getFloat(String key, float defaultValue) {
		Float f = getFloat(key);
		return f!=null?f:defaultValue;
	}
	
	public Boolean getBoolean(String key, boolean defaultValue) {
		Boolean b = getBoolean(key);
		return b!=null?b:defaultValue;
	}
	
	public void setInteger(String key, Integer value) {
		properties.setProperty(key, value.toString());
	}
	
	public void setLong(String key, Long value) {
		properties.setProperty(key, value.toString());
	}
	
	public void setFloat(String key, Float value) {
		properties.setProperty(key, value.toString());
	}
	
	public void setDouble(String key, Double value) {
		properties.setProperty(key, value.toString());
	}
	
	public void setString(String key, String value) {
		properties.setProperty(key, value.toString());
	}
	
	public void setBoolean(String key, Boolean value) {
		properties.setProperty(key, value.toString());
	}

}
