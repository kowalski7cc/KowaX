package com.xspacesoft.kowax;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import com.xspacesoft.kowax.ArgumentParser.ArgumentParserFactory;

public class Main {

	private final static Properties properties = PomParser.load();

	public static void main(String[] args) {
		ArgumentParserFactory parserFactory = new ArgumentParserFactory();
		parserFactory.addPairFilter("home");
		ArgumentParser argumentParser = parserFactory.buildArgumentParser(args);
		if(argumentParser.hasSwitch("-h")||argumentParser.hasSwitch("-help"))
			printHelp();
		Core core = setupCore(argumentParser);
		core.setup();
		core.start();
	}

	private static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {	}
	}

	/**
	 * Search for KowaX root.
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
		if(!(root = new File(new File(System.getProperty("user.home")), "KowaX")).exists())
			root.mkdirs();
		return root;
	}

	public static void printScroll(String string, int pause) {
		for (int i = 0; i < string.length(); i++) {
			System.out.print(string.charAt(i));
			sleep(pause);
		}
		System.out.println();
	}

	public static void printHelp() {
		String[] helpText = { properties.getProperty("artifactId","KowaX") + " help menu:",
				properties.getProperty("version","test build"),
				"",
				"-h -help:\t\tShow help",
				"-root (path)\t\tProvide manually RootFS path",
		};
		Arrays.asList(helpText).forEach(System.out::println);
		System.exit(0);
	}


	public static Core setupCore(ArgumentParser argumentParser) {
		System.out.println("===================\n"
				+ "Starting KowaX\n"
				+ "===================\n");
		System.out.println();
		printScroll("Welcome to "
				+ properties.getProperty("artifactId","KowaX") + " "
				+ properties.getProperty("version","test build"), 10);
		sleep(100);
		File home = null;
		if(argumentParser!=null&&argumentParser.hasPair("home")) {
			home = new File(argumentParser.getPair("home"));
		} else {
			home = findRootFS();
		}
		return new Core(home);
	}
	
	
}
