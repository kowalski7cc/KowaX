package com.xspacesoft.kowax.engine;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.Logwolf;
import com.xspacesoft.kowax.apis.PrivilegedAcces;
import com.xspacesoft.kowax.apis.Service;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.engine.shell.CommandRunner;

public final class PluginManager {

	private List<Class<? extends PluginBase>> availablePlugins;
	private List<PluginBase> enabledPlugins;
	private List<Service> loadedServices;
	private List<PluginBase> privilegedPlugins;
	private TaskManager taskManager;
	private Logwolf logwolf;
	private TokenKey tokenKey;

	public PluginManager(TokenKey tokenKey) {
		availablePlugins = new LinkedList<Class<? extends PluginBase>>();
		enabledPlugins = new LinkedList<PluginBase>();
		loadedServices = new LinkedList<Service>();
		privilegedPlugins = new LinkedList<PluginBase>();
		taskManager = (TaskManager) Core.getSystemApi(SystemApi.TASK_MANAGER, tokenKey);
		logwolf = Core.getLogwolf();
		this.tokenKey = tokenKey;
	}
	
	public boolean loadPlugin(Class<? extends PluginBase>[] plugins, boolean giveSystemPermissions, boolean startService) {
		boolean success = true;
		for (Class<? extends PluginBase> plugin : plugins) {
			try {
				loadPlugin(plugin, giveSystemPermissions, startService);
			} catch (Exception e) {
				logwolf.e("Failed to load \"" + plugin.getName() + "\": " + e.getClass().getSimpleName() + "(" + e.getMessage() + ")");
				success = false;
				e.printStackTrace();
			}
		}
		return success;
	}
	
	public boolean loadPluginFilesystem(File jar, String mainclass, boolean giveSystemPermissions, boolean startService)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		JarFile jarFile = new JarFile(jar);
		Enumeration<JarEntry> entries = jarFile.entries();
		List<JarEntry> classes = new LinkedList<JarEntry>();
		while(entries.hasMoreElements()) {
			JarEntry jarEntry = entries.nextElement();
			if(!jarEntry.isDirectory())
				if(jarEntry.getName().endsWith(".class")&&!jarEntry.getName().startsWith("bin/"))
					classes.add(jarEntry);
		}
		URL[] classpathURL = new URL[] {jar.toURI().toURL()};
		URLClassLoader classLoader = new URLClassLoader(classpathURL, Thread.currentThread().getContextClassLoader());
		for(JarEntry entry : classes) {
			String name = entry.getName().replaceAll("/", ".");
			classLoader.loadClass(name.substring(0, name.length()-6));
		}
		Class<?> c = classLoader.loadClass(mainclass);
		classLoader.close();
		jarFile.close();
		if(c.getSuperclass().equals(PluginBase.class))
			return loadPlugin(c.asSubclass(PluginBase.class), giveSystemPermissions, startService);
		return false;
	}

	public boolean loadPlugin(Class<? extends PluginBase> loadPlugin, boolean giveSystemPermissions, boolean startService)
			throws InstantiationException, IllegalAccessException {
		Core.getLogwolf().d("Trying to load class " + loadPlugin.getName() +".");
		PluginBase newPlugin = loadPlugin.newInstance();
		String pluginName = loadPlugin.getSimpleName();
		
		// Check if has valid plug-in name
		if(newPlugin.getAppletName() == null) {
			throw new IllegalArgumentException("Invalid shell name in plugin " + pluginName + ". Stopped loadup.");
		}
		
		// Check duplicates
		if(enabledPlugins.contains(newPlugin)) {
			return false;
		}

		enabledPlugins.add(newPlugin);
		
		// Give permissions
		if(giveSystemPermissions) {
			if(newPlugin instanceof PrivilegedAcces) {
				if(tokenKey!=null) {
					if(Core.isTokenValid(tokenKey)) {
						PrivilegedAcces kernelAccess = (PrivilegedAcces) newPlugin;
						try {
							kernelAccess.setTokenKey(tokenKey);
							logwolf.d("Plugin " + pluginName + " loaded with privileged access");
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
				logwolf.d(pluginName + " doesn't support load with privileged access.");
			}
		}
		
		// Load and start service
		if(newPlugin instanceof Service) {
			Service service = (Service) newPlugin;
			if(service.getServiceName()!=null) {
				if(startService) {
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
				Core.getLogwolf().e("Can't load " + pluginName + ", invalid service name.");
			}
		} else {
			logwolf.d(pluginName + " doesn't support services");
		}
		logwolf.i("Plugin " + pluginName + " load complete");
		return true;
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
		enabledPlugins.forEach(p -> {
			if(p instanceof SystemEventsListener)
				if(blacklist==null||!blacklist.contains(p.getClass()))
					if(Arrays.asList(((SystemEventsListener) p).getEvents()).contains(event))
						try {
							((SystemEventsListener) p).runIntent(event, extraValue, commandRunner);
						} catch (Exception e) {	}
		});
	}
	
	public void sendSystemEvent(SystemEvent event, String extraValue, CommandRunner commandRunner) {
		enabledPlugins.forEach(p -> {
			if(p instanceof SystemEventsListener)
				if(Arrays.asList(((SystemEventsListener) p).getEvents()).contains(event))
					try {
						((SystemEventsListener) p).runIntent(event, extraValue, commandRunner);
					} catch (Exception e) {	}
		});
	}

	public List<Service> getServices() {
		return loadedServices;
	}
	
	public List<PluginBase> getPrivilegedPlugins() {
		return privilegedPlugins;
	}

}
