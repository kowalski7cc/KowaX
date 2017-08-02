package com.xspacesoft.kowax.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.apis.Service;
import com.xspacesoft.kowax.exceptions.InsufficientPermissionsException;
import com.xspacesoft.kowax.exceptions.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.PluginManager;
import com.xspacesoft.kowax.kernel.SystemApi;
import com.xspacesoft.kowax.kernel.TaskManager;
import com.xspacesoft.kowax.kernel.TaskManager.Task;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.io.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.shell.CommandRunner.CommandNotFoundException;

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
				StringBuilder serviceName = new StringBuilder();
				for(int i = 1; i<commands.length;i++) {
					serviceName.append(commands[i]+(i+1>=commands.length?"":" "));
				}
				List<Task> tasks = ((TaskManager) Core.getSystemApi(SystemApi.TASK_MANAGER, tokenKey)).getRunningTasks();
				for(Task task : tasks) {
					if(task.getService()!=null) {
						if(task.getService().getServiceName().equalsIgnoreCase(serviceName.toString())) {
							stdio.println("Service already running");
							return;
						}
					}
				}
				List<Service> services = ((PluginManager) Core.getSystemApi(SystemApi.PLUGIN_MANAGER, tokenKey)).getServices();
				for(Service service : services) {
					if(service.getServiceName().equalsIgnoreCase(serviceName.toString())) {
						service.startService();
						((TaskManager) Core.getSystemApi(SystemApi.TASK_MANAGER, tokenKey)).newTask("root", service.getServiceName(), service);
						stdio.println("Service started");
						return;
					}
				}
				stdio.println("Can't find requested service");
			} else {
				stdio.print("Insert service name: ");
				String serviceName = stdio.readString();
				try {
					commandRunner.run(getAppletName() + " start " + serviceName);
				} catch (IllegalArgumentException | CommandNotFoundException | MissingPluginCodeException e) {

				}
			}
			break;
		case "stop":
			if(commands.length>1) {
				StringBuilder serviceName = new StringBuilder();
				for(int i = 1; i<commands.length;i++) {
					serviceName.append(commands[i]+(i+1>=commands.length?"":" "));
				}
				for(String blacklisted : blacklist) {
					if(blacklisted.equalsIgnoreCase(serviceName.toString())) {
						if(commandRunner.isSudo()) {
							stdio.println("You shouldn't stop this process");
						} else {
							stdio.println("You can't stop this process");
							return;
						}
					}
				}
				TaskManager taskManager =(TaskManager) Core.getSystemApi(SystemApi.TASK_MANAGER, tokenKey); 
				List<Task> tasks = taskManager.getRunningTasks();
				for(Task task : tasks) {
					if(task.getService()!=null) {
						if(task.getService().getServiceName().equalsIgnoreCase(serviceName.toString())) {
							task.getService().stopService();
							taskManager.removeTask(task.getPid());
							stdio.println("Service stopped");
							return;
						}
					}
				}
				stdio.println("Can't find requested service");
			} else {
				stdio.print("Insert service name: ");
				String serviceName = stdio.readString();
				try {
					commandRunner.run(getAppletName() + " stop " + serviceName);
				} catch (IllegalArgumentException | CommandNotFoundException | MissingPluginCodeException e) {

				}
			}
			break;
		case "list":
			List<Service> services = ((PluginManager) Core.getSystemApi(SystemApi.PLUGIN_MANAGER, tokenKey)).getServices();
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
		return "Usage: service (start (servicename)|stop (servicename)|list)";
	}

	public void addBlacklist(String service, TokenKey tokenKey) {
		if(Core.isTokenValid(tokenKey)) {
			blacklist.add(service);
		} else {
			throw new InsufficientPermissionsException();
		}
	}

}
