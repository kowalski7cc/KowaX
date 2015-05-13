package com.xspacesoft.kowax.windowsystem;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.exceptions.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager.InvalidUserException;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.shell.CommandRunner.CommandNotFoundException;

public class KowaxDirectDraw {

	protected DisplaySettingsManager settingsManager;
	private HttpContext desktop;
	private HttpContext apis;
	private HttpServer server;
	private BasicAuthenticator authenticator;

	private class DisplaySettingsManager {
		public TokenKey tokenKey;
		public int pid;
		public Object displayManager;
		public int port;
		public boolean running;
	}

	public class DesktopRedirect implements HttpHandler {
		@Override
		public void handle(HttpExchange httpExchange) throws IOException {
			String response = "<html><head><meta http-equiv=\"refresh\" content=\"0;url=./desktop\"></head></html>";
			httpExchange.sendResponseHeaders(200, response.length());
			OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public class Logout implements HttpHandler {
		@Override
		public void handle(HttpExchange httpExchange) throws IOException {
			String response = "<html><body><h1>Logged out</h1></body></html>";
			httpExchange.sendResponseHeaders(401, response.length());
			OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public class HttpApiHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange httpExchange) throws IOException {
			Map <String,String>params = queryToMap(httpExchange.getRequestURI().getQuery());
			if(params.get("shutdown")!=null) {
//				if(httpExchange.getPrincipal().toString().equals("root")))
				String response = "<html><head><title>System off</title></head><body><h1>System off</h1><body></html>";
				writeResponse(httpExchange, response);
				Initrfs.getPluginManager(settingsManager.tokenKey).stopServices();
				Initrfs.halt();
			} else if (params.get("commandrunner")!=null) {
				CommandRunner cmd = new CommandRunner(settingsManager.tokenKey, false);
				try {
					cmd.run(params.get("commandrunner"));
					String response = HttpBuilder.redirectBack(5, "<h1>Command sent</h1>You will be redirected in 5 seconds.", "Command runner");
					writeResponse(httpExchange, response);
				} catch (IllegalArgumentException | CommandNotFoundException | MissingPluginCodeException e) {
					String response = HttpBuilder.redirectBack(5, "<h1>Command failed</h1>You will be redirected in 5 seconds.", "Command runner");
					writeResponse(httpExchange, response);
				}
			} else if (params.get("displaymanager")!=null) {
				String response;
				if (params.get("displaymanager").equals("kdm")) {
					setDisplayManager(new KowaxTestDisplayManager(settingsManager.tokenKey));
					response = HttpBuilder.redirectBack(5, "<h1>KDM1 is your new DisplayManager</h1>You will be redirected in 5 seconds.", "Display manager");
				} else if(params.get("displaymanager").equals("kdm2")) {
//					settingsManager.displayManager = new KowaxDisplayManager();
					response = HttpBuilder.redirectBack(5, "<h1>Use \"Kdm --replace\" from console</h1>You will be redirected in 5 seconds.", "Command runner");
				} else {
					response = HttpBuilder.redirectBack(5, "<h1>Selected DisplayManager not found</h1>You will be redirected in 5 seconds.", "Command runner");
				}
				writeResponse(httpExchange, response);
				stopServer();
			} else {
				writeResponse(httpExchange, HttpBuilder.buildPage("<title>" + Initrfs.SHELLNAME + " - Invalid api request", "<h1>Invalid request</h1>"
						+ "Press <a href=\"" + HttpBuilder.JAVASCRIPT_HISTORY_GOBACK + ">here</a> to go back."));
			}
		}
	}
	
	public KowaxDirectDraw(int serverPort, TokenKey tokenKey, Class<? extends DisplayManager> displayManager) {
		settingsManager = new DisplaySettingsManager();
		this.settingsManager.tokenKey = tokenKey;
		this.settingsManager.port = serverPort;
		this.settingsManager.running = false;
		setUp(serverPort, displayManager, tokenKey);
	}

	private void setUp(int serverPort, Object displayManager, TokenKey tokenKey) {
		try {
			server = HttpServer.create(new InetSocketAddress(serverPort), 0);
			server.createContext("/", new DesktopRedirect());
			server.createContext("/logout", new Logout());
			authenticator = new BasicAuthenticator(Initrfs.SHELLNAME + " " + Initrfs.VERSION + " login") {
				@Override
				public boolean checkCredentials(String user, String pwd) {
					try {
						return Initrfs.getUsersManager(settingsManager.tokenKey).isPasswordValid(user, pwd);
					} catch (InvalidUserException e) {
						return false;
					}
				}
			};
			apis = server.createContext("/sysapi", new HttpApiHandler());
			apis.setAuthenticator(authenticator);
			server.setExecutor(null);
		} catch (IOException e) {
			Initrfs.getLogwolf().e("Failed to start KWS: " + e.toString());
		}
	}


public boolean startServer() {
	setDisplayManager(settingsManager.displayManager);
	try {
			server.start();
			settingsManager.pid = Initrfs.getTaskManager(settingsManager.tokenKey).newTask("root", "KowaxDirectDraw Server");
			this.settingsManager.running = true;
			Initrfs.getLogwolf().i("KowaxDirectDraw server started succesfuly");
			return true;
		} catch (IllegalStateException e) {
			Initrfs.getLogwolf().i("KowaxDirectDraw server already running");
			return true;
		} catch (Exception e) {
			Initrfs.getLogwolf().e("Failed to start KowaxDirectDraw server: " + e.toString());
			return false;
		}
	}

	public boolean stopServer() {
		try {
			server.stop(0);
			Initrfs.getTaskManager(settingsManager.tokenKey).removeTask(settingsManager.pid);
			this.settingsManager.running = false;
			Initrfs.getLogwolf().i("KowaxDirectDraw server stopped succesfuly");
			return true;
		} catch (Exception e) {
			Initrfs.getLogwolf().e("Failed to stop KowaxDirectDraw server: " + e.toString());
			return false;
		}
	}
	
	public boolean reloadServer() {
		return stopServer() && startServer();
	}

	public void setDisplayManager(Object displayManager) {
		boolean wasRunning = false;
		if(settingsManager.running) {
			wasRunning = true;
			stopServer();
		}
		this.settingsManager.displayManager = displayManager;
		setUp(this.settingsManager.port, displayManager, this.settingsManager.tokenKey);
		desktop = server.createContext("/desktop", (HttpHandler) this.settingsManager.displayManager);
		desktop.setAuthenticator(authenticator);
		if(wasRunning)
			startServer();
	}
	
	public void setDisplayManager(Class<? extends DisplayManager> displayManager) throws InstantiationException, IllegalAccessException {
		setDisplayManager(displayManager.newInstance());
	}

	public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
		httpExchange.sendResponseHeaders(200, response.length());
		OutputStream os = httpExchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	/**
	 * returns the url parameters in a map
	 * @param query
	 * @return map
	 */
	public static Map<String, String> queryToMap(String query){
		Map<String, String> result = new HashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length>1) {
				result.put(pair[0], pair[1]);
			}else{
				result.put(pair[0], "");
			}
		}
		return result;
	}

	public boolean isRunning() {
		return this.settingsManager.running;
	}
}
