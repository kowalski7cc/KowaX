package com.xspacesoft.kowax.kernel;

import java.util.ArrayList;
import java.util.List;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.Logwolf;

public class PluginManager {

	private List<ShellPlugin> enabledPlugins;
	private TaskManager taskManager;
	private Logwolf logwolf;
	
	public PluginManager(TokenKey tokenKey) {
		enabledPlugins = new ArrayList<ShellPlugin>();
		taskManager = Initrfs.getTaskManager(tokenKey);
		logwolf = Initrfs.getLogwolf();
	}
	
	public void addPlugin(Class<? extends ShellPlugin> loadPlugin)
			throws InstantiationException, IllegalAccessException {
		addPlugin(loadPlugin, null);
	}

	public void addPlugin(Class<? extends ShellPlugin> loadPlugin, TokenKey tokenKey)
			throws InstantiationException, IllegalAccessException {
		ShellPlugin newPlugin = loadPlugin.newInstance();
		String pluginName = loadPlugin.getSimpleName();
		if(newPlugin.getAppletName() == null) {
			logwolf.e("Invalid shell name in plugin " + pluginName);
			return;
		}
		for (ShellPlugin shellPlugin : enabledPlugins) {
			if(shellPlugin.getAppletName().equals(newPlugin.getAppletName())) {
				throw new DuplicateElementException(newPlugin.getAppletName());
			}
		}
		if((tokenKey!=null)&&Initrfs.isTokenValid(tokenKey)) {
			try {
				KernelAccess sup = (KernelAccess) newPlugin;
				sup.setTokenKey(tokenKey);
				logwolf.d("Plugin " + pluginName + " loaded at kernel level");
			} catch (Exception e) {
				// Can't load at kernel level
				logwolf.d("Can't load " + pluginName + " at kernel level.");
			}
		}
		try {
			Service service = (Service) newPlugin;
			service.startService();
			taskManager.newTask("root", service.getServiceName());
			Initrfs.getLogwolf().d("Started " + pluginName + " service @" + service.toString().split("@")[1]);
		} catch (Exception e) {
			// Can't load plugin as service
			logwolf.d("Plugin " + pluginName + " has not a service to start");
		}
		enabledPlugins.add(newPlugin);
		logwolf.d("Plugin " + pluginName + " load complete");
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

	public void stopServices() {
		for(ShellPlugin shellPlugin : enabledPlugins) {
			try {
				Service service = (Service) shellPlugin;
				service.stopService();
				Initrfs.getLogwolf().d("Service " + service + " stopped");
			} catch (Exception e){
				// Plugin has not a service
			}
		}
	}

}
