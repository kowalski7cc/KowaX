package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.apis.Service;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class KWindowManager extends PluginBase implements SystemEventsListener, Service, KernelAccess {

	private class KOrg extends Thread {

		@SuppressWarnings("unused")
		private int port;
		@SuppressWarnings("unused")
		private TokenKey tokenKey;
		private boolean running;

		private KOrg(int port, TokenKey tokenKey) {
			this.port = port;
			this.tokenKey = tokenKey;
		}

		@Override
		public void run() {
			while(running) {
				try {
					sleep(1);
				} catch (InterruptedException e) {
					running = false;
				}
			}
		}
	}

	private TokenKey tokenKey;
	private static KOrg kOrg;
	private final static int DEFAULT_PORT = 4096;

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}

	private void loadK() {
		kOrg = new KOrg(DEFAULT_PORT, tokenKey);
	}


	@Override
	public Boolean isServiceRunning() {
		if(kOrg==null)
			return false;
		return kOrg.isAlive();
	}

	@Override
	public void startService() {
		if(kOrg==null)
			loadK();
		kOrg.start();
	}

	@Override
	public void stopService() {
		if(kOrg!=null)
			kOrg.interrupt();
	}

	@Override
	public String getServiceName() {
		return "KWM";
	}

	@Override
	public SystemEvent[] getEvents() {
		return new SystemEvent[] { SystemEvent.SYSTEM_START, SystemEvent.USER_LOGIN_SUCCESS, SystemEvent.USER_LOGOUT};
	}

	@Override
	public void runIntent(SystemEvent event, String extraValue, CommandRunner commandRunner) {

	}

	@Override
	public String getAppletName() {
		return "KWM";
	}

	@Override
	public String getAppletVersion() {
		return "Technology Preview";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {

	}

	@Override
	public String getDescription() {
		return "K Window Manager for KowaX";
	}

	@Override
	public String getHint() {
		return null;
	}

}
