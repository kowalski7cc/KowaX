package com.xspacesoft.kowax.shell;

import java.io.IOException;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.exceptions.DuplicateElementException;
import com.xspacesoft.kowax.exceptions.InsufficientPermissionsException;
import com.xspacesoft.kowax.exceptions.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.AliasManager;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.SystemApi;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager;
import com.xspacesoft.kowax.kernel.UsersManager.InvalidUserException;
import com.xspacesoft.kowax.kernel.io.Stdio;

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
	 * @param command the command
	 * @throws CommandNotFoundException When command is not found
	 * @throws MissingPluginCodeException When plugin is missing code
	 */
	public void run(String command)
			throws CommandNotFoundException, MissingPluginCodeException, IllegalArgumentException {
		// Trim initial & Final spaces
		if(command==null){
			session.setSessionActive(false);
			return;
		}
		while(command.startsWith(" ")) {
			command = command.substring(1);
		}
		while(command.endsWith(" ")) {
			command = command.substring(0, command.length()-1);
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
		for (PluginBase plugin : pluginmanager.getPlugins()) {
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
	 * @param plugin plugin to run
	 * @param command the command
	 * @throws MissingPluginCodeException When plugin is missing code
	 */
	private void startProcess(PluginBase plugin, String command) throws MissingPluginCodeException {
		int pid = 0;
		String pName;
		if(command==null||command.equals("")) {
			pName = plugin.getAppletName();
		} else {
			pName = plugin.getAppletName() + " " + command.split(" ")[0];
		}
		pName = pName.substring(0, 1).toUpperCase() + pName.substring(1);
		pid = taskmanager.newTask(session.isSudo() ? "root" : session.getUsername(), pName);
		plugin.start(command, session.getSockethelper(), this);
		taskmanager.removeTask(pid);
	}

	public void runExternalClass(String path, String command) {
		if (!session.isSudo()) {
			session.getSockethelper().println("command runner: Operation not permitted");
			return;
		}
		try {
			PluginBase obj = (PluginBase) ClassLoader.getSystemClassLoader().loadClass(path).newInstance();
			obj.start(command, session.getSockethelper(), this);
		} catch (InstantiationException e) {
			session.getSockethelper().println("Failed to load applet: " + e.toString());
		} catch (IllegalAccessException e) {
			session.getSockethelper().println("Failed to load applet: " + e.toString());
		} catch (ClassNotFoundException e) {
			session.getSockethelper().println("Failed to load applet: " + e.toString());
		} catch (MissingPluginCodeException e) {
			session.getSockethelper().println("Failed to load applet: " + e.toString());
		} catch (Exception e) {
			session.getSockethelper().println("Unhandled exception in applet: " + e.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadInternalClass(String path) {
		if (!session.isSudo()) {
			session.getSockethelper().println("command runner: Operation not permitted");
			return;
		}
		try {
			pluginmanager.loadPlugin((Class<? extends PluginBase>) ClassLoader.getSystemClassLoader().loadClass(path), null, null);
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
			if(session.isSudoExpired()) {
				stdio.print("[sudo] password for " + session.getUsername() +": ");
				String response = session.getSockethelper().readString();
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
		return session.getSockethelper();
	}
	
	public void sendSystemEvent(SystemEvent event, String extraValue, TokenKey tokenKey, boolean sudo) {
		if(!Core.isTokenValid(tokenKey))
			throw new InsufficientPermissionsException();
		pluginmanager.sendSystemEvent(event, extraValue, this, null);
		
	}
}
