package com.xspacesoft.kowax;


import java.util.ArrayList;
import java.util.List;

/**
 * Argument parser class.
 *
 * @author kowalski7cc
 * @version 1.2
 */
public class OptionsParser {

	/** Arguments by user. */
	private List<String[]> arguments;
	
	/**
	 * Instantiates a new argument parser.
	 * Takes arguments and splits them in a smart way
	 * @param args user arguments.
	 */
	public OptionsParser(String[] args) {
		arguments = new ArrayList<String[]>();
		for(int i = 0; i < args.length; i++) {
			String[] arg = new String[2];
			// Memorize tag
			arg[0] = args[i].substring(1).toLowerCase();
			if(i+1<args.length) {
				if(!args[i+1].startsWith("-")) {
					if(args[i].startsWith("-")) {
						// Memorize argument for tag
						arg[1] = args[++i];
					}
				}
			}
			arguments.add(arg);
		}
	}

	/**
	 * Gets the argument given a tag.
	 *
	 * @param tag The tag to find an argument option
	 * @return the option
	 */
	public String getArgument(String tag) {
		for(String[] arg : arguments) {
			if(arg[0].equals(tag)) {
				return arg[1];
			}
		}
		return null;
	}
	
	/**
	 * Checks if a is switch present.
	 *
	 * @param tag Tag name
	 * @return true, if is switch present
	 */
	public boolean getTag(String tag) {
		for(String[] arg : arguments) {
			if(arg[0].equals(tag)) {
				return true;
			}
		}
		return false;
	}
}
