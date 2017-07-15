package com.xspacesoft.kowax;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArgumentParser {
	
	public Map<String, String> pair;
	public List<String> switches;
	public List<String> arguments;
	public Map<String, String> regexText;

	private ArgumentParser() {
		pair = new HashMap<String, String>();
		switches = new LinkedList<String>();
		arguments = new LinkedList<String>();
		regexText = new HashMap<String, String>();
	}
	
	public boolean hasSwitch(String argument) {
		return switches.contains(argument.startsWith("-")?argument:"-"+argument);
	}
	
	public boolean hasPair(String argument) {
		return pair.containsKey(argument.startsWith("-")?argument:"-"+argument);
	}
	
	public String getPair(String argument) {
		return pair.get(argument.startsWith("-")?argument:"-"+argument);
	}
	
	public boolean hasRegex(String argument) {
		return regexText.containsKey(argument);
	}
	
	public String getRegex(String argument) {
		return regexText.get(argument);
	}
	
	public String[] getArguments() {
		return arguments.toArray(new String[arguments.size()]);
	}
	
	private void parse(String[] args, ArgumentParserFactory argumentParserFactory) {
		for(int i = 0; i < args.length; i++) {
			if(args[i].startsWith("-")) {
				if(argumentParserFactory.pairFilter.contains(args[i])) {
					if(i+1<args.length) {
						pair.put(args[i], args[i+1].startsWith("-")?"":args[(i++)+1]);
					}
				} else {
					switches.add(args[i].startsWith("-")?args[i]:"-"+args[i]);
				}
			} else {
				String arg = args[i];
				argumentParserFactory.textRegexFilter
					.stream().filter(arg::matches)
					.forEach(p -> regexText.put(p, arg));
				arguments.add(arg);
			}
		}
	}
	
	public static class ArgumentParserFactory {
		
		private List<String> pairFilter;
		private List<String> textRegexFilter;
		
		public ArgumentParserFactory() {
			pairFilter = new LinkedList<String>();
			textRegexFilter = new LinkedList<String>();
		}
		
		public ArgumentParser buildArgumentParser(String[] args) {
			ArgumentParser argumentParser = new ArgumentParser();
			argumentParser.parse(args, this);
			return argumentParser;
		}
		
		public void addPairFilter(String name) {
			pairFilter.add(name.startsWith("-")?name:"-"+name);
		}
		
		public void addTextRegexFilter(String regex) {
			textRegexFilter.add(regex);
		}
	}

}
