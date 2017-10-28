package com.xspacesoft.kowax.engine.shell;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.engine.AliasManager;
import com.xspacesoft.kowax.engine.PluginBase;
import com.xspacesoft.kowax.engine.PluginManager;
import com.xspacesoft.kowax.engine.SystemApi;
import com.xspacesoft.kowax.engine.SystemEvent;
import com.xspacesoft.kowax.engine.TaskManager;
import com.xspacesoft.kowax.engine.TokenKey;
import com.xspacesoft.kowax.engine.UsersManager;
import com.xspacesoft.kowax.engine.UsersManager.InvalidUserException;
import com.xspacesoft.kowax.engine.io.Stdio;
import com.xspacesoft.kowax.exceptions.InsufficientPermissionsException;
import com.xspacesoft.kowax.exceptions.MissingPluginCodeException;

/**
 * The Class CommandRunner handles the running of an applet.
 */
public final class CommandRunner {
	
//	private List<ShellPlugin> plugins;
	private AliasManager aliasManager;
	private PluginManager pluginmanager;
	private TaskManager taskmanager;
	private UsersManager usersManager;
	private TokenKey tokenKey;
	private Session session;

	public static class CommandNotFoundException extends Exception {

		private static final long serialVersionUID = 5514977449107721601L;
		
		public CommandNotFoundException(String command) {
			super(command);
		}		
		
	}
	
	/**
	 * Instantiates a new command runner.
	 * @param session User session
	 * @param tokenKey token
	 * @param sudo Launch command runner as sudo
	 */
	public CommandRunner(Session session, TokenKey tokenKey, boolean sudo) {
		this.session = session;
		this.tokenKey = tokenKey;
		if(!Core.isTokenValid(tokenKey))
			throw new TokenKey.InvalidTokenException();
		this.usersManager = (UsersManager) Core.getSystemApi(SystemApi.USERS_MANAGER, tokenKey);
		this.pluginmanager = (PluginManager) Core.getSystemApi(SystemApi.PLUGIN_MANAGER, tokenKey);
		this.taskmanager = (TaskManager) Core.getSystemApi(SystemApi.TASK_MANAGER, tokenKey);
		this.aliasManager = (AliasManager) Core.getSystemApi(SystemApi.ALIAS_MANAGER, tokenKey);
		this.session.setSudo(sudo);
	}
	
	public CommandRunner(TokenKey tokenKey, boolean sudo) {
//		this.tokenKey = tokenKey;
//		if(!Core.isTokenValid(tokenKey))
//			throw new TokenKey.InvalidTokenException();
//		this.usersManager = Core.getUsersManager(tokenKey);
//		this.pluginmanager = Core.getPluginManager(tokenKey);
//		this.taskmanager = Core.getTaskManager(tokenKey);
//		this.aliasManager = Core.getAliasManager(tokenKey);
		Stdio stdio = new Stdio();
		Session session = new Session(stdio);
		session.setSudo(sudo);
		new CommandRunner(session, tokenKey, sudo);
	}

	/**
	 * Runs the applet with commands
	 *
	 * @param userInput the command
	 * @throws CommandNotFoundException When command is not found
	 * @throws MissingPluginCodeException When plugin is missing code
	 */
	public void run(String userInput)
			throws CommandNotFoundException, MissingPluginCodeException, IllegalArgumentException {
		if(userInput==null){
			session.setSessionActive(false);
			return;
		}
		userInput = puryfyInput(userInput);
		if(userInput.equals("")) {
			return;
		}
		
		List<String> command = parseInput(userInput);
		while (aliasManager.getCommandFromAlias(command.get(0))!=null) {
			command.set(0, aliasManager.getCommandFromAlias(command.get(0)));
		}
		if(command.get(0).equalsIgnoreCase("exit")) {
			if(session.isSudo()) {
				session.setSudo(false);
				return;
			} else {
				session.setSessionActive(false);
				return;
			}
		}
		for (PluginBase plugin : pluginmanager.getPlugins()) {
			if (plugin.getAppletName().equalsIgnoreCase(command.get(0))) {
				if(command.size()>1)
					startProcess(plugin, command.subList(1, command.size()).toArray(new String[0]));
				else
					startProcess(plugin, null);
				System.gc();
				return;
			}
		}
		throw new CommandNotFoundException(userInput);
	}

	private List<String> parseInput(String userInput) {
		Matcher matcher = Pattern.compile("(\"[^\"]+\")|([^\\s\"]+)").matcher(userInput);
		List<String> command = new LinkedList<String>();
		while(matcher.find())
			command.add(matcher.group(1)!=null?matcher.group(1).replaceAll("\"", ""):matcher.group(2));
		return command;
	}

	private String puryfyInput(String command) {
		while(command.startsWith(" ")) {
			command = command.substring(1);
		}
		while(command.endsWith(" ")) {
			command = command.substring(0, command.length()-1);
		}
		return command;
	}

	/**
	 * Start process.
	 *
	 * @param plugin plugin to run
	 * @param strings the command
	 * @throws MissingPluginCodeException When plugin is missing code
	 */
	private void startProcess(PluginBase plugin, String[] strings) throws MissingPluginCodeException {
		int pid = 0;
		String pName;
		if(strings==null||strings.equals("")) {
			pName = plugin.getAppletName();
		} else {
			pName = plugin.getAppletName() + " " + strings[0];
		}
		pName = pName.substring(0, 1).toUpperCase() + pName.substring(1);
		pid = taskmanager.newTask(session.isSudo() ? "root" : session.getUsername(), pName);
		try {
			plugin.start(strings, session.getStdio(), this);
		} catch (Exception e) {
			session.getStdio().println("Sorry, " + plugin.getAppletName() + " stopped working:");
			session.getStdio().println(e.toString());
		}
		taskmanager.removeTask(pid);
	}

	public void runExternalClass(String path, String[] command) {
		if (!session.isSudo()) {
			session.getStdio().println("command runner: Operation not permitted");
			return;
		}
		try {
			PluginBase obj = (PluginBase) ClassLoader.getSystemClassLoader().loadClass(path).newInstance();
			obj.start(command, session.getStdio(), this);
		} catch (InstantiationException e) {
			session.getStdio().println("Failed to load applet: " + e.toString());
		} catch (IllegalAccessException e) {
			session.getStdio().println("Failed to load applet: " + e.toString());
		} catch (ClassNotFoundException e) {
			session.getStdio().println("Failed to load applet: " + e.toString());
		} catch (MissingPluginCodeException e) {
			session.getStdio().println("Failed to load applet: " + e.toString());
		} catch (Exception e) {
			session.getStdio().println("Unhandled exception in applet: " + e.toString());
		}
	}
	
	public boolean isSudo() {
		return session.isSudo();
	}
	
	public void sudo() {
		runSudo(null);
	}
	
	private void runSudo(String command) {
		Stdio stdio = session.getStdio();
		if(!session.isSudo()) {
			if(session.isSudoExpired()) {
				stdio.print("[sudo] password for " + session.getUsername() +": ");
				String response = session.getStdio().readString();
				if (response.equalsIgnoreCase(""))
					return;
				try {
					if(usersManager.isPasswordValid(session.getUsername(), response)) {
						session.setSudo(true);
					} else {
						stdio.println("sudo: incorrect password");
						return;
					}
				} catch (InvalidUserException e) {
					// Will never reach this part
					e.printStackTrace();
				}
			} else {
				session.setSudo(true);
			}
		}
		if(command == null) {
			// Remain sudo
			return;
		} else if(command.equalsIgnoreCase("-s")) {
			// Remain sudo
			return;
		} else {
			try {
				run(command);
			} catch (CommandNotFoundException e) {
				stdio.println("-shell: Command not found.");
				stdio.println();
			} catch (MissingPluginCodeException e) {
				stdio.println("-shell: Error launching applet: " + e.toString());
				stdio.println();
			} catch (IllegalArgumentException e) {
				// New line
			}
			session.setSudo(false);
		}
	}
	
	public void sudo(String command) {
		runSudo(command);
	}	
	
	public TokenKey getTokenKey() {
		if (isSudo())
			return tokenKey;
		return null;
	}
	
	public void buildSystemProcess(String command) throws IOException {
		Runtime.getRuntime().exec(command);
	}
	
	public String getUsername() {
		return session.getUsername();
	}
	
	public Session getSession(TokenKey tokenKey) {
		if(Core.isTokenValid(tokenKey))
			return session;
		return null;
	}
	
	public Stdio getSocketHelper() {
		return session.getStdio();
	}
	
	public void sendSystemEvent(SystemEvent event, String extraValue, TokenKey tokenKey, boolean sudo) {
		if(!Core.isTokenValid(tokenKey))
			throw new InsufficientPermissionsException();
		pluginmanager.sendSystemEvent(event, extraValue, this, null);
		
	}

	public void sudo(String[] array) {
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.asList(array).forEach(s -> stringBuilder.append(s + " "));
		sudo(stringBuilder.toString());
	}
	
	
}
