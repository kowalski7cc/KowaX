package com.xspacesoft.kowax.kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.Pause;

public class Stdio {

	private Socket socket;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private final char[] BACKSPACE = new char[]{'\b'};

	public Stdio(Socket socket) throws IOException {
		this.socket = socket;
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public Stdio(BufferedReader in, PrintWriter out) {
		this.in = in;
		this.out = out;
	}
	
	public Stdio() {
		this.socket = null;
	}
	
	public boolean isOpen() {
		if ((out!=null)&&(in!=null)&&(socket.isConnected())&&(!socket.isClosed()))
			return true;
		return false;
	}
	
	public void println(String message) {
		if(out!=null)
			out.println(message);
	}
	
	public void printTitle(String title) {
		if(out!=null) {
			this.println(title);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < title.length(); i++) {
				sb.append("-");
			}
			this.println(sb.toString());
		}
	}
	
	public void println() {
		if(out!=null)
			out.println();
	}

	public void print(String message) {
		if(out!=null)
			out.printf(message);
	}

	public String scan() {
		if(in==null)
			return null;
		try {
			String recived = in.readLine();
			if(recived == null) {
				socket.close();
				return "exit";
			}
			if(recived.split(" ").length<1) {
				return "";
			}
			if(recived.contains(new String(BACKSPACE))){
				return removeBackspace(recived);
			}
			return recived;
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) { }
			return null;
		}
	}
	
//	public String scanPassword() {
//		out.printf("\b");
//		String input = scan();
//		return input;
//	}

	private String removeBackspace(String recived) {
		boolean clean = false;
		while(!clean) {
			if (recived.length()==0) {
				clean = true;
				break;
			}
			while(recived.startsWith(new String(BACKSPACE))) {
				recived = recived.substring(1);
			}
			for(int i = 0; i < recived.length(); i++) {
				while(recived.startsWith(new String(BACKSPACE))) {
					recived = recived.substring(1);
				}
				if(i<0) {
					i=0;
				}
				if(recived.charAt(i)==BACKSPACE[0]) {
					if(recived.length()==2) {
						recived = "";
					} else if(recived.length()==(i+1)) {
						recived = recived.substring(0, i-1);
						i-=2;
					} else {
						if((i-1)<0) {
							recived = recived.substring(i+1);
						} else {
							recived = recived.substring(0, i-1) + recived.substring(i+1, recived.length());
							i-=2;
						}
					}
				}
			}
			if(!recived.contains(new String(BACKSPACE)))
				clean = true;
		}
		return recived;
	}

	public Socket getSocket(TokenKey tokenKey) {
		if(Initrfs.isTokenValid(tokenKey))
			return socket;
		return null;
	}
	
	public void clear() {
		print("\u001B[2J");
	}
	
	public void reverse() {
		print("\u001B[7m");
	}
	
	public String getRemoteAddress() {
		if(socket==null)
			return "localhost";
		return socket.getInetAddress().isLoopbackAddress() ?
				"localhost" : socket.getInetAddress().getHostAddress().toString();
	}
	
	public void pause() throws IOException {
		Pause pause = new Pause(this.in, this.out);
		pause.showPause();
	}
	
	public static boolean isNumber(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static int parseInt(String string) {
		try {
			int i = Integer.parseInt(string);
			return i;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
