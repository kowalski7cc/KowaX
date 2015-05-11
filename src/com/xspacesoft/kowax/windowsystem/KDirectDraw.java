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

public class KDirectDraw {

	protected DisplaySettingsManager settingsManager;
	private HttpContext desktop;
	private HttpContext apis;
	private HttpServer server;

	private class DisplaySettingsManager {
		public TokenKey tokenKey;
		public int pid;
		public Object displayManager;
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
					HttpBuilder.redirectBack(5, "<h1>Command sent</h1>You will be redirected in 5 seconds.", "Command runner");
				} catch (IllegalArgumentException | CommandNotFoundException | MissingPluginCodeException e) {
					HttpBuilder.redirectBack(5, "<h1>Command failed</h1>You will be redirected in 5 seconds.", "Command runner");
				}
			} else {
				HttpBuilder.buildPage("<title>" + Initrfs.SHELLNAME + " - Invalid api request", "<h1>Invalid request</h1>"
						+ "Press <a href=\"" + HttpBuilder.JAVASCRIPT_HISTORY_GOBACK + ">here</a> to go back.");
			}
		}
	}

	public KDirectDraw(int serverPort, TokenKey tokenKey) {
		settingsManager = new DisplaySettingsManager();
		this.settingsManager.tokenKey = tokenKey;
		try {
			server = HttpServer.create(new InetSocketAddress(serverPort), 0);
			this.settingsManager.displayManager = new KDisplayManager(tokenKey);
			server.createContext("/", new DesktopRedirect());
			server.createContext("/logout", new Logout());
			BasicAuthenticator authenticator = new BasicAuthenticator(Initrfs.SHELLNAME + " " + Initrfs.VERSION + " login") {
				@Override
				public boolean checkCredentials(String user, String pwd) {
					try {
						return Initrfs.getUsersManager(settingsManager.tokenKey).isPasswordValid(user, pwd);
					} catch (InvalidUserException e) {
						return false;
					}
				}
			};
			desktop = server.createContext("/desktop", (HttpHandler) this.settingsManager.displayManager);
			desktop.setAuthenticator(authenticator);
			apis = server.createContext("/sysapi", new HttpApiHandler());
			apis.setAuthenticator(authenticator);
			server.setExecutor(null);
		} catch (IOException e) {
			Initrfs.getLogwolf().e("Failed to start KWS: " + e.toString());
		}
	}



	public boolean startServer() {
		try {
			server.start();
			settingsManager.pid = Initrfs.getTaskManager(settingsManager.tokenKey).newTask("root", "KDirectDraw Server");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean stopServer() {
		try {
			server.stop(0);
			Initrfs.getTaskManager(settingsManager.tokenKey).removeTask(settingsManager.pid);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void setDisplayManager(DisplayManager displayManager) {
		this.settingsManager.displayManager = displayManager;
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
}
