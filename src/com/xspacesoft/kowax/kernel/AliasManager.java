package com.xspacesoft.kowax.kernel;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AliasManager.
 * 
 * @author kowalski7cc
 * @version 1.2
 */
public class AliasManager {

	/**
	 * Class where is memorized a command with its alias.
	 */
	public class Alias {
		
		/** The command. */
		private String command;
		/** The alias. */
		private String alias;
		
		/**
		 * Instantiates a new alias.
		 *
		 * @param command the command
		 * @param alias the alias
		 */
		public Alias(String alias, String command) {
			this.command = command;
			this.alias = alias;
		}

		/**
		 * Gets the command.
		 *
		 * @return the command
		 */
		public String getCommand() {
			return command;
		}

		/**
		 * Gets the alias.
		 *
		 * @return the alias
		 */
		public String getAlias() {
			return alias;
		}
		
	}
	
	/** The aliases. */
	private List<Alias> aliases;
	
	/**
	 * Instantiates a new alias manager.
	 */
	public AliasManager() {
		aliases = new ArrayList<Alias>();
	}
	
	/**
	 * Load defaults aliases.
	 */
	public void loadDefaults() {
		aliases.add(new Alias("h", "system help"));
		aliases.add(new Alias("help", "system help"));
		aliases.add(new Alias("alias", "system alias"));
		aliases.add(new Alias("ps", "system ps"));
		aliases.add(new Alias("hwinfo", "system hwinfo"));
		aliases.add(new Alias("ls", "system ls"));
		aliases.add(new Alias("shutdown", "system shutdown"));
		aliases.add(new Alias("about", "system about"));
		aliases.add(new Alias("eula", "system eula"));
		aliases.add(new Alias("target", "system target"));
		aliases.add(new Alias("macro", "system macro"));
		aliases.add(new Alias("", "exit"));
		aliases.add(new Alias("sudo", "system sudo"));
		aliases.add(new Alias("whoami", "system whoami"));
		aliases.add(new Alias("version", "system version"));
		aliases.add(new Alias("clear", "system clear"));
		aliases.add(new Alias("echo", "system echo"));
	}

	/**
	 * New alias.
	 *
	 * @param command the command
	 * @param alias the alias
	 */
	public void newAlias(String alias, String command) {
		removeAlias(alias);
		aliases.add(new Alias(trimText(alias), trimText(command)));
	}
	
	/**
	 * Cleans up the string given by user.
	 * @param text String to clean
	 * @return trimmed text.
	 */
	private String trimText(String text) {
		while (text.startsWith(" ")) {
			text.substring(1);
		}
		while (text.endsWith(" ")) {
			text.substring(0, text.length()-1);
		}
		return text;
	}

	/**
	 * Removes an alias.
	 *
	 * @param alias the alias
	 */
	public void removeAlias(String alias) {
		for (Alias myAlias : aliases)
			if(myAlias.equals(alias))
				aliases.remove(myAlias);
	}
	
	public boolean existAlias(String alias) {
		for (Alias myAlias : aliases)
			if(myAlias.equals(alias))
				return true;
		return false;
	}
	
	/**
	 * Gets the command given a certain alias.
	 *
	 * @param userCommand the user command
	 * @return the command from alias
	 */
	public String getCommandFromAlias(String userCommand) {
		for (Alias myAlias : aliases) {
			if (userCommand.startsWith(myAlias.getAlias() + " ")) {
				return myAlias.getCommand() + userCommand.substring(myAlias.getAlias().length());
			} else if (userCommand.split(" ")[0].equalsIgnoreCase(myAlias.getAlias())) {
				return myAlias.getCommand() + userCommand.substring(myAlias.getAlias().length());
			}
		}
		return null;
	}
	
	/**
	 * Gets the number of loaded aliases
	 * 
	 * @return the number of aliases.
	 */
	public int getLoadedAliases() {
		return aliases.size();
	}
}
