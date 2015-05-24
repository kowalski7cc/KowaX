package com.xspacesoft.kowax.windowsystem.kenvironment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.xspacesoft.kowax.kernel.TaskManager.Task;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.windowsystem.DisplayManager;
import com.xspacesoft.kowax.windowsystem.KowaxDirectDraw;
import com.xspacesoft.kowax.windowsystem.Window;
import com.xspacesoft.kowax.windowsystem.WindowManager;

public class KowaxDisplayManager extends PluginBase implements DisplayManager, HttpHandler, KernelAccess, SystemEventsListener, Service {

	class InterfaceSession {
		private String username;
		private WindowManager windowManager;
		public InterfaceSession(String username, WindowManager windowManager) {
			super();
			this.username = username;
			this.windowManager = windowManager;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public WindowManager getWindowManager() {
			return windowManager;
		}
		public void setWindowManager(WindowManager windowManager) {
			this.windowManager = windowManager;
		}
		public void close() {
			windowManager.close();
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
			mySession = new InterfaceSession(principal, new KowaxWindowManager(tokenKey, principal));
			sessions.add(mySession);
		}		
		code.append("<html>");
		code.append("<head>");
		code.append("<title>" + principal + "@" + Initrfs.SHELLNAME + "</title>");
		code.append("<style>");
		code.append("table.pro, th.pro, td.pro { border: 0px solid black; }");
//		code.append("table, th, td { border: 0px solid black; border-collapse: collapse; } th, td { padding: 15px;  vertical-align: top;}");
		code.append("table { width: 100%; } input.apps { width: 100%; } button.system { width: 200px; ");
		code.append("button, input.apps { -webkit-appearance: none; height: 150px; }");
		code.append("fieldset.dash { height: 100%; margin: 5px; vertical-align: top; display: block;}");
		code.append("fieldset.app { height: 90%; margin: 5px; }");
		code.append(".divContainer{ width: 100%; height: 100%; }");
//		code.append(".divColumn { vertical-align: top; display: table-cell; }");
//		code.append(".divRow { display: table-row; height: auto; }");
		code.append(".containerColumna{ height: 100%; }");
		code.append("</style>");
		code.append("</head><body>");
		code.append("<h1>" + Initrfs.SHELLNAME + " " + Initrfs.VERSION + "</h1>");
		code.append("<h2>Welcome back, " + principal + "!</h2>");
		code.append("<hr/>");
		try {
			if(httpExchange.getRequestURI().getQuery()!=null) {
				Map <String,String> params = queryToMap(httpExchange.getRequestURI().getQuery());
				if(params.containsKey("application")) {
					String appName = params.get("application");
					Window myWindow = null;
					WindowManager winManager = mySession.getWindowManager();
					myWindow = winManager.getApplication(appName, params);
					if(myWindow==null) {
						myWindow = winManager.runApplication(appName, params);
					}
					code.append("<fieldset class=app>");
					code.append("<legend>");
					// Check if app really opened
					if(myWindow!=null) {
						code.append("<fieldset>" + myWindow.getTitle() + "  ");
						if(myWindow.isMinimizeSupported())
							code.append("<button onClick='window.location.assign(\"desktop\")'>-</button>" + "   ");
						code.append("<button onClick='window.location.assign(\"desktop?closeApp="
							+ myWindow.getTitle() + "\")'>X</button>" + "</fieldset>");
					} else {
						code.append("<fieldset>" + "Error" + "  ");
						code.append("<button onClick='window.location.assign(\"desktop\")'>X</button>" + "</fieldset>");
						
					}
					code.append("</legend>");
					if(myWindow!=null)
						if(myWindow.getContent()!=null)
							code.append(myWindow.getContent().toString());
						else
							code.append("<h2>Error showing content</h2>");
					else
						code.append("</h2>Error launching application</h2>");
					code.append("</fieldset>");
				} else if (params.containsKey("closeApp")) {
					String appName = params.get("closeApp");
					Window myWindow = null;
					WindowManager winManager = mySession.getWindowManager();
					myWindow = winManager.getApplication(appName, params);
					if(myWindow==null) {
						code.append("Application not found");
					} else {
						winManager.closeApplication(myWindow);
						code.append("Application closed");
					}
					code.append("<br/><button onClick='window.location.assign(\"desktop\")'>Return to desktop</button>");
					code.append("<script> window.location.assign(\"desktop\"); </script>");
				}
			} else {
//				code.append("<div class='divContainer'><div class=divRow><div class=divColumn>");
				code.append("<fieldset class='dash'><legend><fieldset><b>Dashboard</b></fieldset></legend><table border=0><tr><td>");
				code.append("<div class='divContainer'>");
				code.append("<fieldset class='dash'><legend><fieldset>All applications</fieldset></legend>");
				code.append("<form action='desktop' method='get'>");
				for(KWindow window : guiApplications)
					code.append("<input title='"
							+ (window.getDescription()==null ? "No desctription available" : window.getDescription())
							+ "' class=apps type='submit' name='application' value='" + window.getAppletName() + "'/><br/>");
				code.append("</form></fieldset></td>");
				code.append("<td><fieldset class='dash'><legend><fieldset>Running processes</fieldset></legend>");
				code.append("<table class=pro>");
				List<Task> tasks = Initrfs.getTaskManager(tokenKey).getRunningTasks();
				code.append("<tr class=pro><th class=pro>User</th><th class=pro>Pid</th><th class=pro>Process</th></tr>");
				for(Task task : tasks) {
					code.append("<tr class=pro><td class=pro>" + task.getUser() + "</td><td class=pro>" + task.getPid() + "</td><td class=pro>" + task.getAppletName()  +"</td>");
				}
				code.append("</table></td></fieldset><td><fieldset class='dash'><legend><fieldset>System users</fieldset></legend><ul>");
				String[] sysUsers = Initrfs.getUsersManager(tokenKey).getUsersName();
				for(String usr : sysUsers) {
					code.append("<li>" + usr + "</li>");
				}
				code.append("</fieldset></td></tr></table><br>");
				code.append("<fieldset><legend><fieldset><b>System actions</b></fieldset></legend><ul>");
				code.append("<li><button class=system>Change password</button></li>");
				code.append("<li><button class=system onClick='window.location.assign(\"logout\")'>Log out</button></li>");
				code.append("<li><button class=system onClick='window.location.assign(\"sysapi?shutdown\")'>Shutdown</button></li>");
				code.append("</ul></fieldset><br/>Copyright XSpaceSoft 2008-2015</fieldset>");
//				code.append("</div></div></div>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	@Override
	public List<KWindow> getSupportedApps() {
		return guiApplications;
	}

	@Override
	public void logout(String user) {
		InterfaceSession session = getSession(user);
		session.windowManager = null;
		session.close();
		sessions.remove(getSession(user));
		
	}

}
