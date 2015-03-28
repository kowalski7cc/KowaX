package com.xspacesoft.kowax.shell;

import java.io.IOException;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.kernel.AliasManager;
import com.xspacesoft.kowax.kernel.DuplicateElementException;
import com.xspacesoft.kowax.kernel.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager;
import com.xspacesoft.kowax.kernel.UsersManager.InvalidUserException;

/**
 * The Class CommandRunner handles the running of an applet.
 */
public class CommandRunner {
	
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
	 *
	 * @param applets the applets
	 */
	public CommandRunner(Session session, TokenKey tokenKey, boolean sudo) {
		this.session = session;
		this.tokenKey = tokenKey;
		if(!Initrfs.isTokenValid(tokenKey))
			throw new TokenKey.InvalidTokenException();
		this.usersManager = Initrfs.getUsersManager(tokenKey);
		this.pluginmanager = Initrfs.getPluginManager(tokenKey);
		this.taskmanager = Initrfs.getTaskManager(tokenKey);
		this.aliasManager = Initrfs.getAliasManager(tokenKey);
		this.session.setSudo(sudo);
	}
	
	public CommandRunner(TokenKey tokenKey, boolean sudo) {
		this.tokenKey = tokenKey;
		if(!Initrfs.isTokenValid(tokenKey))
			throw new TokenKey.InvalidTokenException();
		this.usersManager = Initrfs.getUsersManager(tokenKey);
		this.pluginmanager = Initrfs.getPluginManager(tokenKey);
		this.taskmanager = Initrfs.getTaskManager(tokenKey);
		this.aliasManager = Initrfs.getAliasManager(tokenKey);
		Stdio stdio = new Stdio();
		Session session = new Session(stdio);
		this.session = session;
		this.session.setSudo(sudo);
	}

	/**
	 * Runs the applet with commands
	 *
	 * @param command the command
	 * @throws CommandNotFoundException 
	 * @throws MissingPluginCodeException 
	 */
	public void run(String command)
			throws CommandNotFoundException, MissingPluginCodeException, IllegalArgumentException {
		// Trim initial & Final spaces
		if(command==null){
			session.setSessionActive(false);
			return;
		}
		while(command.startsWith(" ")) {
			command.substring(1);
		}
		while(command.endsWith(" ")) {
			command.substring(0, command.length());
		}
		if(command.equals("")) {
			throw new IllegalArgumentException("Empty");
		}
		while (aliasManager.getCommandFromAlias(command)!=null) {
			command = aliasManager.getCommandFromAlias(command);
		}
		if(command.equalsIgnoreCase("exit")) {
			if(session.isSudo()) {
				session.setSudo(false);
				return;
			} else {
				session.setSessionActive(false);
				return;
			}
		}
		String[] userCommand = command.split(" ");
		for (ShellPlugin plugin : pluginmanager.getPlugins()) {
			if (plugin.getAppletName().equalsIgnoreCase(userCommand[0])) {
				if(userCommand.length>1)
					startProcess(plugin, command.substring(userCommand[0].length() +1));
				else
					startProcess(plugin, null);
				return;
			}
		}
		throw new CommandNotFoundException(command);
	}

	/**
	 * Start process.
	 *
	 * @param hiveApplet the hive applet
	 * @param command the command
	 * @throws MissingPluginCodeException 
	 */
	private void startProcess(ShellPlugin plugin, String command) throws MissingPluginCodeException {
		int pid = 0;
		String pName;
		if(command==null||command.equals("")) {
			pName = plugin.getAppletName();
		} else {
			pName = plugin.getAppletName() + " " + command.split(" ")[0];
		}
		pid = taskmanager.newTask("root", pName);
		plugin.start(command, session.getSockethelper(), this);
		taskmanager.removeTask(pid);
	}

	public void runExternalClass(String path, String command) {
		if (!session.isSudo()) {
			session.getSockethelper().println("command runner: Operation not permitted");
			return;
		}
		try {
			ShellPlugin obj = (ShellPlugin) ClassLoader.getSystemClassLoader().loadClass(path).newInstance();
			obj.start(command, session.getSockethelper(), this);
		} catch (InstantiationException e) {
			session.getSockethelper().println("Failed to load applet: " + e.toString());
		} catch (IllegalAccessException e) {
			session.getSockethelper().println("Failed to load applet: " + e.toString());
		} catch (ClassNotFoundException e) {
			session.getSockethelper().println("Failed to load applet: " + e.toString());
		} catch (MissingPluginCodeException e) {
			session.getSockethelper().println("Failed to load applet: " + e.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadExternalClass(String path) {
		if (!session.isSudo()) {
			session.getSockethelper().println("command runner: Operation not permitted");
			return;
		}
		try {
			pluginmanager.addPlugin((Class<? extends ShellPlugin>) ClassLoader.getSystemClassLoader().loadClass(path));
			session.getSockethelper().println("Applet " + ClassLoader.getSystemClassLoader().loadClass(path) + " loaded");
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			session.getSockethelper().println("Failed to load applet: " + e.toString());
		} catch (DuplicateElementException e) {
			session.getSockethelper().println("Plugin already loaded: " + e.toString());
		}
	}
	
	public boolean isSudo() {
		return session.isSudo();
	}
	
	public void sudo(String command) {
		Stdio stdio = session.getSockethelper();
		if(!session.isSudo()) {
			stdio.print("[sudo] password for " + session.getUsername() +": ");
			String response = session.getSockethelper().scan();
			if ((response.equalsIgnoreCase(""))||(response.equalsIgnoreCase("c")))
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
	
	public Stdio getSocketHelper() {
		return session.getSockethelper();
	}
}
