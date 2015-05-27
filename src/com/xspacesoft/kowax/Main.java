package com.xspacesoft.kowax;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import com.xspacesoft.kowax.kernel.Stdio;

public class Main {
	/** Default system input */
	private final static InputStream DEFAULT_SYSTEM_IN = System.in;
	private final static PrintStream DEFAULT_SYSTEM_OUT = System.out;
	private final static String[] TITLE = {
		"         ___  __    __   _   __  __",
		"  /\\ /\\ /___\\/ / /\\ \\ \\ /_\\  \\ \\/ /",
		" / //_///  //\\ \\/  \\/ ///_\\\\  \\  / ",
		"/ __ \\/ \\_//  \\  /\\  //  _  \\ /  \\ ",
		"\\/  \\/\\___/    \\/  \\/ \\_/ \\_//_/\\_\\",
	};

	public static void main(String[] args) {
		Preferences pref = Preferences.userRoot().node(Initrfs.class.getName());
//		pref.putBoolean("configured", false);
		PrintWriter out = new PrintWriter(DEFAULT_SYSTEM_OUT, true);
//		out.flush();
		OptionsParser ap = new OptionsParser(args);
		if(ap.getTag("h")||ap.getTag("help")) {
//			Initrfs.printHelp();
			System.exit(0);
		}
		if(!BuildGet.stringToBoolean(BuildGet.getString("default.force.debug"))) {
			Initrfs.clear(DEFAULT_SYSTEM_OUT);
			System.out.println();
		}
		for (String String : TITLE) {
			System.out.println(String);
		}
		try {
			Thread.sleep(1000);
			int proc = Runtime.getRuntime().availableProcessors();
			System.out.println();
			for (int i = 0; i < proc; i++) {
				System.out.print("K ");
				Thread.sleep(150);
			}
			System.out.println();
			if(Stdio.isNumber(Initrfs.VERSION.charAt(0)))
				printScroll(out, "Welcome to " + Initrfs.SHELLNAME + " Version " + Initrfs.VERSION + "!" + 
			(Initrfs.BUILD.equals("NA") ? "" : (" (" + Initrfs.BUILD.substring(0,8) + "...)" )) , 20);
			else
				printScroll(out, "Welcome to " + Initrfs.SHELLNAME + " \"" + Initrfs.VERSION + "\" release!" + 
						(Initrfs.BUILD.equals("NA") ? "" : (" (" + Initrfs.BUILD.substring(0, 8) + "...)" )) , 20);
			Thread.sleep(600);
			System.out.println("----------------");
			Thread.sleep(10);
			
			// Do configuration wizard if first launch
			if(!pref.getBoolean("configured", false)) {
				SetupWizard setupWizard = new SetupWizard(pref, out, DEFAULT_SYSTEM_IN);
				setupWizard.start();
				pref.putBoolean("configured", true);
				String a;
				out.println();
				out.println(a = "Starting up!");
				for(int i=0;i<a.length();i++) {
					out.printf("-");
				}
			}
			
			int telnet = pref.getInt("telnet_port", Stdio.parseInt(BuildGet.getString("default.telnet")));
			if(ap.getArgument("telnet")!=null) {
				if(Stdio.isNumber(ap.getArgument("telnet"))) {
					telnet = Stdio.parseInt(ap.getArgument("telnet"));
				}
			}
			int http = pref.getInt("http_port", Stdio.parseInt(BuildGet.getString("default.http")));
			if(ap.getArgument("httpport")!=null) {
				if(Stdio.isNumber(ap.getArgument("httpport"))) {
					http = Stdio.parseInt(ap.getArgument("httpport"));
				}
			}
			boolean debug = pref.getBoolean("force_debug", BuildGet.stringToBoolean(BuildGet.getString("default.force.debug")));
			if(ap.getTag("debug")) {
				debug = true;
			}
			boolean verbose = pref.getBoolean("force_verbose", BuildGet.stringToBoolean(BuildGet.getString("default.force.verbose")));
			if(ap.getTag("verbose")) {
				verbose = true;
			}
			String home = pref.get("home_path", new File("").getAbsolutePath());
			Initrfs init = new Initrfs(home, telnet, http, debug, verbose, DEFAULT_SYSTEM_IN, DEFAULT_SYSTEM_OUT);
			out.flush();
			init.start();
		} catch (InterruptedException e) {
			
		}
	}
	
	public static void printScroll(PrintWriter out, String string, int pause) throws InterruptedException {
		char[] output = string.toCharArray();
		out.flush();
		for (int i = 0; i < output.length; i++) {
			System.out.print(output[i]);
			Thread.sleep(pause);
		}
		out.flush();
		out.println();
	}
}
