package com.xspacesoft.kowax.kernel;

import java.util.ArrayList;
import java.util.List;

import com.xspacesoft.kowax.Initrfs;

public class PluginManager {

	private List<ShellPlugin> enabledPlugins;
	private TaskManager taskManager;
	
	public PluginManager(TokenKey tokenKey) {
		enabledPlugins = new ArrayList<ShellPlugin>();
		taskManager = Initrfs.getTaskManager(tokenKey);
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
				Initrfs.getLogwolf().d(loadPlugin.toString() + " loaded at kernel level");
			} catch (Exception e) {
				// Can't load at kernel level
				Initrfs.getLogwolf().d("Can't load " + loadPlugin.toString() + " at kernel level.");
			}
		}
		try {
			Service service = (Service) newPlugin;
			service.startService();
			taskManager.newTask("root", service.getServiceName());
			Initrfs.getLogwolf().d("Started " + service.toString() + " service.");
		} catch (Exception e) {
			// Can't load plugin as service
			Initrfs.getLogwolf().d(loadPlugin.toString() + " has not a service to start");
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
