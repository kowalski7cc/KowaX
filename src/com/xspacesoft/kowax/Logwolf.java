package com.xspacesoft.kowax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logwolf {

	private boolean debug = false;
	private boolean verbose = false;
	private boolean enableLogging = true;
	private PrintWriter shellOutput;
	private PrintWriter fileOutput;
	
	public Logwolf(PrintStream output, File file) throws FileNotFoundException, 
		UnsupportedEncodingException, IOException {
		if(!file.exists())
			file.createNewFile();
		fileOutput = new PrintWriter(file, "UTF-16");
		shellOutput = new PrintWriter(output, true);
	}
	
	public Logwolf() {
		shellOutput = null;
	}
	
	public Logwolf(PrintStream output) {
		shellOutput = new PrintWriter(output, true);
		shellOutput.flush();
	}

	public void message(String message) {
		log(message, null);
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
	
	public void e(Exception e) {
		e(e.toString());
	}
	
	private void log(String message, String logType) {
		if(!enableLogging)
			return;
		String output;
		if(logType!=null)
			output = getCurrentTime() + " [" + logType + "]: " + message;
		else
			output = getCurrentTime() + "         " + message;
		if (fileOutput!=null) {
			fileOutput.println(output);
		}
		if (shellOutput!=null) {
			shellOutput.println(output);
		}
	}
	
	public void close() {
		if(fileOutput!=null) {
			fileOutput.close();
			fileOutput = null;
		}
		shellOutput = null;
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
	
	public static void updateSplash(String s) {
		if((Initrfs.splash!=null) && (Initrfs.splash.isVisible()))
			Initrfs.splash.getLblLogwolf().setText(s);
	}
}
