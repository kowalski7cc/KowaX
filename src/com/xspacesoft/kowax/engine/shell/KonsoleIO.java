package com.xspacesoft.kowax.engine.shell;

import java.io.Closeable;
import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

import com.xspacesoft.kowax.engine.io.InputReader;
import com.xspacesoft.kowax.engine.io.OutputWriter;

public class KonsoleIO implements OutputWriter, InputReader, Closeable {
	
	private Scanner sc;
	private Console console;

	public KonsoleIO() {
		if((console = System.console())==null)
			sc = new Scanner(System.in);
	}
	
	public boolean isConsoleAvailable() {
		return console!=null?true:false;
	}
	
	public String readPassword() {
		return console!=null?new String(console.readPassword()):sc.nextLine();
	}

	@Override
	public String readString() {
		return console!=null?console.readLine():sc.nextLine();
	}

	@Override
	public boolean hasNextLine() {
		return console!=null?true:sc.hasNextLine();
	}

	@Override
	public Integer readInt() {
		return Integer.parseInt(sc.next());
	}

	@Override
	public Float readFloat() {
		return Float.parseFloat(sc.next());
	}

	@Override
	public Long readLong() {
		return Long.parseLong(sc.next());
	}

	@Override
	public Double readDouble() {
		return Double.parseDouble(sc.next());
	}

	@Override
	public Character readCharacter() {
		return sc.next().charAt(0);
	}

	@Override
	public void print(String string) {
		System.out.print(string);
	}

	@Override
	public void print(int i) {
		System.out.print(i);
	}

	@Override
	public void print(float f) {
		System.out.print(f);
	}

	@Override
	public void print(boolean b) {
		System.out.print(b);
	}

	@Override
	public void print(double d) {
		System.out.print(d);
	}

	@Override
	public void print(char c) {
		System.out.print(c);
	}

	@Override
	public void print(long l) {
		System.out.print(l);
	}

	@Override
	public void println() {
		System.out.println();
	}

	@Override
	public void println(String string) {
		System.out.println(string);
	}

	@Override
	public void println(int i) {
		System.out.println(i);
	}

	@Override
	public void println(float f) {
		System.out.println(f);
	}

	@Override
	public void println(boolean b) {
		System.out.println(b);
	}

	@Override
	public void println(double d) {
		System.out.println(d);
	}

	@Override
	public void println(char c) {
		System.out.println(c);
	}

	@Override
	public void println(long l) {
		System.out.println(l);
	}

	@Override
	public void close() throws IOException {
		sc.close();
	}

}
