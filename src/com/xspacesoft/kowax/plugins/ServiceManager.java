package com.xspacesoft.kowax.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.apis.Service;
import com.xspacesoft.kowax.exceptions.InsufficientPermissionsException;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.SystemApi;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TaskManager.Task;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.shell.CommandRunner;

public class ServiceManager extends PluginBase implements KernelAccess {

	private TokenKey tokenKey;
	private List<String> blacklist;
	
	public ServiceManager() {
		blacklist = new ArrayList<String>();
		blacklist.addAll(Arrays.asList(new String[] {
			"KInit",
		}));
	}
	
	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}

	@Override
	public String getAppletName() {
		return "Service";
	}

	@Override
	public String getAppletVersion() {
		return "1.0a";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski";
	}

	@Override
	protected void runApplet(String command, Stdio stdio,
			CommandRunner commandRunner) {
		if(command==null) {
			stdio.println(getHint());
			return;
		}
		String[] commands = command.split(" ");
		switch(commands[0].toLowerCase()) {
		case "start":
			if(commands.length>1) {
				List<Task> tasks = ((TaskManager) Initrfs.getSystemApi(SystemApi.TASK_MANAGER, tokenKey)).getRunningTasks();
				for(Task task : tasks) {
					if(task.getService()!=null) {
						if(task.getService().getServiceName().equalsIgnoreCase(commands[1])) {
							stdio.println("Service already running");
							return;
						}
					}
				}
				List<Service> services = ((PluginManager) Initrfs.getSystemApi(SystemApi.PLUGIN_MANAGER, tokenKey)).getServices();
				for(Service service : services) {
					if(service.getServiceName().equalsIgnoreCase(commands[1])) {
						service.startService();
						((TaskManager) Initrfs.getSystemApi(SystemApi.TASK_MANAGER, tokenKey)).newTask("root", service.getServiceName(), service);
						stdio.println("Service started");
						return;
					}
				}
				stdio.println("Can't find requested service");
			}
			break;
		case "stop":
			for(String blacklisted : blacklist) {
				if(blacklisted.equalsIgnoreCase(commands[1])) {
					if(commandRunner.isSudo()) {
						stdio.println("You shouldn't stop this process");
					} else {
						stdio.println("You can't stop this process");
						return;
					}
				}
			}
			TaskManager taskManager =(TaskManager) Initrfs.getSystemApi(SystemApi.TASK_MANAGER, tokenKey); 
			List<Task> tasks = taskManager.getRunningTasks();
			for(Task task : tasks) {
				if(task.getService()!=null) {
					if(task.getService().getServiceName().equalsIgnoreCase(commands[1])) {
						task.getService().stopService();
						taskManager.removeTask(task.getPid());
						stdio.println("Service stopped");
						return;
					}
				}
			}
			stdio.println("Can't find requested service");
			break;
		case "list":
			List<Service> services = ((PluginManager) Initrfs.getSystemApi(SystemApi.PLUGIN_MANAGER, tokenKey)).getServices();
			stdio.println("All services:");
			for(Service service : services) {
				stdio.println(service.getServiceName());
			}
		case "":
			break;
		}
	}

	@Override
	public String getDescription() {
		return "Manage services";
	}

	@Override
	public String getHint() {
		return "Usage: service (start|stop (servicename))";
	}

	public void addBlacklist(String service, TokenKey tokenKey) {
		if(Initrfs.isTokenValid(tokenKey)) {
			blacklist.add(service);
		} else {
			throw new InsufficientPermissionsException();
		}
	}
	
}
