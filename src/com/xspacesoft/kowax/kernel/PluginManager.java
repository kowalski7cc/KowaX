package com.xspacesoft.kowax.kernel;

import java.util.ArrayList;
import java.util.List;

import com.xspacesoft.kowax.Initrfs;

public class PluginManager {

	private List<ShellPlugin> enabledPlugins;
	
	public PluginManager() {
		enabledPlugins = new ArrayList<ShellPlugin>();
	}
	
	public void addPlugin(Class<? extends ShellPlugin> loadPlugin)
			throws InstantiationException, IllegalAccessException {
		addPlugin(loadPlugin, null);
	}

	public void addPlugin(Class<? extends ShellPlugin> loadPlugin, TokenKey tokenKey)
			throws InstantiationException, IllegalAccessException {
		ShellPlugin newPlugin = loadPlugin.newInstance();
		for (ShellPlugin shellPlugin : enabledPlugins) {
			if(shellPlugin.getAppletName().equals(newPlugin.getAppletName())) {
				throw new DuplicateElementException(newPlugin.getAppletName());
			}
		}
		if((tokenKey!=null)&&Initrfs.isTokenValid(tokenKey)) {
			try {
				KernelAccess sup = (KernelAccess) newPlugin;
				sup.setTokenKey(tokenKey);
			} catch (Exception e) { }
		}
		enabledPlugins.add(newPlugin);
	}
	
	public String[] getPluginsName() {
		String[] plugins = new String[enabledPlugins.size()];
		for (int i = 0; i < plugins.length; i++) {
			plugins[i] = enabledPlugins.get(i).getAppletName();
		}
		return plugins;
	}
	
	public int getPluginsNumber() {
		return enabledPlugins.size();
	}
	
	public List<ShellPlugin> getPlugins() {
		return enabledPlugins;
	}

}
