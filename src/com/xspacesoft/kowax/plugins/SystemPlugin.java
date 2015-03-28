package com.xspacesoft.kowax.plugins;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.KernelAccess;
import com.xspacesoft.kowax.kernel.TaskManager.Task;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.shell.CommandRunner;

public class SystemPlugin extends ShellPlugin implements KernelAccess {
	
	private TokenKey tokenKey;

	@Override
	public String getAppletName() {
		return "system";
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
			shutdown();
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
			if(job.length>1)
				makeAlias(command.substring(job[0].length()+1), commandRunner);
			else
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
			Initrfs.getAliasManager(tokenKey).newAlias(a[0], a[1]);
			commandRunner.getSocketHelper().println("Alias created");
		}
	}

	private void listApplets() {
		// TODO Auto-generated method stub
		
	}

	private void showAbout(Stdio stdio) {
		stdio.println("Project Security by Kowalski7cc. Copyright XSpaceSoft 2009-2015.");
		stdio.println();
	}

	private void showEula(Stdio stdio) {
		stdio.println("End-User License Agreement for Project Security");
		try {
			InputStream is = this.getClass().getResourceAsStream("eula.txt");
			Scanner scn = new Scanner(is);
			while(scn.hasNext()) {
				stdio.println(scn.nextLine() + "");
			}
			scn.close();
		} catch (Exception e) {
			
		}
		stdio.println();
	}

	private void shutdown() {
		// TODO Auto-generated method stub
		
	}

	private void garbageCollector() {
		// TODO Auto-generated method stub
		
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
		stdio.println("User      Pid       Start      Task Name");
		SimpleDateFormat ft = new SimpleDateFormat ("HH:mm");
		List<Task> tasks = Initrfs.getTaskManager(tokenKey).getRunningTasks();
		for(Task task : tasks) {
			stdio.println(task.getUser() + "      " + task.getPid() + "      " + ft.format(task.getDate()) + "      " + task.getAppletName());
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
