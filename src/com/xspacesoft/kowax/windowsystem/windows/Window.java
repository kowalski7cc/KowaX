package com.xspacesoft.kowax.windowsystem.windows;

import java.util.Map;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.kernel.TokenKey;

public class Window {
	
	private String title;
	private StringBuilder content;
	private String principal;
	private Map <String,String> params;
	private boolean minimizeSupported;
	private KWindow associatedApp;
	private int pid;

	public Window(String title, boolean minimizeSupported, KWindow associatedApp, String principal) {
		this.title = title;
		this.minimizeSupported = minimizeSupported;
		this.associatedApp = associatedApp;
		this.principal = principal;
		content = new StringBuilder();
	}

	public Window(String name, boolean minimizeSupported) {
		new Window(name, minimizeSupported, null, "root");
	}

	public Window(String name) {
		new Window(name, true);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public StringBuilder getContent() {
		return content;
	}

	public void setContent(StringBuilder content) {
		this.content = content;
	}

	public boolean isMinimizeSupported() {
		return minimizeSupported;
	}

	public void setMinimizeSupported(boolean minimizeSupported) {
		this.minimizeSupported = minimizeSupported;
	}

	
	public KWindow getAssociatedApp() {
		return associatedApp;
	}

	public void setAssociatedApp(KWindow associatedApp) {
		this.associatedApp = associatedApp;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid, TokenKey tokenKey) {
		if(Core.isTokenValid(tokenKey))
			this.pid = pid;
	}

	public String getPrincipal() {
		return principal;
	}
	
	public String paramGet(String key) {
		return this.params.get(key);
	}
	
	public boolean paramContainsKey(String key) {
		return this.params.containsKey(key);
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
}
