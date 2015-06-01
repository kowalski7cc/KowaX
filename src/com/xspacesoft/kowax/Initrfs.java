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
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import com.xspacesoft.kowax.kernel.AliasManager;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager;
import com.xspacesoft.kowax.kernel.UsersManager.ExistingUserException;
import com.xspacesoft.kowax.shell.ShellServer;
import com.xspacesoft.kowax.windowsystem.KowaxDirectDraw;


public class Initrfs {
	public final static String SHELLNAME = BuildGet.getString("build.artifact").startsWith("$") ?
			"KowaX" : BuildGet.getString("build.artifact"); //$NON-NLS-1$
	public final static String VERSION = BuildGet.getString("build.version").startsWith("$") ?
			"Test build" : BuildGet.getString("build.version"); //$NON-NLS-1$
	public final static String BUILD = BuildGet.getString("build.number").startsWith("$") ? 
			"NA" : BuildGet.getString("build.number"); //$NON-NLS-1$
	public final static int API = Stdio.parseInt(BuildGet.getString("build.apilevel"));
	
	private int port;
	private int http;
	private boolean debug;
	private boolean verbose;
	private static File kowaxHome;
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
	private static KowaxDirectDraw kowaxDirectDraw;

	private static boolean serviceEnabled;
	
	private static final Object[][] CORE_PLUGINS_DATA = DefaultPlugins.getDefaults();
	
	public Initrfs(String home, int port, int http, boolean debug, boolean verbose, InputStream defalutSystemIn, PrintStream defaultSystemOut) {
		this.port = port;
		this.http = http;
		this.debug = debug;
		this.verbose = verbose;
		kowaxHome = new File(home);
		shellInput = defalutSystemIn;
		shellOutput = defaultSystemOut;
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
		List<Class<? extends PluginBase>> startupBlacklist = new ArrayList<Class<? extends PluginBase>>();
		logwolf.d("-----------------------");
		for (int i = 0; i < CORE_PLUGINS_DATA.length; i++) {
			try {
				pluginManager.addPlugin((Class<? extends PluginBase>) CORE_PLUGINS_DATA[i][0],
						(boolean) CORE_PLUGINS_DATA[i][2], 
						((boolean)CORE_PLUGINS_DATA[i][1] ? tokenKey : null));
				if((boolean) CORE_PLUGINS_DATA[i][3])
					startupBlacklist.add((Class<? extends PluginBase>) CORE_PLUGINS_DATA[i][0]);
			} catch (InstantiationException | IllegalAccessException e) {
				logwolf.e("Unknown error when loading " + CORE_PLUGINS_DATA[i][0] + ": " + e);
			}
		}
		logwolf.d("-----------------------");
		logwolf.d("Default plugins load complete");
		
		// User Manager
		logwolf.v("Loading UsersManager");
		usersManager = new UsersManager();
		File usersFile = new File(new File(new File(kowaxHome,"etc"), "users"), "users.kls");
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
		try {
			logwolf.v("Starting KowaxDirectDraw Server");
			kowaxDirectDraw = new KowaxDirectDraw(http, tokenKey, null);
			kowaxDirectDraw.startServer();
			logwolf.i("KowaxDirectDraw server is now up");
		} catch (Exception e) {
			logwolf.e("Error in KDD: "+ e.getMessage());
			logwolf.e("Continuing without GUI...");
		}
		
		
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
		
		pluginManager.sendSystemEvent(SystemEvent.SYSTEM_START, null, tokenKey, startupBlacklist);
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
		if (!new File(kowaxHome, "bin").exists())
			new File(kowaxHome, "bin").mkdir();
		if (!new File(kowaxHome, "etc").exists())
			new File(kowaxHome, "etc").mkdir();
		if (!new File(kowaxHome, "home").exists())
			new File(kowaxHome, "home").mkdir();
		if (!new File(kowaxHome, "temp").exists())
			new File(kowaxHome, "temp").mkdir();
		if (!new File(kowaxHome, "root").exists())
			new File(kowaxHome, "root").mkdir();
		if (!new File(kowaxHome, "dev").exists())
			new File(kowaxHome, "dev").mkdir();
	}

	public static Logwolf getLogwolf() {
		return logwolf;
	}

	public static AliasManager getAliasManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return aliasManager;
		logwolf.w("[SEKowaX] - Invalid token recived (getAliasManager)");
		return null;
	}

	public static PluginManager getPluginManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return pluginManager;
		logwolf.w("[SEKowaX] - Invalid token recived (getPluginManager)");
		return null;
	}
	
	public static KowaxDirectDraw getKowaxDirectDraw(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return kowaxDirectDraw;
		logwolf.w("[SEKowaX] - Invalid token recived (getKowaxDirectDraw)");
		return null;
	}
	
	public static boolean wizardReset(TokenKey token) {
		if(tokenKey == null)
			return false;
		if(tokenKey.equals(token)) {
			Preferences.userRoot().node(Initrfs.class.getName()).putBoolean("configured", false);
			return true;
		}
		return false;
	}

	public static TaskManager getTaskManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return taskManager;
		logwolf.w("[SEKowaX] - Invalid token recived (getTaskManager)");
		return null;
	}

	public static UsersManager getUsersManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return usersManager;
		logwolf.w("[SEKowaX] - Invalid token recived (getUsersManager)");
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
//		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
//		boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
//		boolean isOsx = System.getProperty("os.name").toLowerCase().contains("osx");
//		try {
//			if(isWindows)
//				Runtime.getRuntime().exec("cls");
//			else if(isLinux||isOsx)
//				Runtime.getRuntime().exec("clear");
//			else {
//				printStream.print("\u001b[2J");
//				printStream.flush();
//			}
//		} catch (IOException e) {
//			printStream.print("\u001b[2J");
//			printStream.flush();
//		}
//		printStream.print("\u001b[2J");
		for(int c=0; c<1000; c++) {
			printStream.println("\b");
		}
		printStream.flush();
	}

	public static void halt() {
		serviceEnabled = false;
	}
}
