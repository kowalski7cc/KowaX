package com.xspacesoft.kowax.plugins;

import java.io.Serializable;
import java.util.List;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.apis.SystemEvent;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.exceptions.InsufficientPermissionsException;
import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.shell.CommandRunner;

public class KAuthenticator extends ShellPlugin implements KernelAccess, SystemEventsListener {

	private TokenKey tokenKey;
	
	private class UserConfig implements Serializable {
		private static final long serialVersionUID = -6383648922397913049L;
		private String username;
		private String salt;
	}
	
	private List<UserConfig> users;
	
	@Override
	public SystemEvent[] getEvents() {
		return new SystemEvent[] {SystemEvent.SYSTEM_START, SystemEvent.USER_LOGIN };
	}

	@Override
	public void runIntent(SystemEvent event, String extraValue, CommandRunner commandRunner) {
		Stdio stdio = commandRunner.getSocketHelper();
		switch(event) {
		case SYSTEM_START:
			if(!Initrfs.isTokenValid(tokenKey)) {
				Initrfs.getLogwolf().e("AUTHENTICATOR - GRAVE: Invalid TokenKey!!!");
			} else {
				Initrfs.getLogwolf().i("AUTHENTICATOR TokenKey OK, Loading config.");
			}
			loadSettings();
			break;
		case USER_LOGIN:
			if(!Initrfs.isTokenValid(tokenKey)) {
				stdio.println("Plugin not correctly configured, please contact system admin.");
				stdio.println(new InsufficientPermissionsException().toString());
				return;
			}
			authenticate(extraValue, commandRunner);
			break;
		default:
			break;
		
		}
	}

	private void authenticate(String extraValue, CommandRunner commandRunner) {
		Stdio stdio = commandRunner.getSocketHelper();
		stdio.print("AUTHENTICATOR Insert your user: ");
		String ans = stdio.scan();
		if(ans.equals(commandRunner.getUsername())) {
			stdio.println("OK");
		} else {
			stdio.println("You failed");
			commandRunner.getSession(tokenKey).setSessionActive(false);
		}
	}

	private void loadSettings() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getAppletName() {
		// Block plugin load
		return null; //"KAuthenticator";
	}

	@Override
	public String getAppletVersion() {
		return "1.0A";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		if(!Initrfs.isTokenValid(tokenKey)) {
			stdio.println("Plugin not correctly configured, please contact system admin.");
			stdio.println(new InsufficientPermissionsException().toString());
			return;
		}
		if(command == null) {
			stdio.println(getHint());
			return;
		}
		switch(command.split(" ")[0]) {
		case "newkey":
			setup(stdio);
			break;
		case "enable":
			
			break;
		case "disable":
			break;
		default:
			stdio.println(getHint());
		}
	}

	private void setup(Stdio stdio) {
		
	}

	@Override
	public String getDescription() {
		return "Log in using two-factor authentication";
	}

	@Override
	public String getHint() {
		return "Usage: KAuthenticator (newkey|enable|disable)";
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}

}
