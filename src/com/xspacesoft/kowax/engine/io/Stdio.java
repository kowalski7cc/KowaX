package com.xspacesoft.kowax.engine.io;

import java.io.IOException;

import com.xspacesoft.kowax.Pause;

public class Stdio {

	private OutputWriter outputWriter;
	private InputReader inputReader;

	public <T extends OutputWriter & InputReader> Stdio(T t) throws IOException {
		this.inputReader = (InputReader) t;
		this.outputWriter = (OutputWriter) t;
	}

	public Stdio(OutputWriter outputWriter, InputReader inputReader) throws IOException {
		this.inputReader = inputReader;
		this.outputWriter = outputWriter;
	}

	public Stdio() {
		outputWriter = null;
		inputReader = null;
	}

	public OutputWriter getOutputWriter() {
		return outputWriter;
	}

	public void setOutputWriter(OutputWriter outputWriter) {
		this.outputWriter = outputWriter;
	}

	public InputReader getInputReader() {
		return inputReader;
	}

	public void setInputReader(InputReader inputReader) {
		this.inputReader = inputReader;
	}

	public void println(String message) {
		if(outputWriter!=null)
			outputWriter.println(message);
	}

	public void printTitle(String title) {
		if(outputWriter!=null) {
			println(title);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < title.length(); i++) {
				sb.append("-");
			}
			println(sb.toString());
		}
	}


	public void println() {
		if(outputWriter!=null)
			outputWriter.println();
	}

	public void print(String message) {
		if(outputWriter!=null)
			outputWriter.print(message);
	}

	public String readString() {
		if(inputReader!=null)
			return inputReader.readString();
		return null;
	}

	public Integer readInt() {
		if(inputReader!=null)
			return inputReader.readInt();
		return null;
	}

	public Float readFloat() {
		if(inputReader!=null)
			return inputReader.readFloat();
		return null;
	}

	public Character readCharacter() {
		if(inputReader!=null)
			return inputReader.readCharacter();
		return null;
	}

	public void clear() {
		print("\u001B[2J");
	}

	public void reverse() {
		print("\u001B[7m");
	}

	public void pause() throws IOException {
		Pause pause = new Pause();
		outputWriter.print(pause.showPause());
		inputReader.readString();
	}

	public static boolean isNumber(char character) {
		if((character>='0')&&(character<='9')) {
			return true;
		}
		return false;
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
