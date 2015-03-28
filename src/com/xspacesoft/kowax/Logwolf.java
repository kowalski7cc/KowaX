package com.xspacesoft.kowax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logwolf {

	private boolean debug = false;
	private boolean verbose = false;
	private boolean enableLogging = true;
	private PrintWriter writer;

	public Logwolf(File file) throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter("the-file-name.txt", "UTF-8");
	}
	
	public Logwolf(boolean enableLogging) {
		this.enableLogging = enableLogging;
	}
	
	public Logwolf() {
		
	}
	
	public void i(String message) {
		log(message, "INFO");
	}
	
	public void d(String message) {
		if(debug) {
			log(message, "DEBG");
		}
	}
	
	public void v(String message) {
		if(verbose) {
			log(message, "VERB");
		}
	}
	
	public void w(String message) {
		log(message, "WARN");
	}
	
	public void e(String message) {
		log(message, "ERRO");
	}
	
	private void log(String message, String logType) {
		if(!enableLogging)
			return;
		String output = getCurrentTime() + " [" + logType + "]: " + message;
		System.out.println(output);
		if(writer!=null) {
			writer.println(output);
		}
	}
	
	public void close() {
		writer.close();
	}
	
	private static String getCurrentTime() {
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat ("HH:mm");
		return ft.format(dNow);
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isLoggingEnabled() {
		return enableLogging;
	}

	public void setLoggiongEnabled(boolean enableLogging) {
		this.enableLogging = enableLogging;
	}
}
