package com.xspacesoft.kowax;

import java.io.IOException;
import java.util.Locale;

public class Pause {
	
	private Locale locale;

	public Pause(Locale locale) {
		this.locale = locale;
	}
	
	public Pause() {
		locale = null;
	}

	public String showPause() throws IOException {
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
		return output;
	}
}
