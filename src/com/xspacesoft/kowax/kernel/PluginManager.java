package com.xspacesoft.kowax.kernel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.Logwolf;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.apis.Service;
import com.xspacesoft.kowax.apis.SystemEvent;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.exceptions.DuplicateElementException;
import com.xspacesoft.kowax.shell.CommandRunner;

public class PluginManager {

	private List<ShellPlugin> enabledPlugins;
	private TaskManager taskManager;
	private Logwolf logwolf;

	public PluginManager(TokenKey tokenKey) {
		enabledPlugins = new ArrayList<ShellPlugin>();
		taskManager = Initrfs.getTaskManager(tokenKey);
		logwolf = Initrfs.getLogwolf();
	}

	public boolean modprobe(File file) {
		URL[] urls = null;
		URL url;
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e1) {
			return false;
		}
		urls = new URL[] { url };
		URLClassLoader ucl = new URLClassLoader(urls);
		try {
			@SuppressWarnings("unchecked")
			Class<? extends ShellPlugin> load = (Class<? extends ShellPlugin>) ucl.loadClass(file.getName());
			ShellPlugin plugin = load.newInstance();
			if (plugin.getAppletName() == null)
				return false;
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			try {
				ucl.close();
			} catch (IOException e) {
				logwolf.e(e);
			}
		}
	}

	public void addPlugin(File file) throws MalformedURLException, ClassNotFoundException, ClassCastException {
		URL[] urls = null;
		URL url = file.toURI().toURL();
		urls = new URL[] { url };
		URLClassLoader ucl = new URLClassLoader(urls);
		try {
			@SuppressWarnings("unchecked")
			Class<? extends ShellPlugin> load = (Class<? extends ShellPlugin>) ucl.loadClass(file.getName());
			addPlugin(load);
		} catch (Exception e) {
			throw new ClassCastException();
		} finally {
			try {
				ucl.close();
			} catch (IOException e) {
				logwolf.e(e);
			}
		}
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
		if((tokenKey!=null)&&(Initrfs.isTokenValid(tokenKey))) {
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
			if(service.getServiceName()!=null) {
				service.startService();
				taskManager.newTask("root", service.getServiceName());
				Initrfs.getLogwolf().d("Started " + pluginName + " service @" + service.toString().split("@")[1]);
			} else {
				Initrfs.getLogwolf().d("Can't start " + pluginName + ", invalid service name.");
			}
			
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

	public void sendSystemEvent(SystemEvent event, String extraValue, CommandRunner commandRunner) {
		for (ShellPlugin thisPlugin : enabledPlugins) {
			try {
				SystemEventsListener listeners = (SystemEventsListener) thisPlugin;
				if (new ArrayList<SystemEvent>(Arrays.asList(listeners.getEvents())).contains(event))
					listeners.runIntent(event, extraValue, commandRunner);
			} catch (Exception e) { }
		}
	}

	public void sendSystemEvent(SystemEvent event, String extraValue, TokenKey tokenKey) {
		CommandRunner commandRunner = new CommandRunner(tokenKey, false);
		sendSystemEvent(event, extraValue, commandRunner);
	}

}
