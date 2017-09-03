package com.xspacesoft.kowax.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.PomParser;
import com.xspacesoft.kowax.apis.PrivilegedAcces;
import com.xspacesoft.kowax.engine.PluginBase;
import com.xspacesoft.kowax.engine.PluginManager;
import com.xspacesoft.kowax.engine.SystemApi;
import com.xspacesoft.kowax.engine.TaskManager;
import com.xspacesoft.kowax.engine.TaskManager.Task;
import com.xspacesoft.kowax.engine.TokenKey;
import com.xspacesoft.kowax.engine.io.OutputWriter;
import com.xspacesoft.kowax.engine.io.Stdio;
import com.xspacesoft.kowax.engine.shell.CommandRunner;
import com.xspacesoft.kowax.engine.shell.ShellIO;

public class KowaBox extends PluginBase implements PrivilegedAcces {

	private static final String EULA = "eula.txt";
	private TokenKey tokenKey;
	private Properties systemProperties = PomParser.load();

	@Override
	public String getAppletName() {
		return "System";
	}

	@Override
	public String getAppletVersion() {
		return "1.0";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski";
	}

	@Override
	protected void runApplet(String[] command, Stdio stdio, CommandRunner commandRunner) {
		if(command==null) {
			stdio.println("Hint");
			return;
		}
		String[] job = command;
		if (job[0].equalsIgnoreCase("ps")) {
			printTasks(commandRunner, stdio);
		} else if (job[0].equalsIgnoreCase("hwinfo")) {
			hwInfo(stdio);
		} else if (job[0].equalsIgnoreCase("gc")) {
			garbageCollector();
		} else if (job[0].equalsIgnoreCase("shutdown")) {
			shutdown(stdio, commandRunner);
		} else if (job[0].equalsIgnoreCase("eula")) {
			showEula(stdio);
		} else if (job[0].equalsIgnoreCase("about")) {
			showAbout(stdio);
		} else if (job[0].equalsIgnoreCase("sudo")) {
			if(job.length>1)
				commandRunner.sudo(Arrays.asList(command).subList(1, job.length).toArray(new String[0]));
			else
				commandRunner.sudo();
		} else if (job[0].equalsIgnoreCase("whoami")){
			stdio.println(commandRunner.getUsername());
		} else if (job[0].equalsIgnoreCase("version")) {
			stdio.println(systemProperties.getProperty("version","test build"));
		} else if (job[0].equalsIgnoreCase("clear")) {
			stdio.clear();
		}
	}

	private void showAbout(Stdio stdio) {
		stdio.println(systemProperties.getProperty("artifactId","KowaX")
				+ " by Kowalski7cc. Copyright XSpaceSoft 2009-2017.");
		stdio.println();
	}

	private void showEula(Stdio stdio) {
		stdio.println("End-User License Agreement for "
				+ systemProperties.getProperty("artifactId","KowaX"));
		try {
			InputStream is = Core.class.getResourceAsStream(EULA);
			Scanner scn = new Scanner(is);
			while(scn.hasNext()) {
				stdio.println(scn.nextLine() + "");
			}
			scn.close();
		} catch (Exception e) {

		}
		stdio.println();
	}

	private void shutdown(Stdio stdio, CommandRunner commandRunner) {
		if(!commandRunner.isSudo()) {
			stdio.println("Must be root");
		} else {
			stdio.println("Shutting down");
			Core.getLogwolf().i("Stopping " + systemProperties.getProperty("artifactId","KowaX"));
			((PluginManager) Core.getSystemApi(SystemApi.PLUGIN_MANAGER, tokenKey)).stopServices();
			Core.getLogwolf().i("Closing server socket");
			try {
				OutputWriter outputWriter = stdio.getOutputWriter();
				if(outputWriter instanceof ShellIO) {
					ShellIO io = (ShellIO) outputWriter;
					io.getSocket().close();
				}
			} catch (IOException e) {
				Core.getLogwolf().e("IOException on shutdown: " + e);
			} finally {
				Core.halt();
			}
		}
	}

	@SuppressWarnings("unused")
	private void poweroff() {
		Core.halt();
	}

	private void garbageCollector() {
		System.gc();
	}

	private void hwInfo(Stdio stdio) {		
		stdio.println("System informations");
		stdio.println("System name-> " + System.getProperty("os.name") + " on arch " + System.getProperty("os.arch"));
		stdio.println("JVM Memory-> " + (((Runtime.getRuntime().maxMemory()/1024)/1024)-((Runtime.getRuntime().freeMemory()/1024)/1024))
				+ "MB of " + ((Runtime.getRuntime().maxMemory()/1024)/1024) + "MB");
		stdio.println("CPU cores-> " + Runtime.getRuntime().availableProcessors());
		stdio.println();
	}

	private void printTasks(CommandRunner commandRunner, Stdio stdio) {
		stdio.println("Running processes:");
		stdio.println("User" + "\t" + "Pid" + "\t" +"Start" + "\t" + "Service" + "\t" + "Task Name");
		SimpleDateFormat ft = new SimpleDateFormat ("HH:mm");
		List<Task> tasks = ((TaskManager) Core.getSystemApi(SystemApi.TASK_MANAGER, tokenKey)).getRunningTasks();
		for(Task task : tasks) {
			stdio.println(task.getUser() + "\t" + 
					task.getPid() + "\t" + 
					ft.format(task.getDate()) + "\t" 
					+ (task.getService()!=null ? "yes" : "no") + "\t"
					+ task.getAppletName());
		}
		stdio.println();
	}

	@Override
	public String getDescription() {
		return "System base app.";
	}

	@Override
	public String getHint() {
		return null;
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}

}
