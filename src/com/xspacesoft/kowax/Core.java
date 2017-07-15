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
import com.xspacesoft.kowax.kernel.SystemApi;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager;
import com.xspacesoft.kowax.kernel.UsersManager.ExistingUserException;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.ShellServer;
import com.xspacesoft.kowax.windowsystem.KowaxDirectDraw;


public class Core {
	public final static String SHELLNAME = BuildGet.getString("build.artifact").startsWith("$") ?
			"KowaX" : BuildGet.getString("build.artifact"); //$NON-NLS-1$
	public final static String VERSION = BuildGet.getString("build.version").startsWith("$") ?
			"Test build" : BuildGet.getString("build.version"); //$NON-NLS-1$
	public final static String BUILD = BuildGet.getString("build.number").startsWith("$") ? 
			null : BuildGet.getString("build.number"); //$NON-NLS-1$
	public final static int API = Stdio.parseInt(BuildGet.getString("build.apilevel"));

	private int port;
	private int http;
	private boolean debug;
	private boolean verbose;
	private String newUser;
	private String newPassword;
	private static File kowaxHome;
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

	public Core(File home, int port, int http, boolean debug, boolean verbose, InputStream defalutSystemIn, PrintStream defaultSystemOut) {
		this.port = port;
		this.http = http;
		this.debug = debug;
		this.verbose = verbose;
		kowaxHome = home;
		shellInput = defalutSystemIn;
		shellOutput = defaultSystemOut;
	}

	public void setNewUser(String username, String password) {
		this.newUser = username;
		this.newPassword = password;
	}

	@SuppressWarnings("unchecked")
	public void start() {
		logwolf = new Logwolf(System.out);
		logwolf.setDebug(debug);
		logwolf.setVerbose(verbose);
		logwolf.i("Init started");

		// TOKEN KEY GENERATION
		logwolf.v("Creating new TokenKey");
		tokenKey = TokenKey.newKey();
		logwolf.d("TokenKey: ==" + tokenKey.getKey() + "==");
		sleep(100);

		// TASK MANAGER LOAD-UP
		logwolf.v("Starting TaksManager");
		taskManager = new TaskManager();
		taskManager.newTask("root", "KInit");
		logwolf.i("Task manager started");
		sleep(100);

		// GET CURRENT WORK FOLDER AND FILE VARS
		checkFolderTree();
		sleep(100);

		// User Manager
		logwolf.v("Loading UsersManager");
		usersManager = new UsersManager();
		usersManager.loadFromFile();
		if(usersManager.getLoadedUsers()==0)
			if((newUser!=null)&&(newPassword!=null)) {
				try {
					usersManager.addUser(newUser, newPassword, "First system administrator", false);
				} catch (ExistingUserException e) { }
			} else {
				try {
					usersManager.loadDefaults();
				} catch (ExistingUserException e2) { }
			}
		logwolf.d("UsersManager loaded");
		logwolf.i("Registred users: " + usersManager.getLoadedUsers());
		// Alias Manager
		logwolf.v("Loading AliasManager");
		aliasManager = new AliasManager();
		logwolf.v("AliasManager loaded");
		aliasManager.loadDefaults();
		logwolf.i(aliasManager.getLoadedAliases() + " aliases loaded");

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

		// START PLUGIN MANAGER AND LOAD PLUGINS
		logwolf.v("Starting PluginManager");
		pluginManager = new PluginManager(tokenKey);
		logwolf.v("PluginManager Started");
		logwolf.v("Loading default plugins");
		List<Class<? extends PluginBase>> startupBlacklist = new ArrayList<Class<? extends PluginBase>>();
		logwolf.d("-----------------------");
		for (int i = 0; i < CORE_PLUGINS_DATA.length; i++) {
			try {
				if(CORE_PLUGINS_DATA[i][0].getClass().getSuperclass().isInstance(PluginBase.class)) {
					pluginManager.addPlugin((Class<? extends PluginBase>) CORE_PLUGINS_DATA[i][0],
							(boolean) CORE_PLUGINS_DATA[i][2], 
							((boolean)CORE_PLUGINS_DATA[i][1] ? tokenKey : null));
					if((boolean) CORE_PLUGINS_DATA[i][3])
						startupBlacklist.add((Class<? extends PluginBase>) CORE_PLUGINS_DATA[i][0]);
				} else {
					logwolf.e(CORE_PLUGINS_DATA[i][0] + " is not a valid plugin entry");
				}
				//				splash.getProgressBar().setValue(p+=step);
				Thread.sleep(50);
			} catch (InstantiationException | IllegalAccessException e) {
				logwolf.e("Unknown error when loading " + CORE_PLUGINS_DATA[i][0] + ": " + e);
			} catch (InterruptedException e) {

			}
		}
		//		splash.getProgressBar().setValue(1);
		//		splash.getProgressBar().setIndeterminate(true);
		logwolf.d("-----------------------");
		logwolf.d("Default plugins load complete");
		sleep(100);

		// Server open
		serviceEnabled = true;
		logwolf.i("Preparing shell server");
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
			} else if (e.getMessage().contains("Address already in use")) {
				logwolf.e("Is already a server running on port " + port + "? " + e.toString());
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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
		sleep(100);
		logwolf.v("Broadcasting system event startup");
		pluginManager.sendSystemEvent(SystemEvent.SYSTEM_START, null, tokenKey, startupBlacklist);
		sleep(100);
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

	@Deprecated
	public static AliasManager getAliasManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return aliasManager;
		logwolf.w("[SEKowaX] - Invalid token recived (getAliasManager)");
		return null;
	}

	@Deprecated
	public static PluginManager getPluginManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return pluginManager;
		logwolf.w("[SEKowaX] - Invalid token recived (getPluginManager)");
		return null;
	}

	@Deprecated
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
			Preferences.userRoot().node(Core.class.getName()).putBoolean("configured", false);
			return true;
		}
		return false;
	}

	@Deprecated
	public static TaskManager getTaskManager(TokenKey token) {
		if(token==null)
			return null;
		if(tokenKey.equals(token))
			return taskManager;
		logwolf.w("[SEKowaX] - Invalid token recived (getTaskManager)");
		return null;
	}

	@Deprecated
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
		//		printStream.print("\u001b[2J");
		for(int c=0; c<1000; c++) {
			printStream.println("\b");
		}
		printStream.flush();
	}

	public static <T> T getSystemApi(SystemApi api, TokenKey tokenKey) {
		if(api==null)
			throw new IllegalArgumentException("SystemApi is null");
		if((tokenKey!=null)&&(!isTokenValid(tokenKey)))
			throw new TokenKey.InvalidTokenException();
		switch(api) {
		case ALIAS_MANAGER:
			if(isTokenValid(tokenKey))
				return (T) aliasManager;
			break;
		case HTTP_DISPLAY:
			if(isTokenValid(tokenKey))
				return (T) kowaxDirectDraw;
			break;
		case INPUT_STREAM:
			if(isTokenValid(tokenKey))
				return (T) shellInput;
			break;
		case KOWAX_HOME:
			if(isTokenValid(tokenKey))
				return (T) kowaxHome;
			break;
		case LOGWOLF:
			return (T) logwolf;
		case OUTPUT_STREAM:
			if(isTokenValid(tokenKey))
				return (T) shellOutput;
			break;
		case PLUGIN_MANAGER:
			if(isTokenValid(tokenKey))
				return (T) pluginManager;
			break;
		case SERVER_SOCKET:
			if(isTokenValid(tokenKey))
				return (T) serverSocket;
			break;
		case TASK_MANAGER:
			if(isTokenValid(tokenKey))
				return (T) taskManager;
			break;
		case USERS_MANAGER:
			if(isTokenValid(tokenKey))
				return (T) usersManager;
			break;
		default:
			return null;
		}
		return null;
	}

	public static void stopShellSocket() {
		serviceEnabled = false;
	}

	public static void halt() {
		serviceEnabled = false;
		pluginManager.stopServices();
		kowaxDirectDraw.stopServer();
		System.exit(0);
	}

	public static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) { }
	}

	public static File getSystemFolder(SystemFolder folder, String user, TokenKey tokenKey) {
		switch (folder) {
		case BIN:
			return new File(kowaxHome, "bin");
		case DEV: 
			return new File(kowaxHome, "dev");
		case ETC:
			return new File(kowaxHome, "etc");
		case ROOT:
			if(isTokenValid(tokenKey))
				return new File(kowaxHome, "root");
			return null;
		case TEMP:
			return new File(kowaxHome, "temp");
		case USER_HOME:
			if(!new File(new File(kowaxHome, "home"), user).exists())
				new File(new File(kowaxHome, "home"), user).mkdirs();
			return new File(new File(kowaxHome, "home"), user);
		default:
			return null;
		}
	}
}
