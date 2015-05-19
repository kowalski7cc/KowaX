package com.xspacesoft.kowax.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.windowsystem.Window;

public class KowaxUpdater extends PluginBase implements KernelAccess, SystemEventsListener, KWindow {

	@SuppressWarnings("unused")
	private TokenKey tokenKey;
	private final static String COMMITURL = "https://bitbucket.org/api/2.0/repositories/xspacesoft/kowax/commits";

	public KowaxUpdater() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public SystemEvent[] getEvents() {
		return new SystemEvent[] { SystemEvent.SYSTEM_START };
	}

	@Override
	public void runIntent(SystemEvent event, String extraValue, CommandRunner commandRunner) {
		if(event==SystemEvent.SYSTEM_START) {
			if(isUpdateAvailable())
				Initrfs.getLogwolf().i("[KowaxUpdater] - Update available!");
		}
	}

	private String downloadCommits() {
		URL url;
		try {
			url = new URL(COMMITURL);
			HttpURLConnection hConnection = (HttpURLConnection) url
					.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
				BufferedReader is = new BufferedReader(new
						InputStreamReader(hConnection.getInputStream()));
				String response = is.readLine();
				return response;
			} else {
				return null;
			}
		} catch(IOException e) {
			return null;
		}
	}

	private String[] getCommits() {
		String result = downloadCommits();
		String[] splitted = result.split("hash");
		return splitted;
	}

	private boolean isUpdateAvailable() {
		String[] commits = getCommits();
		if(commits==null)
			return false;
		else if(!Arrays.asList(commits).contains(Initrfs.BUILD)) {
			return false;
		} else if (Arrays.asList(commits).get(1).contains(Initrfs.BUILD)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}

	@Override
	public String getAppletName() {
		return "Updater";
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
			if(isUpdateAvailable())
				stdio.println("There is an update ready for download!");
			else
				stdio.println("No update is available.");
			return;
		}
		switch(command.split(" ")[0]) {
		case "update":
			if(isUpdateAvailable())
				downloadUpdate();
			break;
		default:
			
		}
	}

	private void downloadUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreateWindow(Window window) {
		if(isUpdateAvailable())
			window.getContent().append("<h4>Update ready for download!</h4>");
		else
			window.getContent().append("<h4>No update available.</h4>");
	}

	@Override
	public void onDestroyWindow(Window window) {

	}

	@Override
	public void onWindowHidden(Window window) {

	}

	@Override
	public void onWindowResume(Window window) {

	}

	@SuppressWarnings("unused")
	private static void downloadFileFromURL(String urlString, File destination) throws IOException {
		URL website = new URL(urlString);
		ReadableByteChannel rbc;
		rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(destination);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}

}
