package com.xspacesoft.kowax;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import com.xspacesoft.kowax.ArgumentParser.ArgumentParserFactory;
import com.xspacesoft.kowax.kernel.io.Stdio;

public class Main {
	/** Default system input */
	private final static InputStream DEFAULT_SYSTEM_IN = System.in;
	private final static PrintStream DEFAULT_SYSTEM_OUT = System.out;
	
	public static void main(String[] args) {
		ArgumentParserFactory parserFactory = new ArgumentParserFactory();
		parserFactory.addPairFilter("home");
		ArgumentParser argumentParser = parserFactory.buildArgumentParser(args);
		
		if(argumentParser.hasSwitch("-h")||argumentParser.hasSwitch("-help"))
			printHelp();
		
//		printHelp();
//		System.exit(1);;
		
//		OptionsParser ap = new OptionsParser(args);
//		Preferences pref = Preferences.userRoot().node(Core.class.getName());
		PrintWriter out = new PrintWriter(DEFAULT_SYSTEM_OUT, true);
//		out.flush();
//		if(ap.getTag("h")||ap.getTag("help")) {
////			Core.printHelp();
//			System.exit(0);
//		}
//		if(!BuildGet.stringToBoolean(BuildGet.getString("default.force.debug"))) {
//			Core.clear(DEFAULT_SYSTEM_OUT);
//			System.out.println();
//		}
		System.out.println(
				"-------------------\n"
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
//			SetupWizard setupWizard = null;
			int telnet = 21;
			int http = 8080;
			boolean debug = true;
			boolean verbose = true;
			File home = argumentParser.hasPair("home")?new File(argumentParser.getPair("home")):findRootFS();
			Core init = new Core(home, telnet, http, debug, verbose, DEFAULT_SYSTEM_IN, DEFAULT_SYSTEM_OUT);
			out.flush();
//			if(setupWizard!=null)
//				init.setNewUser(setupWizard.getUsername(), setupWizard.getPassword());
			init.start();
		} catch (InterruptedException e) {
			out.println("Unknown error in startup: " + e);
			System.exit(1);
		}
	}
	
	/**
	 * Search for KowaX RootFS.
	 * It can be either in
	 * 1) Same directory as KowaX executable
	 * 2) Host system user's home folder
	 * @return
	 */
	public static File findRootFS() {
		String rootName = "KowaX";
		File root;
		if((root = new File(rootName)).isDirectory())
			return root;
		if((root = new File(new File(System.getProperty("user.home")), "KowaX")).isDirectory())
			return root;
		return null;
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
	
	public static void printHelp() {
		String[] helpText = {
			Core.SHELLNAME + " help menu:",
			Core.BUILD==null ? "" : (" (" + Core.BUILD.substring(0,8) + "...)" ),
			"",
			"-h -help:\t\tShow help",
			"-root (path)\t\tProvide manually RootFS path",
		};
		
		for(String string : helpText) {
			System.out.println(string);
		}
		
		System.exit(0);
	}
}
