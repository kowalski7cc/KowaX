package com.xspacesoft.kowax.windowsystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class HtmlDataLoader {
	
	public static void loadFile(File file) throws FileNotFoundException {
		loadFile(new FileInputStream(file));
	}

	public static String loadFile(InputStream input) {
		StringBuilder sb = new StringBuilder();
		Scanner scn = new Scanner(input);
		String line;
		while ((line = scn.nextLine()) != null) {
			sb.append(line);
		}
		scn.close();
		return sb.toString();
	}

}
