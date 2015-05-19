package com.xspacesoft.kowax;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

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
		PrintWriter out = new PrintWriter(DEFAULT_SYSTEM_OUT, true);
//		out.flush();
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
			Initrfs init = new Initrfs(port, debug, verbose, DEFALUT_SYSTEM_IN, DEFAULT_SYSTEM_OUT);
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
