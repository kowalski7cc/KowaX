package com.xspacesoft.kowax;

import java.io.InputStream;
import java.io.PrintStream;

import com.xspacesoft.kowax.kernel.Stdio;

public class Main {
	
	private final static int DEFAULT_PORT = 23;
	/** Forces debug output */
	private final static boolean DEFAULT_OUTPUT_DEBUG = true;
	/** Forces verbose output */
	private final static boolean DEFAULT_OUTPUT_VERBOSE = true;
	private final static InputStream DEFALUT_SYSTEM_IN = System.in;
	private final static PrintStream DEFAULT_SYSTEM_OUT = System.out;
	private final static String[] TITLE = {
		"         ___  __    __   _   __  __",
		"  /\\ /\\ /___\\/ / /\\ \\ \\ /_\\  \\ \\/ /",
		" / //_///  //\\ \\/  \\/ ///_\\\\  \\  / ",
		"/ __ \\/ \\_//  \\  /\\  //  _  \\ /  \\ ",
		"\\/  \\/\\___/    \\/  \\/ \\_/ \\_//_/\\_\\",
	};

	public static void main(String[] args) {
		OptionsParser ap = new OptionsParser(args);
		if(ap.getTag("h")||ap.getTag("help")) {
//			Initrfs.printHelp();
			System.exit(0);
		}
		int port = DEFAULT_PORT;
		if(ap.getArgument("port")!=null) {
			if(Stdio.isNumber(ap.getArgument("port"))) {
				port = Stdio.parseInt(ap.getArgument("port"));
			}
		}
		boolean debug = DEFAULT_OUTPUT_DEBUG;
		if(ap.getTag("debug")) {
			debug = true;
		}
		boolean verbose = DEFAULT_OUTPUT_VERBOSE;
		if(ap.getTag("verbose")) {
			verbose = true;
		}
		if(!debug) {
			Initrfs.clear(DEFAULT_SYSTEM_OUT);
			System.out.println();
		}
		for (String String : TITLE) {
			System.out.println(String);
		}
		int proc = Runtime.getRuntime().availableProcessors();
		System.out.println();
		for (int i = 0; i < proc; i++) {
			System.out.print("K ");
		}
		System.out.println();
		if(Stdio.isNumber(Initrfs.VERSION.charAt(0)))
			System.out.println("Welcome to " + Initrfs.SHELLNAME + " Version " + Initrfs.VERSION + "!");
		else
			System.out.println("Welcome to " + Initrfs.SHELLNAME + " \"" + Initrfs.VERSION + "\" release!");		
		System.out.println("----------------");
		Initrfs init = new Initrfs(port, debug, verbose, DEFALUT_SYSTEM_IN, DEFAULT_SYSTEM_OUT);
		init.start();
	}

}
