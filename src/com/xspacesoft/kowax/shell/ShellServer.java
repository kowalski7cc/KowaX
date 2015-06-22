package com.xspacesoft.kowax.shell;

import java.io.IOException;
import java.net.Socket;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.exceptions.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.SystemApi;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager;
import com.xspacesoft.kowax.kernel.UsersManager.InvalidUserException;
import com.xspacesoft.kowax.shell.CommandRunner.CommandNotFoundException;

public class ShellServer extends Thread {
	
	private final static boolean LOCALHOST_FORCE_LOGIN_ADMIN = true;
	private final static String LOCALHOST_FORCE_LOGIN_USERNAME = "admin";
	private final static String LOCALHOST_FORCE_LOGIN_PASSWORD = "password";
	private Stdio sockethelper;
	private TokenKey tokenKey;
	private UsersManager usersManager;
	private CommandRunner commandrunner;
	private TaskManager taskManager;
	private Session session;
	private int pid;
	
	public ShellServer(Socket socket, TokenKey tokenKey) throws IOException {
		sockethelper = new Stdio(socket);
		this.tokenKey = tokenKey;
		this.setName("Console (" + (socket.getInetAddress().isLoopbackAddress() ?
				"Localhost" : socket.getInetAddress().getHostAddress()) + ")");
		if(!Core.isTokenValid(tokenKey))
			throw new TokenKey.InvalidTokenException();
		usersManager = (UsersManager) Core.getSystemApi(SystemApi.USERS_MANAGER, tokenKey);
		taskManager = (TaskManager) Core.getSystemApi(SystemApi.TASK_MANAGER, tokenKey);
	}
	
	@Override
	public void run() {
		pid = taskManager.newTask("root", "Console (" + sockethelper.getRemoteAddress() + ")");
		Core.getLogwolf().i(sockethelper.getRemoteAddress() + " connected");
		sockethelper.printTitle("Kowax Shell");
		sockethelper.println();
		session = new Session(sockethelper);
		if(LOCALHOST_FORCE_LOGIN_ADMIN && sockethelper.getSocket(tokenKey).getInetAddress().isLoopbackAddress()) {
			try {
				if(usersManager.isPasswordValid(LOCALHOST_FORCE_LOGIN_USERNAME, LOCALHOST_FORCE_LOGIN_PASSWORD)) {
					session.setAuthenticated(true);
					session.setUsername(LOCALHOST_FORCE_LOGIN_USERNAME);
					session.setSessionActive(true);
				} else {
					Core.getLogwolf().e("Wrong LOCALHOST_FORCE_LOGIN config");
				}
			} catch (InvalidUserException e) {
				Core.getLogwolf().e("Wrong LOCALHOST_FORCE_LOGIN config");
			}
		} else {
			int attempts = 0;
			while(!session.isAuthenticated()) {
				sockethelper.print("Username: ");
				String username = sockethelper.scan();
				if(username==null) {
					try {
						sockethelper.getSocket(tokenKey).close();
					} catch (IOException e) { }
					return;
				}
				sockethelper.print("Password: ");
				String password = sockethelper.scan();
				if(password==null) {
					try {
						sockethelper.getSocket(tokenKey).close();
					} catch (IOException e) { }
					return;
				}
				if (usersManager.existsUser(username)) {
					try {
						if(usersManager.isPasswordValid(username, password)) {
							session.setAuthenticated(true);
							session.setUsername(username);
							session.setSessionActive(true);
							break;
						}
					} catch (InvalidUserException e) { }
				}
				sockethelper.println("Invalid username or password. " + ++attempts + " of 3 attempts.");
				sockethelper.println();
				if(attempts>2) {
					try {
						sockethelper.getSocket(tokenKey).close();
					} catch (IOException e) { }
					return;
				}
			}
		}
		
		
		// USER LOGGED IN!!!
		taskManager.getTask(pid).setUser(session.getUsername());
		sockethelper.clear();
		Core.getLogwolf().i(session.getUsername() + " logged in");;
		sockethelper.println("Welcome back, " + session.getUsername() + "!");
		commandrunner = new CommandRunner(session, tokenKey, false);
		// Send SystemEvent.USER_LOGIN to apps
		commandrunner.sendSystemEvent(SystemEvent.USER_LOGIN_SUCCESS, session.getUsername(), tokenKey, false);
		while(session.isSessionActive()) {
			if(!session.getSockethelper().isOpen()){
				session.setSessionActive(false);
			} else {
				if(session.isSudo())
					sockethelper.print("root@kowax:-# ");
				else
					sockethelper.print(session.getUsername() + "@kowax:-$ ");
				String userInput = sockethelper.scan();
				if(userInput==null) {
					try {
						session.getSockethelper().getSocket(tokenKey).close();
					} catch (IOException e) { }
					session.setSessionActive(false);
				} else {
					try {
						commandrunner.run(userInput);
					} catch (CommandNotFoundException e) {
						sockethelper.println("-shell: Command not found.");
						sockethelper.println();
					} catch (MissingPluginCodeException e) {
						sockethelper.println("-shell: Error launching applet: " + e.toString());
						sockethelper.println();
					} catch (IllegalArgumentException e) {
						// New line
					} catch (Exception e) {
						sockethelper.println("-shell: Error launching applet: " + e.toString());
					}
				}
			}
		}
		
		// User disconnect
		try {
			sockethelper.getSocket(tokenKey).close();
		} catch (IOException e) { } finally {
			taskManager.removeTask(pid);
			commandrunner.sendSystemEvent(SystemEvent.USER_LOGOUT, session.getUsername(), tokenKey, false);
			Core.getLogwolf().i(sockethelper.getRemoteAddress() + " disconnected");
		}
	}

}
