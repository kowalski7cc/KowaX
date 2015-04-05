package com.xspacesoft.kowax.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.xspacesoft.kowax.kernel.KernelAccess;
import com.xspacesoft.kowax.kernel.MissingPluginCodeException;
import com.xspacesoft.kowax.kernel.Service;
import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.shell.CommandRunner.CommandNotFoundException;
import com.xspacesoft.kowax.shell.Session;

public class CronTab extends ShellPlugin implements Service, KernelAccess {
	
	public class Job implements Serializable {

		private static final long serialVersionUID = 2691864417112435327L;
		private String command;
		private Boolean sudo;
		private Integer timing;
		
		public Job(String command, Boolean sudo, Integer timing) {
			this.command = command;
			this.sudo = sudo;
			this.timing = timing;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}

		public Integer getTiming() {
			return timing;
		}

		public void setTiming(Integer timing) {
			this.timing = timing;
		}

		public Boolean isSudo() {
			return sudo;
		}
		
	}
	
	public class CronTabJob {
		
		private List<Job> jobs;
		private Integer sleep;
		
		public CronTabJob() {
			jobs = new ArrayList<Job>();
		}
		
		public void addJob(String command, boolean sudo, int timing) {
			jobs.add(new Job(command, sudo, timing));
		}
		
		public void removeJob(String command) {
			for (Job job : jobs)
				if (job.getCommand().equals(command))
					jobs.remove(job);
		}
		
		public Job[] getJobs() {
			Job jbs[] = new Job[jobs.size()];
			return jobs.toArray(jbs);
		}

		public Integer getSleep() {
			return sleep;
		}

		public void setSleep(Integer sleep) {
			this.sleep = sleep;
		}
		
	}

	public class Cron extends Thread {
		
		private CronTabJob job;
		private boolean running;
		private int sleep;
		private CommandRunner commandRunner;
		private Session session;
	
		public Cron(CronTabJob job, Integer sleep, CommandRunner commandRunner, Session session) {
			this.job = job;
			this.commandRunner = commandRunner;
			this.session = session;
			this.running = true;
			if(sleep==null)
				sleep=10000;
			this.sleep = sleep;
			this.setName("CronTab worker - Idle");
			job.setSleep(sleep);
		}
		
		public void run() {
			while(running) {
				this.sleep = job.getSleep();
				try {
					this.setName("CronTab worker - Running");
					doJobs();
					this.setName("CronTab worker - Idle");
					sleep(sleep);
				} catch (InterruptedException e) {
					running = false;
				}
			}
		}

		private void doJobs() {
			Job[] jobs = job.getJobs();
			for (Job job : jobs) {
				session.setSudo(job.isSudo());
				try {
					commandRunner.run(job.getCommand());
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CommandNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MissingPluginCodeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private Cron cron;
	private CronTabJob job;
	private TokenKey tokenKey;

	protected CronTabJob getJob() {
		return job;
	}
	
	protected Integer getSleep() {
		return job.getSleep();
	}

	protected void setSleep(Integer sleep) {
		job.setSleep(sleep);
	}

	@Override
	public Boolean isServiceRunning() {
		if(cron != null)
			return cron.isAlive();
		return false;
	}

	@Override
	public void startService() {
		if (cron==null){
			CronTabJob job = new CronTabJob();
			Stdio stdio = new Stdio();
			Session session = new Session(stdio);
			session.setAuthenticated(true);
			session.setSudo(true);
			session.setUsername("root");
			CommandRunner commandRunner = new CommandRunner(tokenKey, true);
			cron = new Cron(job, null, commandRunner, session);
		}
		if (!cron.isAlive())
			cron.start();
	}

	@Override
	public void stopService() {
		if((cron!=null)&&(cron.isAlive()))
			cron.interrupt();
	}
	
	@Override
	public String getServiceName() {
		return "Cron";
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		this.tokenKey = tokenKey;
	}

	@Override
	public String getAppletName() {
		return "cron";
	}

	@Override
	public String getAppletVersion() {
		return "2.0A";
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
		return null;
	}

	@Override
	public String getHint() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
