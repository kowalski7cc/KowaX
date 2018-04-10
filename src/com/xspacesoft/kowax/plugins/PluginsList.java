package com.xspacesoft.kowax.plugins;

import java.util.List;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.apis.PrivilegedAcces;
import com.xspacesoft.kowax.engine.PluginBase;
import com.xspacesoft.kowax.engine.PluginManager;
import com.xspacesoft.kowax.engine.TokenKey;
import com.xspacesoft.kowax.engine.io.Stdio;
import com.xspacesoft.kowax.engine.shell.CommandRunner;

public class PluginsList extends PluginBase implements PrivilegedAcces {

	private TokenKey tokenKey;

	@Override
	public String getAppletName() {
		return "Plugins";
	}

	@Override
	public String getAppletVersion() {
		return "1.0";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski7cc";
	}

	@Override
	protected void runApplet(String[] command, Stdio stdio, CommandRunner commandRunner) {
		@SuppressWarnings("deprecation")
		PluginManager pluginManager = Core.getPluginManager(tokenKey);
		List<PluginBase> plugins = pluginManager.getPlugins();
		StringBuilder stringBuilder = new StringBuilder();
		plugins.forEach(p -> stringBuilder.append(p.getAppletName() + ", "));
		stdio.println(stringBuilder.substring(0, stringBuilder.length()>2?stringBuilder.length()-2:stringBuilder.length()));
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

	/* (non-Javadoc)
	 * @see com.xspacesoft.kowax.apis.PrivilegedAcces#setTokenKey(com.xspacesoft.kowax.engine.TokenKey)
	 */
	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}
}
