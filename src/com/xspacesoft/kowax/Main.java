package com.xspacesoft.kowax;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import com.xspacesoft.kowax.kernel.io.Stdio;

public class Main {
	/** Default system input */
	private final static InputStream DEFAULT_SYSTEM_IN = System.in;
	private final static PrintStream DEFAULT_SYSTEM_OUT = System.out;;
	
	public static void main(String[] args) {
		OptionsParser ap = new OptionsParser(args);
		Preferences pref = Preferences.userRoot().node(Core.class.getName());
		PrintWriter out = new PrintWriter(DEFAULT_SYSTEM_OUT, true);
//		out.flush();
		if(ap.getTag("h")||ap.getTag("help")) {
//			Core.printHelp();
			System.exit(0);
		}
		if(!BuildGet.stringToBoolean(BuildGet.getString("default.force.debug"))) {
			Core.clear(DEFAULT_SYSTEM_OUT);
			System.out.println();
		}
		System.out.println("-------------------\n"
				+ "Starting KowaX\n"
				+ "-------------------");
		try {
			Thread.sleep(1000);
			int proc = Runtime.getRuntime().availableProcessors();
			System.out.println();
			for (int i = 0; i < proc; i++) {
				System.out.print("K ");
				Thread.sleep(150);
			}
			System.out.println();
			if(Stdio.isNumber(Core.VERSION.charAt(0)))
				printScroll(out, "Welcome to " + Core.SHELLNAME + " Version " + Core.VERSION + "!" + 
			(Core.BUILD==null ? "" : (" (" + Core.BUILD.substring(0,8) + "...)" )) , 20);
			else
				printScroll(out, "Welcome to " + Core.SHELLNAME + " \"" + Core.VERSION + "\" release!" + 
						(Core.BUILD==null ? "" : (" (" + Core.BUILD.substring(0, 8) + "...)" )) , 20);
			Thread.sleep(600);
			System.out.println("----------------");
			Thread.sleep(10);
			SetupWizard setupWizard = null;
			// Do configuration wizard if first launch
			if(!pref.getBoolean("configured", false)) {
				setupWizard = new SetupWizard(pref, out, DEFAULT_SYSTEM_IN);
				setupWizard.start();
				Core.sleep(1000);
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
			boolean debug = ap.getTag("debug") ? 
					true : pref.getBoolean("force_debug", BuildGet.stringToBoolean(BuildGet.getString("default.force.debug")));
			boolean verbose = ap.getTag("verbose") ? 
					true : pref.getBoolean("force_verbose", BuildGet.stringToBoolean(BuildGet.getString("default.force.verbose")));
			String home = pref.get("home_path", new File("").getAbsolutePath());

			Core init = new Core(home, telnet, http, debug, verbose, DEFAULT_SYSTEM_IN, DEFAULT_SYSTEM_OUT);
			out.flush();
			if(setupWizard!=null)
				init.setNewUser(setupWizard.getUsername(), setupWizard.getPassword());
			init.start();
		} catch (InterruptedException e) {
			out.println("Unknown error in startup: " + e);
			System.exit(1);
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
