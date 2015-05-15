package com.xspacesoft.kowax.windowsystem;

public class HttpBuilder {
	
	public final static String JAVASCRIPT_HISTORY_GOBACK = "javascript:history.back()";

	public static String redirect(int time, String body, String title, String path) {
		StringBuilder string = new StringBuilder();
		string.append("<html>");
		string.append("<head>");
		if(title!=null)
			string.append("<title>" + title + "</title>");
		string.append("<meta http-equiv=\"refresh\" content=\"" + time + ";url=\"" + path + "\">");
		string.append("</head>");
		string.append("<body>");
		if(body!=null)
			string.append(body);
		string.append("</body>");
		return string.toString();
	}
	
	public static String redirectBack(int time, String body, String title) {
		return redirect(time, body, title, JAVASCRIPT_HISTORY_GOBACK);
	}
	
	public static String buildPage(String head, String body) {
		StringBuilder string = new StringBuilder();
		string.append("<html>");
		string.append("<head>");
		if(head!=null)
			string.append(head);
		string.append("</head>");
		string.append("<body>");
		if(body!=null)
			string.append(body);
		string.append("</body>");
		return string.toString();
	}
}
