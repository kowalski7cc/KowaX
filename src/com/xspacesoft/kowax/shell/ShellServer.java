package com.xspacesoft.kowax.shell;

import java.io.IOException;
import java.net.Socket;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.exceptions.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.SystemApi;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager;
import com.xspacesoft.kowax.kernel.UsersManager.InvalidUserException;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner.CommandNotFoundException;

public class ShellServer extends Thread {

	private ShellIO sockethelper;
	private Stdio stdio;
	private TokenKey tokenKey;
	private UsersManager usersManager;
	private CommandRunner commandrunner;
	private TaskManager taskManager;
	private Session session;
	private int pid;

	public ShellServer(Socket socket, TokenKey tokenKey) throws IOException {
		sockethelper = new ShellIO(socket);
		stdio = new Stdio(sockethelper, sockethelper);
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
		
		pid = taskManager.newTask("unlogged", "Console (" + sockethelper.getRemoteAddress() + ")");
		Core.getLogwolf().i(sockethelper.getRemoteAddress() + " connected");
		stdio.printTitle("Kowax Shell");
		stdio.println();
		session = new Session(stdio);

		int attempts = 0;
		while(!session.isAuthenticated()) {
			sockethelper.print("Username: ");
			String username = stdio.scan();
			if(username==null) {
				try {
					sockethelper.getSocket().close();
				} catch (IOException e) { }
				return;
			}
			sockethelper.print("Password: ");
			String password = stdio.scan();
			if(password==null) {
				try {
					sockethelper.getSocket().close();
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
					sockethelper.getSocket().close();
				} catch (IOException e) { }
				return;
			}
		}


		// USER LOGGED IN!!!
		taskManager.getTask(pid).setUser(session.getUsername());
		stdio.clear();
		Core.getLogwolf().i(session.getUsername() + " logged in (" + sockethelper.getRemoteAddress() + ")");
		sockethelper.println("Welcome back, " + session.getUsername() + "!");
		commandrunner = new CommandRunner(session, tokenKey, false);
		// Send SystemEvent.USER_LOGIN to apps
		commandrunner.sendSystemEvent(SystemEvent.USER_LOGIN_SUCCESS, session.getUsername(), tokenKey, false);
		while(session.isSessionActive()) {
			if(!sockethelper.isOpen()){
				session.setSessionActive(false);
			} else {
				if(session.isSudo())
					stdio.print("root@kowax:-# ");
				else
					stdio.print(session.getUsername() + "@kowax:-$ ");
				String userInput = stdio.scan();
				if(userInput==null) {
					try {
						sockethelper.getSocket().close();
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
			sockethelper.getSocket().close();
		} catch (IOException e) { } finally {
			taskManager.removeTask(pid);
			commandrunner.sendSystemEvent(SystemEvent.USER_LOGOUT, session.getUsername(), tokenKey, false);
			Core.getLogwolf().i(session.getUsername() + " disconnected (" + sockethelper.getRemoteAddress() + ")");
		}
	}

}
