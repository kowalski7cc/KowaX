package com.xspacesoft.kowax.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.SystemFolder;
import com.xspacesoft.kowax.apis.PrivilegedAcces;
import com.xspacesoft.kowax.engine.PluginBase;
import com.xspacesoft.kowax.engine.PluginManager;
import com.xspacesoft.kowax.engine.SystemApi;
import com.xspacesoft.kowax.engine.TokenKey;
import com.xspacesoft.kowax.engine.io.Stdio;
import com.xspacesoft.kowax.engine.shell.CommandRunner;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class PLoaderTest extends PluginBase implements PrivilegedAcces {

	private TokenKey tokenKey;
	private Map<String, String> packages;
	private Map<String, Boolean> loadedPackages;
	private File appData;

	public PLoaderTest() {
		packages = new HashMap<String, String>();
		loadedPackages = new HashMap<String, Boolean>();
		File f = new File(Core.getSystemFolder(SystemFolder.CONFIGURATIONS, tokenKey),"PluginLoader");
		f.mkdirs();
		appData = new File(f, "config.json");
		loadData();
	}
	
	private void loadData() {
		if(appData.exists()) {
			StringBuilder stringBuilder = new StringBuilder();
			try (Scanner scanner = new Scanner(appData)) {
				scanner.forEachRemaining(stringBuilder::append);
			} catch (Exception e) {
				Core.getLogwolf().e("Failed to load previous config");
			}
			try {
				JSONArray dataArray = new JSONArray(stringBuilder.toString());
				for(int i = 0; i < dataArray.length(); i++) {
					JSONObject obj = dataArray.getJSONObject(i);
					if(obj.has("name")&&obj.has("class")) {
						packages.put(obj.getString("name"), obj.getString("class"));
						loadedPackages.put(obj.getString("name"), false);
					} else {
						Core.getLogwolf().w("Warning, array element " + i + " is malformed (class-name)");
					}
				}
			} catch (JSONException e) {
				if(Core.getLogwolf().isDebug())
					Core.getLogwolf().e(e.toString());
				else
					Core.getLogwolf().e("Invalid configuration file");
			}
		} else {
			saveData();
		}
	}

	@Override
	public String getAppletName() {
		return "Ploader";
	}

	@Override
	public String getAppletVersion() {
		return "1.0";
	}

	@Override
	public String getAppletAuthor() {
		return "kowalski7cc";
	}

	@Override
	protected void runApplet(String[] command, Stdio stdio, CommandRunner commandRunner) {
		if(command!=null) {
			switch(command[0]) {
			case "add":
			case "addpackage":
				if(command.length>=3)
					addpackage(command, stdio);
				else
					printhelp(stdio);
				break;
			case "load":
			case "loadpackage":
				if(command.length>=2)
					loadpackage(command, stdio);
				else
					printhelp(stdio);
				break;
			case "list":
			case "listpackages":
				listpackages(stdio);
				break;
			case "help":
			case "?":
			default:
				printhelp(stdio);
			}
		} else {
			printhelp(stdio);
		}
	}
	
	private void load(Stdio stdio, String packageName, String className) {
		File bin = Core.getSystemFolder(SystemFolder.APPLICATIONS, null, tokenKey);
		File jar = new File(bin, packageName);
		PluginManager pluginManager = (PluginManager) Core.getSystemApi(SystemApi.PLUGIN_MANAGER, tokenKey);
		try {
			pluginManager.loadPluginFilesystem(jar, className, true, true);
			stdio.println(className + " plugin loaded");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void listpackages(Stdio stdio) {
		stdio.println("ID\t\tName\t\t\tClass\t\t\t\t\tLoaded");
		int i = 0;
		for(String a : packages.keySet()) {
			Boolean loaded = loadedPackages.get(a);
			String l = (loaded==null?"false":loaded.toString());
			stdio.println(i++ + "\t\t" + a + "\t\t" + packages.get(a) + "\t"+ l);
		}
	}

	private void loadpackage(String[] command, Stdio stdio) {
		String packageName = command[1];
		File binDir = Core.getSystemFolder(SystemFolder.APPLICATIONS, tokenKey);
		File packageFile = new File(binDir, packageName);
		if(!packageFile.exists()) {
			try {
				Integer id = Integer.parseInt(packageName);
				if(id.intValue()<0||id.intValue()>packages.size()-1) {
					stdio.println("Invalid ID. Use " + getAppletName() + " listpackages to view IDs.");
					return;
				} else {
					String[] names = packages.keySet().toArray(new String[] {});
					packageName = names[id.intValue()];
					packageFile = new File(binDir, packageName);
					if(!packageFile.exists()) {
						stdio.println("Can't find " + packageName + " stopping package import.");
						return;
					}
				}
			} catch (NumberFormatException e) {
				stdio.println("Can't find " + packageName + " stopping package import.");
				return;
			}
			
		}
		String className = packages.get(packageName);
		if(className==null)
			if(command.length>=3){
				className = command[2];
			} else {
				stdio.println("Please add package with " + getAppletName() + " addpackage or specify class");
				return;
			}
		try {
			load(stdio, packageName, className);
		} catch (Exception e) {
			stdio.println("Error during class load: ");
			stdio.println(e.toString());
		}
	}

	private void printhelp(Stdio stdio) {
		stdio.println("Help for Plugin Loader");
		stdio.println();
		stdio.println(getAppletName() + " addpackage packagename.jar com.fully.qualified.name");
		stdio.println(getAppletName() + " removepackage name");
		stdio.println(getAppletName() + " loadpackage name [com.fully.qualified.name]");
		stdio.println(getAppletName() + " listpackages");
		stdio.println(getAppletName() + " ");
	}

	private void addpackage(String[] command, Stdio stdio) {
		String packageName = command[1];
		File binDir = Core.getSystemFolder(SystemFolder.APPLICATIONS, tokenKey);
		File packageFile = new File(binDir, packageName);
		if(!packageFile.exists()) {
			stdio.println("Can't find " + packageName + " stopping package import.");
			return;
		}
		String className = command[2];
		packages.put(packageName, className);
		loadedPackages.put(packageName, false);
		saveData();
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
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}
	
	private void saveData() {
		if(!appData.exists())
			try {
				appData.createNewFile();
			} catch (IOException e) {
				Core.getLogwolf().c(e.toString());
				return;
			}
		JSONArray dataArray = new JSONArray();
		for(String name : packages.keySet()) {
			try {
				JSONObject pluginData = new JSONObject();
				pluginData.put("name", name);
				pluginData.put("class", packages.get(name));
				dataArray.put(pluginData);
			} catch (JSONException e) {
				Core.getLogwolf().e(e.toString());
			}
		}
		try (PrintWriter p = new PrintWriter(new FileOutputStream(appData))) {
			p.println(dataArray.toString());
		} catch (FileNotFoundException e) {
			Core.getLogwolf().e(e.toString());
		}
	}

}
