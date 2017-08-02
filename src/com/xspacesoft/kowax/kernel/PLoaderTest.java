package com.xspacesoft.kowax.kernel;

import java.io.File;
import java.io.IOException;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.SystemFolder;
import com.xspacesoft.kowax.apis.PrivilegedAcces;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class PLoaderTest extends PluginBase implements PrivilegedAcces {
	
	private TokenKey tokenKey;

	@Override
	public String getAppletName() {
		return "ploader";
	}

	@Override
	public String getAppletVersion() {
		return "1.0";
	}

	@Override
	public String getAppletAuthor() {
		return "kowalski7cc";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		PluginManager pluginManager = Core.getPluginManager(tokenKey);
		File bin = Core.getSystemFolder(SystemFolder.APPLICATIONS, null, tokenKey);
		File file = new File(bin, command); 
		try {
			pluginManager.loadPluginFromClassFile(file, false, tokenKey);
		} catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException
				| IOException e) {
			stdio.println(e.getMessage());
		}
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}

}
