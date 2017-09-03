package com.xspacesoft.kowax.kernel.io;

public interface OutputWriter {

	public void print(String string);
	
	public void print(int i);
	
	public void print(float f);
	
	public void print(boolean b);
	
	public void print(double d);
	
	public void print(char c);
	
	public void print(long l);
	
	public void println();
	
	public void println(String string);
	
	public void println(int i);
	
	public void println(float f);
	
	public void println(boolean b);
	
	public void println(double d);
	
	public void println(char c);
	
	public void println(long l);
	

}
