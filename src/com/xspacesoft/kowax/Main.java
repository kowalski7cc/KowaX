package com.xspacesoft.kowax;

import java.io.File;

import com.xspacesoft.kowax.ArgumentParser.ArgumentParserFactory;
import com.xspacesoft.kowax.kernel.io.Stdio;

public class Main {
	
	public static void main(String[] args) {
		ArgumentParserFactory parserFactory = new ArgumentParserFactory();
		parserFactory.addPairFilter("home");
		ArgumentParser argumentParser = parserFactory.buildArgumentParser(args);
		if(argumentParser.hasSwitch("-h")||argumentParser.hasSwitch("-help"))
			printHelp();

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
				printScroll("Welcome to " + Core.SHELLNAME + " Version " + Core.VERSION + "!" + 
			(Core.BUILD==null ? "" : (" (" + Core.BUILD.substring(0,8) + "...)" )) , 20);
			else
				printScroll("Welcome to " + Core.SHELLNAME + " \"" + Core.VERSION + "\" release!" + 
						(Core.BUILD==null ? "" : (" (" + Core.BUILD.substring(0, 8) + "...)" )) , 20);
			Thread.sleep(600);
			System.out.println("----------------");
			Thread.sleep(10);
			File home = argumentParser.hasPair("home")?new File(argumentParser.getPair("home")):findRootFS();
			Core init = new Core(home);
			init.start();
		} catch (InterruptedException e) {
			System.out.println("Unknown error in startup: " + e);
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
	
	public static void printScroll(String string, int pause) throws InterruptedException {
		for (int i = 0; i < string.length(); i++) {
			System.out.print(string.charAt(i));
			Thread.sleep(pause);
		}
		System.out.println();
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
