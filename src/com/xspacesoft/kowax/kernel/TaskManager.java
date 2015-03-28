package com.xspacesoft.kowax.kernel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TaskManager {
	
	public class Task {

		private String user;
		private int pid;
		private String applet;
		private Date date;
		
		public Task(String user, int pid, String applet) {
			super();
			this.user = user;
			this.pid = pid;
			this.applet = applet;
			date = new Date();
		}

		public String getUser() {
			return user;
		}

		public int getPid() {
			return pid;
		}

		public String getAppletName() {
			return applet;
		}

		public Date getDate() {
			return date;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public void setAppletName(String applet) {
			this.applet = applet;
		}
		
		
		
	}
	
	private List<Task> tasks;
	private int lastPid;
	
	public TaskManager() {
		tasks = new ArrayList<Task>();
	}
	
	public int newTask(String user, String appletName) {
		tasks.add(new Task(user, lastPid, appletName));
		return lastPid++;
	}
	
	public Task getTask(int pid) {
		for(Task task : tasks)
			if(task.getPid() == pid)
				return task;
		return null;
	}
	
	public void editTask(int pid, String appletName) {
		for(Task task : tasks)
			if(task.getPid() == pid) {
				task.setAppletName(appletName);
			}
	}
	
	public void removeTask(int pid) {
		if(pid<=1)
			return;
		Iterator<Task> ite = tasks.iterator();
		while(ite.hasNext()) {
			Task task = ite.next();
			if(task.getPid() == pid) {
				ite.remove();
			}
		}
	}
	
	public ArrayList<Task> getRunningTasks() {
		return new ArrayList<Task>(this.tasks);
	}
	
//	public void printTasks() {
//		Stdio stdout = HivemindServer.stdout;
//		stdout.println("Running processes:");
//		stdout.println("User      Pid       Start      Task Name");
//		SimpleDateFormat ft = new SimpleDateFormat ("HH:mm");
//		for(Task task : tasks) {
//			stdout.println(task.getUser() + "      " + task.getPid() + "      " + ft.format(task.getDate()) + "      " + task.getTaskName());
//		}
//	}
}
