package com.xspacesoft.kowax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

public class Pause {
	
	private Locale locale;
	private InputStream inputStream;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;

	public Pause(Locale locale, InputStream inputStream, OutputStream outputStream) {
		this.locale = locale;
		this.inputStream = inputStream;
		this.printWriter = new PrintWriter(outputStream, true);
	}
	
	public Pause(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.printWriter = new PrintWriter(outputStream, true);
	}
	
	public Pause(BufferedReader bufferedReader, PrintWriter printWriter) {
		this.bufferedReader = new BufferedReader(bufferedReader);
		this.printWriter = new PrintWriter(printWriter,true);
	}

	public void showPause() throws IOException {
		String output = "Hello!";
		if(locale==null)
			locale = Locale.getDefault();
		switch (locale.getLanguage()) {
		case "it": output = "Premi enter per continuare...";
			break;
		case "en": output = "Press enter to continue...";
			break;
		case "fr": output = "Appuyez sur Entrée pour continuer...";
			break;
		case "es": output = "Pulse Intro para continuar...";
			break;
		case "de": output = "Drücken Sie die Eingabetaste, um fortzufahren";
			break;
		case "hu": output = "Nyomja meg az Entert a folytatáshoz";
			break;
		case "pl": output = "Naciśnij klawisz Enter, aby kontynuować";
			break;
		default: output = "Press enter to continue...";
		}
		printWriter.printf(output);
		if (bufferedReader!=null)
			bufferedReader.readLine();
		else 
			inputStream.read();
	}
}
