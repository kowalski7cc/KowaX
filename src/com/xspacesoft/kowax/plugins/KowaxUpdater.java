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

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.Logwolf;
import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.apis.SystemEventsListener;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.SystemApi;
import com.xspacesoft.kowax.kernel.SystemEvent;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.windowsystem.windows.Window;

public class KowaxUpdater extends PluginBase implements KernelAccess, SystemEventsListener, KWindow {

	private TokenKey tokenKey;
	private File update;
	private static String build;
	private static String lastBuild;
	private final static String COMMITURL = "https://bitbucket.org/api/2.0/repositories/xspacesoft/kowax/commits";
	private final static boolean DEBUG = false;
	private final static String BASEURL = "https://drone.io/bitbucket.org/xspacesoft/kowax/files/target/";
	private static String version;
	public KowaxUpdater() {
		if(!DEBUG)
			build = Core.BUILD;
		else
			build = "77b272edf73ca4490831253d0da7349bbd7f7328";
	}

	@Override
	public SystemEvent[] getEvents() {
		return new SystemEvent[] { SystemEvent.SYSTEM_START };
	}

	@Override
	public void runIntent(SystemEvent event, String extraValue, CommandRunner commandRunner) {
		if(event==SystemEvent.SYSTEM_START) {
			Logwolf.updateSplash("[KowaxUpdate] - Checking for updates...");
			if(isUpdateAvailable()) {
				Core.getLogwolf().i("[KowaxUpdate] - Use 'Update -upgrade' from shell or Start upgrade from GUI.");
			} else {
				Core.getLogwolf().i("[KowaxUpdate] - No update available");
			}
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
//				Stream<String> buffer = is.lines();
				String response = is.readLine();
//				System.of.a.Down();
				response = response.substring(20, 110);
				return response;
			} else {
				return null;
			}
		} catch(IOException e) {
			return null;
		}
	}

	private String getLastCommit() {
		String result = downloadCommits();
		return result.split("\"hash\": \"")[1].split("\"")[0];
	}

	private boolean isUpdateAvailable() {
		if(build==null) {
			Core.getLogwolf().w("[KowaxUpdate] - Can't get build version. Is this a dev build?");
			return false;
		}
		Core.getLogwolf().i("[KowaxUpdate] - Checking for updates...");
		String commit = getLastCommit();
		if(commit==null) {
			Core.getLogwolf().w("Can't check for updates. Are you offline?");
			return false;
		}
		lastBuild = commit;
		Core.getLogwolf().d("[KowaxUpdate] - My BUILD: '"+ build.substring(0, 10) +"...'");
		Core.getLogwolf().d("[KowaxUpdate] - Last BUILD: '" + lastBuild.substring(0, 10) + "'");
		if (!commit.equals(build)) {
			Core.getLogwolf().i("[KowaxUpdate] - Update available!");
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}

	@Override
	public String getAppletName() {
		return "Update";
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
		case "upgrade":
			if(isUpdateAvailable()){
				getBuildBranch();
				if(downloadUpdate()) {
					stdio.println("Update success. Rebooting.");
					systemReload();
				} else {
					stdio.println("Update failed, check console for more informations");
				}
			}
			break;
		default:
			
		}
	}
	
	private String getBuildBranch() {
		URL url;
		try {
			url = new URL("https://bitbucket.org/xspacesoft/kowax/raw/" + lastBuild + "/pom.xml");
//			System.out.println("https://bitbucket.org/xspacesoft/kowax/raw/" + lastBuild + "/pom.xml");
			HttpURLConnection hConnection = (HttpURLConnection) url
					.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
				BufferedReader is = new BufferedReader(new
						InputStreamReader(hConnection.getInputStream()));
				String response = "";
				String buffer;
				while((buffer = is.readLine()) != null) {
					response += buffer;
					if(buffer.contains("</version>"))
						break;
				}
//				System.out.println(response);
				if(!response.equals("")) {
					response = response.split("<version>")[1].split("</version>")[0];
					version = response;
//					System.out.println(response);
					Core.getLogwolf().i("[KowaxUpdate] - Branch: " + response );
					return response;
				}
				return null;
			} else {
				return null;
			}
		} catch(IOException e) {
			return null;
		}
	}

	private boolean downloadUpdate() {
		File file = new File("KowaX-" + version + "-update." + (System.getProperty("os.name").toLowerCase().contains("win") ? "exe" : "jar"));
		Core.getLogwolf().i("[KowaxUpdate] - Downloading file: " + file.getName());
		try {
			downloadFileFromURL(BASEURL + "KowaX-AlphaPreview."
					+ (System.getProperty("os.name").toLowerCase().contains("win") ? "exe" : "jar"), file);
			Core.getLogwolf().i("[KowaxUpdate] - Download complete!");
			Core.getLogwolf().i("[KowaxUpdate] - Path: " + file.getAbsolutePath());
			this.update = file;
			return true;
		} catch (IOException e) {
			Core.getLogwolf().e("[KowaxUpdate] - Can't download update file: " + e.toString()); 
			return false;
		}
	}

	@Override
	public String getDescription() {
		return "KowaX Update Plugin";
	}

	@Override
	public String getHint() {
		return "Usage: Update (upgrade)";
	}

	@Override
	public void onCreateWindow(Window window) {
		window.setTitle("KowaX Update");
		if(window.paramContainsKey("upgrade")) {
			if(isUpdateAvailable()){
				getBuildBranch();
				if(downloadUpdate()) {
					window.getContent().append("<h4>Update success!</h4>");
					window.getContent().append("<ul><li>Current build: " + lastBuild + "</li></ul>");
					systemReload();
				} else {
					window.getContent().append("<h4>Update failed</h4>");
					window.getContent().append("Check console for more details.");
				}
			}
		} else {
			if(isUpdateAvailable()) {
				window.getContent().append("<h4>Update ready for download!</h4>");
				window.getContent().append("<ul>");
				window.getContent().append("<li>Current build: '" + build + "'</li>");
				window.getContent().append("<li>Lastest build: '" + lastBuild + "'</li>");
				window.getContent().append("</ul>");
				window.getContent().append("<button onClick='window.location.assign(\"desktop?application=" + getAppletName() + "&upgrade=do\")'>Start upgrade</button>");
			} else {
				window.getContent().append("<h4>No update available.</h4>");
				window.getContent().append("<ul>");
				if(build.equalsIgnoreCase("na"))
					window.getContent().append("<li>Current build unaviable. Is this a dev build?</li>");
				else
					window.getContent().append("<li>Current build: '" + build + "'</li>");
				window.getContent().append("</ul>");
			}
		}
	}

	private void systemReload() {
		File path = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
//		System.out.println("1: "+path);
//		System.out.println("2: "+update);
		if(path.canWrite())
			System.out.println("Can write");
//		path.setWritable(true);
//		path.delete();
		if(path.getName().equals("classes")) {
			update.renameTo(new File("KowaX-" + version + (System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : ".jar")));
		} else {
			path.delete();
			update.renameTo(path);
		}
		update.delete();
		((PluginManager) Core.getSystemApi(SystemApi.PLUGIN_MANAGER, tokenKey)).stopServices();
		Core.halt();
		System.out.println("3: "+update);
		try {
			if(Runtime.getRuntime().exec(new String[]{"java", "-jar", path.getAbsolutePath()}).isAlive())
				System.out.println("IS ALIVE");
//				System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private static void downloadFileFromURL(String urlString, File destination) throws IOException {
		URL website = new URL(urlString);
		ReadableByteChannel rbc;
		rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(destination);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}

}
