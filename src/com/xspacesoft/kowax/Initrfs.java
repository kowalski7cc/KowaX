package com.xspacesoft.kowax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.xspacesoft.kowax.WindowSystem.KDirectDraw;
import com.xspacesoft.kowax.kernel.AliasManager;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager;
import com.xspacesoft.kowax.kernel.UsersManager.ExistingUserException;
import com.xspacesoft.kowax.plugins.*;
import com.xspacesoft.kowax.shell.ShellServer;


public class Initrfs {
	
	public final static String SHELLNAME = "KowaX";
	public final static String VERSION = "Alpha 1"; //$NON-NLS-1$
	// Indicates api level
	public final static int API = 1;
//	private static File currentDirectory;
	private int port;
	private boolean debug;
	private boolean verbose;
	@SuppressWarnings("unused")
	private static InputStream shellInput;
	private static PrintStream shellOutput;
	private static TokenKey tokenKey;
	private static Logwolf logwolf;
	private static AliasManager aliasManager;
	private static PluginManager pluginManager;
	private static TaskManager taskManager;
	private static UsersManager usersManager;
	private static ServerSocket serverSocket;
	private static KDirectDraw kDirectDraw;
	private static boolean serviceEnabled;
	
	private static final Object[][] CORE_PLUGINS_DATA = {
		// ClassName, RootAccess
		{BusyBox.class, true},
		{CronTab.class, true},
		{AppExample.class, false},
		{HivemindControl.class, false},
		{DenialService.class, false},
		{Kalculator.class, false},
		{Kalendar.class, false},
		{Man.class, false},
		{Fortune.class, false},
	};
	
	public Initrfs(int port, boolean debug, boolean verbose, InputStream defalutSystemIn, PrintStream defaultSystemOut) {
		this.port = port;
		this.debug = debug;
		this.verbose = verbose;
		shellInput = defalutSystemIn;
		shellOutput = defaultSystemOut;
	}
	
	public Initrfs(int port, boolean debug, boolean verbose) {
		this.port = port;
		this.debug = debug;
		this.verbose = verbose;
	}
	
	@SuppressWarnings("unchecked")
	public void start() {
		logwolf = new Logwolf(System.out);
		logwolf.setDebug(debug);
		logwolf.setVerbose(verbose);
		logwolf.i("Started loading initrfs");
		
		// TOKEN KEY GENERATION
		logwolf.v("Creating new TokenKey");
		tokenKey = new TokenKey();
		tokenKey.newKey();
		logwolf.d("TokenKey: ==" + tokenKey.getKey() + "==");
		
		// TASK MANAGER LOAD-UP
		logwolf.v("Starting TaksManager");
		taskManager = new TaskManager();
		taskManager.newTask("root", "KInit");
		logwolf.i("Task manager started");
		
		// GET CURRENT WORK FOLDER AND FILE VARS
		checkFolderTree();
		
		// START PLUGIN MANAGER AND LOAD PLUGINS
		logwolf.v("Starting PluginManager");
		pluginManager = new PluginManager(tokenKey);
		logwolf.v("PluginManager Started");
		logwolf.v("Loading default plugins");
		logwolf.d("-----------------------");
		
		for (int i = 0; i < CORE_PLUGINS_DATA.length; i++) {
			try {
				if((boolean)CORE_PLUGINS_DATA[i][1]==true)
					pluginManager.addPlugin((Class<? extends PluginBase>) CORE_PLUGINS_DATA[i][0], tokenKey);
				else
					pluginManager.addPlugin((Class<? extends PluginBase>) CORE_PLUGINS_DATA[i][0]);
			} catch (InstantiationException | IllegalAccessException e) {
				logwolf.e("Cannot load " + CORE_PLUGINS_DATA[i][0].toString());
			}
		}
		logwolf.d("-----------------------");
		logwolf.d("Default plugins load complete");
		
		// User Manager
		logwolf.v("Loading UsersManager");
		usersManager = new UsersManager();
		File usersFile = new File("users.kls");
		logwolf.i("Users file path: " + usersFile.toURI().toString());
		try {
			if((usersFile!=null)&&(usersFile.exists())) {
				usersManager.loadFromFile(usersFile);
			} else {
				usersManager.loadDefaults();
			}
			logwolf.d("UsersManager loaded");
			logwolf.i("Registred users: " + usersManager.getLoadedUsers());
		} catch (ExistingUserException e1) {
			logwolf.e("Error in loading users");
			System.exit(1);
		}
		
		// Alias Manager
		logwolf.v("Loading AliasManager");
		aliasManager = new AliasManager();
		logwolf.v("AliasManager loaded");
		aliasManager.loadDefaults();
		logwolf.i(aliasManager.getLoadedAliases() + " aliases loded");
		
		// Start KWindowSystem
		logwolf.v("Starting KDirectDraw Server");
		kDirectDraw = new KDirectDraw(80, tokenKey);
		kDirectDraw.startServer();
		logwolf.i("KDirectDraw server is now up");
		
		// Server open
		serviceEnabled = true;
		logwolf.i("Opening socket server");
		try {
			serverSocket = new ServerSocket(port);
		} catch (BindException e) {
			if (e.getMessage().equalsIgnoreCase("Permission denied")){
				logwolf.e("Is server running as root? " + e.toString());
				String backupPort = port + "" + port;
				logwolf.i("Trying to open server on port " + backupPort);
				try {
					serverSocket = new ServerSocket(Stdio.parseInt(backupPort));
				} catch (IOException e1) {
					logwolf.e("Failed to open server on port " + backupPort + ": " + e1.toString());
				}
			} else if (e.getMessage().equalsIgnoreCase("Address already in use")) {
				logwolf.e("Is already a server running on port " + port + "? " + e.toString());
				System.exit(1);
			} else {
				logwolf.e(e.toString());
				System.exit(1);
			}
			
		} catch (IOException e) {
			logwolf.e(e.toString());
			System.exit(1);
		}
		if (serverSocket == null) {
			System.exit(1);
		}
		pluginManager.sendSystemEvent(SystemEvent.SYSTEM_START, null, tokenKey);
		try {
			serverSocket.setSoTimeout(1000);
			logwolf.i("Server ready!");
			while(serviceEnabled) {
				try {
					Socket socket = serverSocket.accept();
					if(socket!=null) {
						ShellServer shellServer = new ShellServer(socket, tokenKey);
						shellServer.start();
					}
				} catch (SocketTimeoutException e) {
					// Wait for it
				} catch (IOException e) {
					logwolf.e("Connection failed: " + e.toString());
				}
				Thread.sleep(1);
			}
		} catch (InterruptedException e) {
			// Stop service
			serviceEnabled = false;
		} catch (SocketException e) {
			logwolf.e(e.toString());
		}
		logwolf.i("Server stopped");
//		Pause pause = new Pause(System.in, System.out);
//		try {
//			pause.showPause();
//		} catch (IOException e) { } finally {
//			System.exit(0);
//		}
		System.exit(0);
	}
	
	private void checkFolderTree() {
//		currentDirectory = new File("");
		if (!new File("bin").exists())
			new File("bin").mkdir();
		if (!new File("etc").exists())
			new File("etc").mkdir();
		if (!new File("home").exists())
			new File("home").mkdir();
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
	
	public static boolean isTokenValid(TokenKey token) {
		if(token==null)
			return false;
		if(tokenKey.equals(token))
			return true;
		return false;
	}
	
	public static void clear() {
		if(shellOutput!=null)
			clear(shellOutput);
	}
	
	public static void clear(PrintStream printStream) {
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
		boolean isOsx = System.getProperty("os.name").toLowerCase().contains("osx");
		if(isWindows) {
			try {
				Runtime.getRuntime().exec("cls");
			} catch (IOException e) {
				printStream.print("\u001b[2J");
				printStream.flush();
			}
		} else if(isLinux) {
			printStream.print("\u001b[2J");
			printStream.flush();
		} else if (isOsx) {
			try {
				Runtime.getRuntime().exec("clear");
			} catch (IOException e) {
				printStream.print("\u001b[2J");
				printStream.flush();
			}
		} else {
			printStream.print("\u001b[2J");
			shellOutput.flush();
		}
	}

	public static void halt() {
		serviceEnabled = false;
	}
}
