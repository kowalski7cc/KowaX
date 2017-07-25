package com.xspacesoft.kowax.shell;

import java.io.Closeable;
import java.io.IOException;
import java.util.Scanner;

import com.xspacesoft.kowax.kernel.io.InputReader;
import com.xspacesoft.kowax.kernel.io.OutputWriter;

public class ConsoleIO implements OutputWriter, InputReader, Closeable {
	
	private Scanner sc;

	public ConsoleIO() {
		sc = new Scanner(System.in);
	}

	@Override
	public String next() {
		return sc.next();
	}

	@Override
	public String nextLine() {
		return sc.nextLine();
	}

	@Override
	public boolean hasNextLine() {
		return sc.hasNextLine();
	}

	@Override
	public Integer nextInt() {
		return Integer.parseInt(sc.next());
	}

	@Override
	public Float nextFloat() {
		return Float.parseFloat(sc.next());
	}

	@Override
	public Long nextLong() {
		return Long.parseLong(sc.next());
	}

	@Override
	public Double nextDouble() {
		return Double.parseDouble(sc.next());
	}

	@Override
	public Character nextCharacter() {
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
