package com.xspacesoft.kowax.windowsystem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.TaskManager.Task;
import com.xspacesoft.kowax.kernel.TokenKey;

public class KDisplayManager implements DisplayManager, HttpHandler {
	
	private TokenKey tokenKey;
	private int pid;

	public KDisplayManager(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
		Initrfs.getLogwolf().i("KDesktopManager loaded");
		pid = Initrfs.getTaskManager(tokenKey).newTask("root", "KDesktopManager");
	}

	@Override
	public void setWindowManager(WindowManager windowManager) {
		// TODO Set window manager
	}

	@Override
	public void handle(HttpExchange arg0) throws IOException {
		String username = arg0.getPrincipal().getName().toString();
		StringBuilder myResponse = new StringBuilder();
		myResponse.append("<html>");
		myResponse.append("<head>");
		myResponse.append("<title>" + username + "@" +Initrfs.SHELLNAME + " " + Initrfs.VERSION + "</title>");
		myResponse.append("<style>td{vertical-align: top; padding: 15px;}</style>");
		myResponse.append("</head>");
		myResponse.append("<body>");
		myResponse.append("<h1>" + Initrfs.SHELLNAME + " " + Initrfs.VERSION + " - WebOS Experimental GUI</h1>");
		myResponse.append("<h2>Welcome back, " + username + "!</h2>");
		myResponse.append("<hr/>");
		myResponse.append("<table border=\"0\"><tr><td>");
		myResponse.append("<h3>Installed apps:</h3>");
		myResponse.append("<ul>");
		List<PluginBase> plugins = Initrfs.getPluginManager(tokenKey).getPlugins();
		for(PluginBase plugin : plugins) {
			myResponse.append("<a title=\"");
			myResponse.append(plugin.getClass().toString());
			myResponse.append("\"><li>");
			myResponse.append(plugin.getAppletName());
			myResponse.append("</li></a>");
		}
		myResponse.append("</ul>");
		myResponse.append("</td></td><td><td>");
		myResponse.append("<h3>Running processes:</h3>");
		myResponse.append("<ul>");
		ArrayList<Task> tasks = Initrfs.getTaskManager(tokenKey).getRunningTasks();
		for(Task task : tasks) {
			myResponse.append("<li>");
			myResponse.append(task.getUser() + " - " + task.getPid() + " - " + task.getAppletName());
			myResponse.append("</li>");
		}
		myResponse.append("</ul>");
		myResponse.append("</td></td><td><td>");
		myResponse.append("<h3>System users:</h3>");
		String[] users = Initrfs.getUsersManager(tokenKey).getUsersName();
		for(String user : users) {
			myResponse.append("<li>");
			if(user.equals(username))
				myResponse.append(user + " (You)");
			else
				myResponse.append(user);
			myResponse.append("</li>");
		}
		myResponse.append("</ul>");
		myResponse.append("</td></tr></table>");
		myResponse.append("<h3>System actions:</h3>");
		myResponse.append("<ul>");
		myResponse.append("<li>Command runner: ");
		myResponse.append("<form action=\"/sysapi\">");
		myResponse.append("<input type=\"text\" name=\"commandrunner\">");
		myResponse.append("<input type=\"submit\" value=\"Run\"/>");
		myResponse.append("</form>");
		myResponse.append("</li>");
		myResponse.append("<a href=\"/404\"><li>Change password</li></a>");
		myResponse.append("<a href=\"/logout\"><li>Log out</li></a>");
		myResponse.append("<a href=\"/sysapi?shutdown\"><li>Shutdown</li></a>");
		myResponse.append("</ul>");
		myResponse.append("<body>");
		myResponse.append("</html>");
		writeResponse(arg0, myResponse.toString());
	}

	public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
	    httpExchange.sendResponseHeaders(200, response.length());
	    OutputStream os = httpExchange.getResponseBody();
	    os.write(response.getBytes());
	    os.close();
	}
	
	public int getPid() {
		return pid;
	}
}
