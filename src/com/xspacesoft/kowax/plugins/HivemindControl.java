package com.xspacesoft.kowax.plugins;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class HivemindControl extends ShellPlugin {
	
	public class HivemindHost {
		
		private String address;
		
		private Integer port;
		
		private HostRole hostRole;
		
		public HivemindHost(String address, Integer port, HostRole hostRole) {
			this.address = address;
			this.port = port;
			this.hostRole = hostRole;
		}

		public Integer getPort() {
			return port;
		}
		
		public void setPort(Integer port) {
			this.port = port;
		}
		
		public HostRole getRole() {
			return hostRole;
		}
		
		public void setRole(HostRole hostRole) {
			this.hostRole = hostRole;
		}
		
		public String getAddress() {
			return address;
		}
		
	}
	
	public class HivemindManager {
		
		private List<HivemindHost> hivemindHosts;
		private HivemindHost alpha;
		private long lastHeartbeat;

		private HostRole myRole;

		public HivemindManager() {
			hivemindHosts = new ArrayList<HivemindHost>();
			alpha = null;
			lastHeartbeat = 0;
		}
		
		public void addHost(HivemindHost hivemindHost) {
			hivemindHosts.add(hivemindHost);
		}
		
		public HivemindHost getHost(String address) {
			for (HivemindHost hivemindHost : hivemindHosts)
				if(hivemindHost.getAddress().equals(address))
					return hivemindHost;
			return null;
		}
		
		public void removeHost(String address) {
			for (HivemindHost hivemindHost : hivemindHosts)
				if(hivemindHost.getAddress().equals(address))
					hivemindHosts.remove(hivemindHost);
		}
		
		public void sendToHost(String string) {
			for (HivemindHost hivemindHost : hivemindHosts) {
				if ((hivemindHost.getRole() == HostRole.BETA)||(hivemindHost.getRole() == HostRole.GAMMA)) {
					try {
						Socket socket = new Socket(hivemindHost.getAddress(), hivemindHost.getPort());
						Stdio hostio = new Stdio(socket);
						hostio.println(string);
					} catch (IOException e) {
						
					}
				}
			}
		}
		
		public void sendToHostCascade(String string) {
			for (HivemindHost hivemindHost : hivemindHosts) {
				if ((hivemindHost.getRole() == HostRole.BETA)||(hivemindHost.getRole() == HostRole.GAMMA)) {
					try {
						Socket socket = new Socket(hivemindHost.getAddress(), hivemindHost.getPort());
						Stdio hostio = new Stdio(socket);
						hostio.println(string);
						hostio.println("Hive  run " + string);
					} catch (IOException e) {
						
					}
				}
			}
		}

		public List<HivemindHost> getHivemindHosts() {
			return hivemindHosts;
		}

		public void setHivemindHosts(List<HivemindHost> hivemindHosts) {
			this.hivemindHosts = hivemindHosts;
		}

		public HostRole getMyRole() {
			return myRole;
		}

		public void setMyRole(HostRole myRole) {
			this.myRole = myRole;
		}
		
		public HivemindHost getAlpha() {
			return alpha;
		}

		public void setAlpha(HivemindHost alpha) {
			this.alpha = alpha;
		}

		public long getLastHeartbeat() {
			return lastHeartbeat;
		}

		public void setLastHeartbeat(long lastHeartbeat) {
			this.lastHeartbeat = lastHeartbeat;
		}
		
	}
	
	public class HivemindService extends Thread {
		
		private HivemindManager hivemindManager;
		private boolean running;
		private final int MINUTES = 6;
		
		public HivemindService(HivemindManager hivemindManager) {
			this.hivemindManager = hivemindManager;
			running = false;
			this.setName("Hivemind service");
		}
		
		public void run() {
			running = true;
			while (running) {
				try {
					if (this.hivemindManager.getMyRole() == HostRole.ALPHA) {
						// TODO Heartbeat
					} else if (this.hivemindManager.getMyRole() == HostRole.BETA) {
						if ((System.currentTimeMillis() - this.hivemindManager.getLastHeartbeat()) > (60000*MINUTES)) {
							// Become the Alpha
							this.hivemindManager.setMyRole(HostRole.ALPHA);
						}
					}
					sleep(60000);
				} catch (InterruptedException e) {
					running = false;
				}
			}
		}
		
	}
	
	public enum HostRole {
		ALPHA,
		BETA,
		GAMMA,
		OMEGA,
	}

	private HivemindManager hivemindManager;
	
	@Override
	public String getAppletName() {
		return "Hive";
	}

	@Override
	public String getAppletVersion() {
		return "1.0A";
	}

	@Override
	public String getAppletAuthor() {
		return "kowalski";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		if (hivemindManager == null)
			hivemindManager = new HivemindManager();
		if (command.length()<1) {
			stdio.print(getHint());
			return;
		}
		String[] commands = command.split(" ");
		switch(commands[0].toLowerCase()) {
		case "role":
			String role;
			if (commands.length == 1) {
				stdio.print("Insert localhost role: ");
				role = stdio.scan();
				if(role.equalsIgnoreCase("c"))
					return;
			} else if (commands.length == 2) {
				role = commands[1];
			} else {
				stdio.print("Hive role (alpha|beta|gamma|omega)");
				return;
			}
			if(stringToRole(role)!=null) {
				hivemindManager.setMyRole(stringToRole(role));
				stdio.println("Current role: " + hivemindManager.getMyRole());
			} else {
				stdio.print("Hive role (alpha|beta|gamma|omega)");
			}
		case "add":
			String address3 = null;
			Integer port3 = null;
			HostRole role3 = null;
			if (commands.length == 1) {
				stdio.print("Insert host address: ");
				address3 = stdio.scan();
				if(address3.equalsIgnoreCase("c"))
					return;
				stdio.print("Insert host port: ");
				String bufferPort = stdio.scan();
				if(Initrfs.isNumber(bufferPort))
					port3 = Initrfs.parseInt(bufferPort);
				else {
					stdio.println("Invalid port. Usage 'hive add address port (alpha|beta|gamma|omega)'");
					return;
				}
				stdio.print("Insert host role: ");
				String bufferRole = stdio.scan();
				if(bufferRole.equalsIgnoreCase("c"))
					return;
				if (stringToRole(bufferRole)==null) {
					stdio.println("Invalid role. Usage 'hive add address port (alpha|beta|gamma|omega)'");
					return;
				} else {
					role3 = stringToRole(bufferRole);
				}
			} else if (commands.length == 4) {
				address3 = commands[1];
				if(Initrfs.isNumber(commands[2])) {
					port3 = Initrfs.parseInt(commands[2]);
				} else {
					stdio.println("Invalid port. Usage 'hive add address port (alpha|beta|gamma|omega)'");
					return;
				}
				if(stringToRole(commands[3])==null) {
					role3 = stringToRole(commands[3]);
				} else {
					stdio.println("Invalid role. Usage 'hive add address port (alpha|beta|gamma|omega)'");
					return;
				}
			} else {
				stdio.println("Invalid port. Usage 'hive add address port (alpha|beta|gamma|omega)'");
				return;
			}
			hivemindManager.addHost(new HivemindHost(address3, port3, role3));
			stdio.println("Host " + address3 + " added");
			break;
		case "remove":
			String address = null;
			if (commands.length == 1) {
				stdio.print("Insert host address: ");
				address = stdio.scan();
				if(address.equalsIgnoreCase("c"))
					return;
			} else if (commands.length == 2) {
				address = commands[1];
			}
			if (hivemindManager.getHost(address) == null) {
				stdio.println("Unknown host");
			} else {
				hivemindManager.removeHost(address);
				stdio.println("Host " + address + " removed");
			}
			break;
		case "view":
			String address2 = null;
			if (commands.length == 1) {
				stdio.print("Insert host address: ");
				address = stdio.scan();
				if(address.equalsIgnoreCase("c"))
					return;
			} else if (commands.length == 2) {
				address = commands[1];
			}
			if (hivemindManager.getHost(address2) == null) {
				stdio.println("Unknown host");
			} else {
				HivemindHost host = hivemindManager.getHost(address2);
				stdio.println("Host address: " + host.getAddress());
				stdio.println("Host port: " + host.getPort());
				stdio.println("Host role: " + host.getRole().toString().toLowerCase());
			}
			break;
		case "heartbeat":
			hivemindManager.setLastHeartbeat(System.currentTimeMillis());
			break;
		case "run":
			if (commands.length < 2) {
				stdio.println(getHint());
			} else {
				hivemindManager.sendToHost(command.substring(commands[0].length()));
			}
			break;
		case "runcascade":
			if (commands.length < 2) {
				stdio.println(getHint());
			} else {
				hivemindManager.sendToHostCascade(command.substring(commands[0].length()));
			}
			break;
		default: stdio.println(getHint());
			break;
		}
		
	}

	@Override
	public String getDescription() {
		return "Plugin for multi-host control";
	}

	@Override
	public String getHint() {
		return "Hive (role|add|remove|view|run|runcascade)";
	}

	private HostRole stringToRole(String string) {
		switch(string.toLowerCase()) {
		case "alpha": return HostRole.ALPHA;
		case "beta": return HostRole.BETA;
		case "gamma": return HostRole.GAMMA;
		case "omega": return HostRole.OMEGA;
		default: return null;
		}
	}
}
