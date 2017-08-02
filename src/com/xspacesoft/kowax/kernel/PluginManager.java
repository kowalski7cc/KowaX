package com.xspacesoft.kowax.kernel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.Logwolf;
import com.xspacesoft.kowax.apis.PrivilegedAcces;
import com.xspacesoft.kowax.apis.Service;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.exceptions.DuplicateElementException;
import com.xspacesoft.kowax.shell.CommandRunner;

public final class PluginManager {

	private List<Class<? extends PluginBase>> pluginClass;
	private List<PluginBase> enabledPlugins;
	private List<Service> loadedServices;
	private List<PluginBase> privilegedPlugins;
	private TaskManager taskManager;
	private Logwolf logwolf;

	public PluginManager(TokenKey tokenKey) {
		pluginClass = new LinkedList<Class<? extends PluginBase>>();
		enabledPlugins = new LinkedList<PluginBase>();
		loadedServices = new LinkedList<Service>();
		privilegedPlugins = new LinkedList<PluginBase>();
		taskManager = (TaskManager) Core.getSystemApi(SystemApi.TASK_MANAGER, tokenKey);
		logwolf = Core.getLogwolf();
	}

	public void loadPluginFromClassFile(File file, Boolean startService, TokenKey tokenKey) throws ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException, IOException {
		URL[] urls = null;
		URL url = file.toURI().toURL();
		urls = new URL[] { url };
		URLClassLoader ucl = new URLClassLoader(urls);
		Class<? extends PluginBase> obj = ucl.loadClass(file.getName()).asSubclass(PluginBase.class);
		if(obj.isInstance(PluginBase.class)) {
			loadPlugin(obj, startService, tokenKey);
		}
		ucl.close();
	}

	public void loadPlugin(Class<? extends PluginBase> loadPlugin, Boolean startService, TokenKey tokenKey)
			throws InstantiationException, IllegalAccessException {
		Core.getLogwolf().d("[PluginManager] - Trying to load class " + loadPlugin.getName() +".");
		PluginBase newPlugin = loadPlugin.newInstance();
		String pluginName = loadPlugin.getSimpleName();
		
		// Check if has valid plug-in name
		if(newPlugin.getAppletName() == null) {
			logwolf.e("[PluginManager] - Invalid shell name in plugin " + pluginName + ". Stopped loadup.");
			return;
		}
		
		// Check duplicates
		for (PluginBase shellPlugin : enabledPlugins) {
			if(shellPlugin.getAppletName().equals(newPlugin.getAppletName())) {
				throw new DuplicateElementException(newPlugin.getAppletName());
			}
		}
		enabledPlugins.add(newPlugin);
		
		// Give permissions
		if(newPlugin instanceof PrivilegedAcces) {
			if(tokenKey!=null) {
				if(Core.isTokenValid(tokenKey)) {
					PrivilegedAcces kernelAccess = (PrivilegedAcces) newPlugin;
					try {
						kernelAccess.setTokenKey(tokenKey);
						logwolf.d("[PluginManager] - Plugin " + pluginName + " loaded with privileged access");
						privilegedPlugins.add(newPlugin);
					} catch (Exception e) {
						logwolf.e("[PluginManager] - Plugin " + pluginName + " failed to load with privileged access");
						logwolf.e("[PluginManager] - " + e.toString());
					}
				} else {
					logwolf.e("[PluginManager] - Invalid TokenKey");
				}
			}
		} else {
			logwolf.d("[PluginManager] - " + pluginName + " doesn't support load with privileged access.");
		}
		
		// Start service
		if(newPlugin instanceof Service) {
			Service service = (Service) newPlugin;
			if(service.getServiceName()!=null) {
				if((startService!=null)&&startService) {
					try {
						service.startService();
						taskManager.newTask("root", service.getServiceName(), service);
					} catch (Exception e) {
						Core.getLogwolf().e("[PluginManager] - Can't start service" + service.getServiceName());
						Core.getLogwolf().e("[PluginManager] - " + e.toString());
					}
				}
				loadedServices.add(service);
			} else {
				Core.getLogwolf().e("[PluginManager] - Can't start " + pluginName + ", invalid service name.");
			}
		} else {
			logwolf.d("[PluginManager] - " + pluginName + " doesn't support services");
		}
		logwolf.i("[PluginManager] - Plugin " + pluginName + " load complete");
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

	public List<PluginBase> getPlugins() {
		return enabledPlugins;
	}

	public void stopServices() {
		for(PluginBase shellPlugin : enabledPlugins) {
			try {
				Service service = (Service) shellPlugin;
				service.stopService();
				Core.getLogwolf().d("Service " + service + " stopped");
			} catch (Exception e){
				// Plugin has not a service
			}
		}
	}

	public void sendSystemEvent(SystemEvent event, String extraValue, CommandRunner commandRunner, List<Class<? extends PluginBase>> blacklist) {
		for (PluginBase thisPlugin : enabledPlugins) {
			try {
				SystemEventsListener listeners = (SystemEventsListener) thisPlugin;
				if (new ArrayList<SystemEvent>(Arrays.asList(listeners.getEvents())).contains(event))
					listeners.runIntent(event, extraValue, commandRunner);
			} catch (Exception e) { }
		}
	}
	
	public void sendSystemEvent(SystemEvent event, String extraValue, TokenKey tokenKey) {
		sendSystemEvent(event, extraValue, tokenKey, null);
	}

	public void sendSystemEvent(SystemEvent event, String extraValue, TokenKey tokenKey , List<Class<? extends PluginBase>> blacklist) {
		CommandRunner commandRunner = new CommandRunner(tokenKey, false);
		sendSystemEvent(event, extraValue, commandRunner, blacklist);
	}

	public List<Service> getServices() {
		return loadedServices;
	}
	
	public List<PluginBase> getPrivilegedPlugins() {
		return privilegedPlugins;
	}

}
