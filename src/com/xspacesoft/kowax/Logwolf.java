package com.xspacesoft.kowax;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.xspacesoft.kowax.engine.io.OutputWriter;
import com.xspacesoft.kowax.engine.shell.KonsoleIO;

public class Logwolf {

	//	private boolean debug = false;
	//	private boolean verbose = false;
	private List<OutputWriter> outputs;
	private LogLevel logLevel;
	private boolean loggingEnabled = true;

	public enum LogLevel {
		CRITICAL(0),
		ERROR(1),
		WARNING(2),
		INFORMATION(3),
		VERBOSE(4),
		DEBUG(5);

		private int level;

		private LogLevel(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}

		public static LogLevel getByLevel(int level) {
			return Arrays.asList(LogLevel.values()).stream().filter(p -> p.getLevel()==level).findFirst().orElse(null);
		}

		@Override
		public String toString() {
			String name = this.name();
			return name.charAt(0) + name.substring(0).toLowerCase();
		}

		public String getLogTitle() {
			return this.name().substring(0, this.name().length()>4?4:this.name().length()-1);
		}
	}

	public Logwolf(OutputWriter outputWriter, LogLevel logLevel) {
		this.logLevel = logLevel;
		outputs = new ArrayList<OutputWriter>(1);
		outputs.add(outputWriter);
	}

	public Logwolf(LogLevel logLevel) {
		this.logLevel = logLevel;
		outputs = new ArrayList<OutputWriter>(1);
		OutputWriter outputWriter = new KonsoleIO();
		outputs.add(outputWriter);
	}

	public void message(String message) {
		log(message, null);
	}

	public void i(String message) {
		log(message, LogLevel.INFORMATION);
	}

	public void d(String message) {
		log(message, LogLevel.DEBUG);
	}

	public void v(String message) {
		log(message, LogLevel.VERBOSE);
	}

	public void w(String message) {
		log(message, LogLevel.WARNING);
	}

	public void e(String message) {
		log(message, LogLevel.ERROR);
	}

	public void c(String message) {
		log(message, LogLevel.CRITICAL);
	}

	public void e(Exception e) {
		e(e.toString());
	}

	private void log(String message, LogLevel logLevel) {
		if(loggingEnabled) {
			if(this.logLevel.level>=logLevel.level) {
				StringBuilder output = new StringBuilder();
				output.append(getCurrentTime());
				output.append(getCallerClassName()==null?"":(" [" + getCallerClassName() + "]"));
				output.append(logLevel==null?"":(" [" + logLevel.getLogTitle() + "]"));
				output.append(": " + message);
				outputs.forEach(o -> o.println(output.toString()));
			}
		}
	}

	private static String getCurrentTime() {
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat ("HH:mm");
		return ft.format(dNow);
	}



	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	public List<OutputWriter> getOutputs() {
		return outputs;
	}

	private String getCallerClassName() {
		StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
		for (int i = 1; i < stackTraceElement.length; i++) {
			if(!stackTraceElement[i].getClassName().equals(this.getClass().getName())) {
				String[] name = stackTraceElement[i].getClassName().split("\\.");
				return name[name.length-1];
			}
		}
		return this.getClass().getSimpleName();
	}

	public boolean isDebug() {
		return logLevel.equals(LogLevel.DEBUG);
	}
}
