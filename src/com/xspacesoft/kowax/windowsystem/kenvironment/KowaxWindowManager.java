package com.xspacesoft.kowax.windowsystem.kenvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.windowsystem.Window;
import com.xspacesoft.kowax.windowsystem.WindowManager;

public class KowaxWindowManager implements WindowManager, KernelAccess{
	
	private List<Window> windows;
	private TokenKey tokenKey;
	private List<KWindow> guiApps;
	private String principal;
	private int myPid;

	public KowaxWindowManager() {
		windows = new ArrayList<Window>();
		guiApps = new ArrayList<KWindow>();
	}
	
	public KowaxWindowManager(TokenKey tokenKey, String principal) {
		windows = new ArrayList<Window>();
		guiApps = new ArrayList<KWindow>();
		setTokenKey(tokenKey);
		this.principal = principal;
		myPid = Initrfs.getTaskManager(tokenKey).newTask(principal, "KWM");
	}

	@Override
	public boolean isAppOpen(String name) {
		if(windows.isEmpty())
			return false;
		for(Window window : windows) {
			if(window.getTitle().equals(name))
				return true;
		}
		return false;
	}

	@Override
	public Window getApplication(String name, Map<String, String> params) {
		for(Window window : windows) {
			if(window.getTitle().equals(name)){
				window.setParams(params);
				window.getAssociatedApp().onWindowResume(window);
				return window;
			}
		}
		return null;
	}

	@Override
	public Window runApplication(String name, Map<String, String> params) {
		KWindow rightPlugin = null;
		for(KWindow myPlugin : guiApps) {
			if(myPlugin.getAppletName().equals(name))
				rightPlugin = myPlugin;
		}
		if(rightPlugin!=null) {
			Window newWindow = new Window(name, true, rightPlugin, principal);
			try {
			KWindow kwin = rightPlugin;
			newWindow.setParams(params);
			kwin.onCreateWindow(newWindow);
			windows.add(newWindow);
			newWindow.setPid(Initrfs.getTaskManager(tokenKey).newTask(principal, newWindow.getTitle() + " (KWM)"), tokenKey);
			return newWindow;
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
		guiApps = Initrfs.getKowaxDirectDraw(tokenKey).getDisplayManger().getSupportedApps();
	}

	@Override
	public void closeApplication(Window myWindow) {
		for(Window window : windows) {
			if(window.equals(myWindow)) {
				window.getAssociatedApp().onDestroyWindow(window);
				Initrfs.getTaskManager(tokenKey).removeTask(myWindow.getPid());
				windows.remove(window);
				System.gc();
				return;
			}
		}
	}
	
	public void unload() {
		for(Window window : windows) {
				window.getAssociatedApp().onDestroyWindow(window);
				Initrfs.getTaskManager(tokenKey).removeTask(window.getPid());
				windows.remove(window);
				return;
		}
	}

	@Override
	public void close() {
		unload();
		Initrfs.getTaskManager(tokenKey).removeTask(myPid);
	}

}
