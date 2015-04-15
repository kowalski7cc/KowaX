package com.xspacesoft.kowax.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.TaskManager.Task;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.shell.CommandRunner;

public class BusyBox extends ShellPlugin implements KernelAccess {
	
	private TokenKey tokenKey;

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
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		if(command==null) {
			stdio.println("Hint");
			return;
		}
		String[] job = command.split(" ");
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
		} else if (job[0].equalsIgnoreCase("ls")) {
			listApplets();
		} else if (job[0].equalsIgnoreCase("sudo")) {
			if(job.length>1)
				commandRunner.sudo(command.substring(job[0].length() + 1));
			else
				commandRunner.sudo(null);
		} else if (job[0].equalsIgnoreCase("alias")) {
			if(job.length>1) {
				Initrfs.getLogwolf().i(command);
				makeAlias(command.substring(job[0].length()+1), commandRunner);
			} else
				stdio.println("Usage: alias alias=command");
		} else if (job[0].equalsIgnoreCase("run")) {
			runExternalClass(command.substring(job[0].length()+1), commandRunner);
		} else if (job[0].equalsIgnoreCase("load")) {
			commandRunner.loadExternalClass(command.substring(job[0].length()+1));
		} else if (job[0].equalsIgnoreCase("whoami")){
			stdio.println(commandRunner.getUsername());
		} else if (job[0].equalsIgnoreCase("version")) {
			stdio.println(Initrfs.VERSION);
		} else if (job[0].equalsIgnoreCase("clear")) {
			stdio.clear();
		} else if (job[0].equalsIgnoreCase("reverse")) {
			stdio.reverse();
		} else if (job[0].equalsIgnoreCase("help")) {
			PluginManager pluginManager = Initrfs.getPluginManager(tokenKey);
			List<ShellPlugin> shellPlugins = pluginManager.getPlugins();
			for(ShellPlugin shellPlugin : shellPlugins) {
				stdio.print(shellPlugin.getAppletName().substring(0, 1).toUpperCase() + 
						shellPlugin.getAppletName().substring(1) + "   ");
			}
			stdio.println();
		} else if (job[0].equalsIgnoreCase("echo")) {
			stdio.println(command.substring(job[0].length()+1));
		}
	}

	private void runExternalClass(String command, CommandRunner commandRunner) {
		if(command.split(" ").length<2) {
			commandRunner.getSocketHelper().println("Usage: run \"classpath\" \"commands\".");
		}
		commandRunner.runExternalClass(command.split(" ")[0],
				command.substring(command.split(" ")[0].length()));
	}

	private void makeAlias(String substring, CommandRunner commandRunner) {
		if((substring.split("=").length<2)||(substring.split("=").length>3)) {
			String[] a = substring.split("=");
			Initrfs.getLogwolf().i(substring);
			Initrfs.getAliasManager(tokenKey).newAlias(a[1], a[0]);
			commandRunner.getSocketHelper().println("Alias created");
		}
	}

	private void listApplets() {
		// TODO Auto-generated method stub
		
	}

	private void showAbout(Stdio stdio) {
		stdio.println(Initrfs.SHELLNAME + " by Kowalski7cc. Copyright XSpaceSoft 2009-2015.");
		stdio.println();
	}

	private void showEula(Stdio stdio) {
		stdio.println("End-User License Agreement for Project Security");
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("eula.txt");
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
			Initrfs.getLogwolf().i("Stopping " + Initrfs.SHELLNAME);
			Initrfs.getLogwolf().i("Stopping all services");
			Initrfs.getPluginManager(tokenKey).stopServices();
			Initrfs.getLogwolf().i("Closing server socket");
			try {
				stdio.getSocket(tokenKey).close();
			} catch (IOException e) {
				// WHO EVEN CARES?
			}
			Initrfs.halt();
		}
	}
	
	@SuppressWarnings("unused")
	private void poweroff() {
		Initrfs.halt();
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
		stdio.println("User" + "\t" + "Pid" + "\t" +"Start" + "\t" + "Task Name");
		SimpleDateFormat ft = new SimpleDateFormat ("HH:mm");
		List<Task> tasks = Initrfs.getTaskManager(tokenKey).getRunningTasks();
		for(Task task : tasks) {
			stdio.println(task.getUser() + "\t" + 
					task.getPid() + "\t" + 
					ft.format(task.getDate()) + "\t" 
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
