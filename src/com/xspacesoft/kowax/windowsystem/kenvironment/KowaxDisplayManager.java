package com.xspacesoft.kowax.windowsystem.kenvironment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.apis.Service;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.exceptions.InsufficientPermissionsException;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.windowsystem.DisplayManager;
import com.xspacesoft.kowax.windowsystem.KowaxDirectDraw;
import com.xspacesoft.kowax.windowsystem.WindowManager;

public class KowaxDisplayManager extends PluginBase implements DisplayManager, HttpHandler, KernelAccess, SystemEventsListener, Service {

	class InterfaceSession {
		private String username;
		private KowaxDisplayManager windowManager;
		public InterfaceSession(String username, KowaxDisplayManager kowaxDisplayManager) {
			super();
			this.username = username;
			this.windowManager = kowaxDisplayManager;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public WindowManager getWindowManager() {
			return (WindowManager) windowManager;
		}
		public void setWindowManager(WindowManager windowManager) {
			this.windowManager = (KowaxDisplayManager) windowManager;
		}
	}

	private TokenKey tokenKey;
	private List<KWindow> guiApplications;
	private List<InterfaceSession> sessions;
	private PluginManager pluginManager;
	private List<PluginBase> plugins;

	public KowaxDisplayManager() {
		sessions = new ArrayList<InterfaceSession>();
		guiApplications = new ArrayList<KWindow>();
		
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
//		if(this.plugins==null)
//			loadPlugins();
		StringBuilder code = new StringBuilder();
		String principal = httpExchange.getPrincipal().toString();
		InterfaceSession mySession = getSession(httpExchange.getPrincipal().toString());
		if(mySession==null) {
			mySession = new InterfaceSession(principal, new KowaxDisplayManager());
			sessions.add(mySession);
		}
//		Map <String,String> params = KowaxDirectDraw.queryToMap(httpExchange.getRequestURI().getQuery());
		code.append("<html>");
		code.append("<head>");
		code.append("<title>" + principal + "@" + Initrfs.SHELLNAME + "</title>");
		code.append("</head><body>");
		code.append("<h1>" + Initrfs.SHELLNAME + " " + Initrfs.VERSION + "</h1>");
		code.append("<h2>Welcome back, " + principal + "!</h2>");
		code.append("<hr/>");
		code.append("Aviable applications: <br/>");
		for(KWindow window : guiApplications)
			code.append("<button>" + window.getAppletName() + "</button><br/>");
		code.append("</body></html>");
		KowaxDirectDraw.writeResponse(httpExchange, code.toString());
		
	}

	private void loadPlugins() {
		List<PluginBase> plugins = pluginManager.getPlugins();
		guiApplications = new ArrayList<KWindow>();
		for(PluginBase plugin : plugins) {
			try {
				guiApplications.add((KWindow) plugin);
			} catch (Exception e) {
				
			}
		}
	}
	
	@Override
	public String getAppletName() {
		return "kdm";
	}

	@Override
	public String getAppletVersion() {
		return "1.0A";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski7cc";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		if(command==null) {
			stdio.println(getHint());
			return;
		}
		switch(command.toLowerCase()) {
		case "--replace": setDisplayManager();
		break;
		}
	}

	@Override
	public String getDescription() {
		return "Kowax Display Manager";
	}

	@Override
	public String getHint() {
		return "Usage kdm --replace";
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		if(Initrfs.isTokenValid(tokenKey))
			this.tokenKey = tokenKey;
	}

	@Override
	public SystemEvent[] getEvents() {
		return new SystemEvent[]{ SystemEvent.SYSTEM_START };
	}

	@Override
	public void runIntent(SystemEvent event, String extraValue, CommandRunner commandRunner) {
		if(event == SystemEvent.SYSTEM_START)
			setDisplayManager();
	}

	@Override
	public void setDisplayManager() {
		Initrfs.getLogwolf().i("[KDM]: Setting Display Manager");
		Initrfs.getKowaxDirectDraw(tokenKey).setDisplayManager(this);
		if(this.plugins==null)
			loadPlugins();
	}

	private InterfaceSession getSession(String username) {
		for(InterfaceSession interfaceSession : sessions) {
			if(interfaceSession.getUsername().equals(username))
				return interfaceSession;
		}
		return null;
	}

	@Override
	public Boolean isServiceRunning() {
		// No service exist, we just return false
		return false;
	}

	@Override
	public void startService() {
		// The service implementation is a workaround for load thing @ startup
		// With elevated permissions
		if(Initrfs.isTokenValid(tokenKey))
			pluginManager = Initrfs.getPluginManager(tokenKey);
		else
			throw new InsufficientPermissionsException();
		loadPlugins();
	}

	@Override
	public void stopService() {
		
	}

	@Override
	public String getServiceName() {
		return "KDM";
	}
}
