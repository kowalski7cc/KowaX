package com.xspacesoft.kowax.windowsystem;

import com.xspacesoft.kowax.apis.KWindow;

public class Window {
	
	private String title;
	private StringBuilder content;
	private boolean minimizeSupported;
	private KWindow associatedApp;

	public Window(String title, boolean minimizeSupported, KWindow associatedApp) {
		this.title = title;
		this.minimizeSupported = minimizeSupported;
		this.associatedApp = associatedApp;
		content = new StringBuilder();
	}

	public Window(String name, boolean minimizeSupported) {
		new Window(name, minimizeSupported, null);
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
}
