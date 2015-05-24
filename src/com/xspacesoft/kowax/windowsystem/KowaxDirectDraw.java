package com.xspacesoft.kowax.windowsystem;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager.InvalidUserException;

public class KowaxDirectDraw {

	private HttpContext desktop;
	private HttpContext apis;
	private HttpServer server;
	private BasicAuthenticator authenticator;

	protected TokenKey tokenKey;
	private int serverPid;
	private Object displayManager;
	private int serverPort;
	private boolean running;

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
			((DisplayManager) displayManager).logout(httpExchange.getPrincipal().toString());
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
			Map <String,String> params = queryToMap(httpExchange.getRequestURI().getQuery());
			if(params.get("shutdown")!=null) {
				String response = "<html><head><title>System off</title></head><body><h1>System off</h1><body></html>";
				writeResponse(httpExchange, response);
				Initrfs.getPluginManager(tokenKey).stopServices();
				Initrfs.halt();
			}
		}
	}
	
	public KowaxDirectDraw(int serverPort, TokenKey tokenKey, Class<? extends DisplayManager> displayManager) {
		this.tokenKey = tokenKey;
		this.serverPort = serverPort;
		this.running = false;
		setAuthenticator();
	}
	
	private void setAuthenticator() {
		authenticator = new BasicAuthenticator(Initrfs.SHELLNAME + " " + Initrfs.VERSION + " login") {
			@Override
			public boolean checkCredentials(String user, String pwd) {
				try {
					return Initrfs.getUsersManager(tokenKey).isPasswordValid(user, pwd);
				} catch (InvalidUserException e) {
					return false;
				}
			}
		};
	}

	private void setUp() {
		if(!this.running) {
			try {
				server = HttpServer.create(new InetSocketAddress(serverPort), 0);
				server.createContext("/", new DesktopRedirect());
				server.createContext("/logout", new Logout());
				apis = server.createContext("/sysapi", new HttpApiHandler());
				apis.setAuthenticator(authenticator);
				if(displayManager!=null)
					setDisplayManager(displayManager);
			} catch (BindException e) {
				Initrfs.getLogwolf().e("Failed to start KWS: " + e.toString());
				server=null;
			} catch (IOException e) {
				Initrfs.getLogwolf().e("Failed to start KWS: " + e.toString());
			}
		}
	}
	
	public void startServer() {
		if(isRunning())
			stopServer();
		setUp();
		if(server!=null) {
			server.start();
			Initrfs.getTaskManager(tokenKey).newTask("root", "KowaxDirectDraw Server");
		} else {
			Initrfs.getLogwolf().e("Failed to start KWS: NullPointerServer");
		}
	}
	
	public void stopServer() {
		server.stop(0);
		Initrfs.getTaskManager(tokenKey).removeTask(serverPid);
	}
	
	@Deprecated
	public void setDisplayManager(Class<? extends DisplayManager> displayManager) throws InstantiationException, IllegalAccessException {
		setDisplayManager(displayManager.newInstance());
	}
	
	public void setDisplayManager(Object displayManager) {
		this.displayManager = displayManager;
		desktop = server.createContext("/desktop", (HttpHandler) displayManager);
		desktop.setAuthenticator(authenticator);
	}

	/**
	 * Sends response back to the browser
	 * @param httpExchange
	 * @param response
	 * @throws IOException
	 */
	public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
		httpExchange.sendResponseHeaders(200, response.length());
		OutputStream os = httpExchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	/**
	 * Returns the URL parameters in a map
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
		return running;
	}

	public DisplayManager getDisplayManger() {
		return (DisplayManager) displayManager;
	}

}
