package com.xspacesoft.kowax.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.xspacesoft.kowax.kernel.io.InputReader;
import com.xspacesoft.kowax.kernel.io.OutputWriter;

public class ShellIO implements OutputWriter, InputReader{
	
	private Socket socket;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private final char[] BACKSPACE = new char[]{'\b'};

	public ShellIO(Socket socket) throws IOException {
		this.socket = socket;
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	public ShellIO(BufferedReader in, PrintWriter out) {
		this.in = in;
		this.out = out;
	}
	
	public boolean isOpen() {
		if ((out!=null)&&(in!=null)&&(socket.isConnected())&&(!socket.isClosed()))
			return true;
		return false;
	}
	
	@Override
	public void print(String string) {
		if(out!=null)
			out.printf(string);
	}

	@Override
	public void print(int i) {
		if(out!=null)
			out.printf(Integer.toString(i));
	}

	@Override
	public void print(float f) {
		if(out!=null)
			out.printf(Float.toString(f));
	}

	@Override
	public void print(boolean b) {
		if(out!=null)
			out.printf(Boolean.toString(b));
	}

	@Override
	public void print(double d) {
		if(out!=null)
			out.printf(Double.toString(d));
	}

	@Override
	public void print(char c) {
		if(out!=null)
			out.printf(Character.toString(c));
	}

	@Override
	public void print(long l) {
		if(out!=null)
			out.printf(Long.toString(l));
	}

	@Override
	public void println() {
		if(out!=null)
			out.println();
	}

	@Override
	public void println(String string) {
		if(out!=null)
			out.println(string);
	}

	@Override
	public void println(int i) {
		if(out!=null)
			out.println(Integer.toString(i));
	}

	@Override
	public void println(float f) {
		if(out!=null)
			out.println(Float.toString(f));
	}

	@Override
	public void println(boolean b) {
		if(out!=null)
			out.println(Boolean.toString(b));
	}

	@Override
	public void println(double d) {
		if(out!=null)
			out.println(Double.toString(d));
	}

	@Override
	public void println(char c) {
		if(out!=null)
			out.println(Character.toString(c));
	}

	@Override
	public void println(long l) {
		if(out!=null)
			out.println(Long.toString(l));
	}

	@Override
	public String next() {
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

	@Override
	public String nextLine() {
		return next();
	}

	@Override
	public boolean hasNextLine() {
		try {
			return in.ready();
		} catch (IOException e) {
			return false;
		}
	}
	
	public String getRemoteAddress() {
		if(socket==null)
			return "localhost";
		return socket.getInetAddress().isLoopbackAddress() ?
				"localhost" : socket.getInetAddress().getHostAddress().toString();
	}

	@Override
	public Integer nextInt() {
		String s = next();
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Float nextFloat() {
		String s = next();
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Long nextLong() {
		String s = next();
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Double nextDouble() {
		String s = next();
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Character nextCharacter() {
		String s = next();
		if(s.length()>0)
			return s.charAt(0);
		return null;
	}
	
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

	public Socket getSocket() {
		return socket;
	}

	public BufferedReader getIn() {
		return in;
	}

	public PrintWriter getOut() {
		return out;
	}

}
