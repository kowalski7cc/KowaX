package com.xspacesoft.kowax.shell;

import java.io.IOException;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.exceptions.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.SystemApi;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager;
import com.xspacesoft.kowax.kernel.UsersManager.InvalidUserException;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner.CommandNotFoundException;

public class Konsole {
	
	//private ConsoleIO consoleIO;
	private Stdio stdio;
	private Session session;
	private CommandRunner commandRunner;
	private TokenKey tokenKey;
	private UsersManager usersManager;

	public Konsole(TokenKey tokenKey, Stdio stdio) throws IOException {
		this.stdio = stdio;
		session = new Session(stdio);
		this.tokenKey = tokenKey;
		usersManager = (UsersManager) Core.getSystemApi(SystemApi.USERS_MANAGER, tokenKey);
	}
	
	public void start() {
		
		stdio.println("KowaX Shell");
		stdio.println("---------------");
		stdio.println();
		while(true) {
			stdio.print("Username: ");
			String username = stdio.readString();
			if(stdio.getInputReader() instanceof KonsoleIO)
				if(!((KonsoleIO)stdio.getInputReader()).isConsoleAvailable()) {
					stdio.println("Warning, password will be not hidden.");
					stdio.readString();
				}
					
			stdio.print("Password: ");
			String password = (stdio.getInputReader() instanceof KonsoleIO)?
					((KonsoleIO)stdio.getInputReader()).readPassword():stdio.readString();
			stdio.println();
			
			if(isUserValid(username, password)) {
				session.setAuthenticated(true);
				session.setUsername(username);
				session.setSessionActive(true);
				stdio.println("Welcome back, " + session.getUsername() + "!");
				commandRunner = new CommandRunner(session, tokenKey, false);
				commandRunner.sendSystemEvent(SystemEvent.USER_LOGIN_SUCCESS, session.getUsername(), tokenKey, false);
				while(session.isSessionActive()) {
					if(session.isSudo())
						stdio.print("root@kowax:-# ");
					else
						stdio.print(session.getUsername() + "@kowax:-$ ");
					String userInput = stdio.readString();
					try {
						commandRunner.run(userInput);
					} catch (CommandNotFoundException e) {
						stdio.println("-shell: Command not found.");
						stdio.println();
					} catch (MissingPluginCodeException e) {
						stdio.println("-shell: Error launching applet: " + e.toString());
						stdio.println();
					} catch (IllegalArgumentException e) {
						// New line
					} catch (Exception e) {
						stdio.println("-shell: Error launching applet: " + e.toString());
					}
				}
			} else {
				stdio.println("Invalid username or password.");
			}

		}
	}
	
	public boolean isUserValid(String username, String password) {
		if(usersManager.existsUser(username))
			try {
				if(usersManager.isPasswordValid(username, password))
					return true;
			} catch (InvalidUserException e) {
				return false;
			}
		return false;
	}

}
