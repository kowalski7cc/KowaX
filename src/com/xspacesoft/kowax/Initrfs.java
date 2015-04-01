package com.xspacesoft.kowax;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.xspacesoft.kowax.kernel.AliasManager;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager;
import com.xspacesoft.kowax.kernel.UsersManager.ExistingUserException;
import com.xspacesoft.kowax.plugins.*;
import com.xspacesoft.kowax.services.CronTab;
import com.xspacesoft.kowax.shell.ShellServer;


public class Initrfs {
	
	public final static String SHELLNAME = "Kowax";
	public final static String VERSION = "2.0A"; //$NON-NLS-1$
	private final static int DEFAULT_SERVER_PORT = 23;
	private int port;
	private static TokenKey tokenKey;
	private static Logwolf logwolf;
	private static AliasManager aliasManager;
	private static PluginManager pluginManager;
	private static TaskManager taskManager;
	private static UsersManager usersManager;
	private static CronTab cronTab;
	private static ServerSocket serverSocket;
	private static final Object[] CORE_PLUGINS_KERNELACCESS = {
		SystemPlugin.class,
	};
	private static final Object[] CORE_PLUGINS = {
		AppExample.class,
	};
	
	public static void main(String[] args) {
		OptionsParser ap = new OptionsParser(args);
		if(ap.getTag("h")||ap.getTag("help")) { //$NON-NLS-1$ //$NON-NLS-2$
			printHelp();
			System.exit(0);
		}
		int port = DEFAULT_SERVER_PORT;
		if(ap.getArgument("port")!=null) { //$NON-NLS-1$
			if(isNumber(ap.getArgument("port"))) { //$NON-NLS-1$
				port = parseInt(ap.getArgument("port")); //$NON-NLS-1$
			}
		}
		System.out.println("Booting " + SHELLNAME + " V" + VERSION);
		int proc = Runtime.getRuntime().availableProcessors();
		for (int i = 0; i < proc; i++) {
			System.out.print("K ");
		}
		System.out.println();
		System.out.println("----------------");
		Initrfs init = new Initrfs(port);
		init.start();
	}
	
	public Initrfs(int port) {
		this.port = port;
	}
	
	@SuppressWarnings("unchecked")
	public void start() {
		logwolf = new Logwolf();
		logwolf.setDebug(true);
		logwolf.setVerbose(true);
		logwolf.i("Started loading initrfs");
		logwolf.v("Starting TaksManager");
		taskManager = new TaskManager();
		taskManager.newTask("root", "KInit");
		logwolf.i("Task manager started");
		logwolf.v("Creating new TokenKey");
		tokenKey = new TokenKey();
		tokenKey.newKey();
		logwolf.d("TokenKey: ==" + tokenKey.getKey() + "==");
		logwolf.v("Starting PluginManager");
		pluginManager = new PluginManager();
		logwolf.v("PluginManager Started");
		logwolf.v("Loading default plugins");
		for (int i = 0; i < CORE_PLUGINS.length; i++) {
			try {
				pluginManager.addPlugin((Class<? extends ShellPlugin>) CORE_PLUGINS[i]);
				logwolf.d(CORE_PLUGINS[i] + " loaded");
			} catch (InstantiationException | IllegalAccessException e) {
				logwolf.e("Cannot load " + CORE_PLUGINS[i]);
			}
		}
		for (int i = 0; i < CORE_PLUGINS_KERNELACCESS.length; i++) {
			try {
				pluginManager.addPlugin((Class<? extends ShellPlugin>) CORE_PLUGINS_KERNELACCESS[i], tokenKey);
				logwolf.d(CORE_PLUGINS_KERNELACCESS[i] + " loaded");
			} catch (InstantiationException | IllegalAccessException e) {
				logwolf.e("Cannot load " + CORE_PLUGINS_KERNELACCESS[i]);
			}
		}
		logwolf.d("Default plugins load complete");
		logwolf.v("Loading UsersManager");
		usersManager = new UsersManager();
		try {
			usersManager.loadDefaults();
			logwolf.d("UsersManager loaded");
			logwolf.i("Registred users: " + usersManager.getLoadedUsers());
		} catch (ExistingUserException e1) {
			logwolf.e("Error in loading default users");
			System.exit(1);
		}
		logwolf.v("Loading AliasManager");
		aliasManager = new AliasManager();
		logwolf.v("AliasManager loaded");
		aliasManager.loadDefaults();
		logwolf.i(aliasManager.getLoadedAliases() + " aliases loded");
		logwolf.v("Starting Cron service");
		cronTab = new CronTab(100, tokenKey);
		cronTab.startCronTab();
		logwolf.i("Crontab service started");
		boolean serviceEnabled = true;
		logwolf.i("Opening socket server");
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(1000);
			while(serviceEnabled) {
				try {
					Socket socket = serverSocket.accept();
					if(socket!=null) {
						ShellServer shellServer = new ShellServer(socket, tokenKey);
						shellServer.start();
					}
				} catch (SocketTimeoutException e) { }
				Thread.sleep(1);
			}
		} catch (BindException e) {
			if (e.getMessage().equalsIgnoreCase("Permission denied")){
				logwolf.e("Is server running as root? " + e.toString());
			} else if (e.getMessage().equalsIgnoreCase("Address already in use")) {
				logwolf.e("Is already a server running on port " + port + "? " + e.toString());
			} else {
				logwolf.e(e.toString());
			}
			System.exit(1);
		} catch (IOException e) {
			logwolf.e(e.toString());
			System.exit(1);
		} catch (InterruptedException e) {
			serviceEnabled = false;
		}
	}
	
	public static Logwolf getLogwolf() {
			return logwolf;
	}

	public static AliasManager getAliasManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return aliasManager;
		return null;
	}

	public static PluginManager getPluginManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return pluginManager;
		return null;
	}

	public static TaskManager getTaskManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return taskManager;
		return null;
	}

	public static UsersManager getUsersManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return usersManager;
		return null;
	}

	public static CronTab getCronTab(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return cronTab;
		return null;
	}
	
	public static boolean isTokenValid(TokenKey token) {
		if(token==null)
			return false;
		if(tokenKey.equals(token))
			return true;
		return false;
	}
	
	private static void printHelp() {
		System.out.println("Kowax shell V" + VERSION); //$NON-NLS-1$
		System.out.println();
		System.out.println("Aviable options: "); //$NON-NLS-1$
		System.out.println("-nogui"); //$NON-NLS-1$
		System.out.println("-h -help"); //$NON-NLS-1$
		System.out.println("-password"); //$NON-NLS-1$
		System.out.println("-port"); //$NON-NLS-1$
		System.out.println(""); //$NON-NLS-1$
	}
	
	public static boolean isNumber(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static int parseInt(String string) {
		try {
			int i = Integer.parseInt(string);
			return i;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
