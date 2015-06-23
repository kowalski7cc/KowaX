package com.xspacesoft.kowax;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.prefs.Preferences;

import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.UsersManager;

public class SetupWizard {
	
	private Preferences preferences;
	private PrintWriter out;
	private InputStream in;
	private String username;
	private String password;
	
	private List<String> defaultUsers = UsersManager.getDefaultUsers();
	
	public SetupWizard(Preferences preferences, PrintWriter out, InputStream in) {
		this.preferences = preferences;
		this.out = out;
		this.in = in;
	}
	
	public void start() {
		configTitle("Introduction", 1, 5);
		out.println("Welcome to " + Core.SHELLNAME + "!");
		out.println("This wizard will allow you to set up your server easily.");
		out.println("It will be asked you the ports that will be used by " + Core.SHELLNAME);
		out.println("for it's services.");
		out.println();
		out.println("Press ENTER when you are ready.");
		Scanner scn = new Scanner(in);
		scn.nextLine();
		configTitle("Port configuration", 2, 5);
		int telnet = portConfiguration("telnet", Stdio.parseInt(BuildGet.getString("default.telnet")), scn);
		int http = portConfiguration("http", Stdio.parseInt(BuildGet.getString("default.http")), scn);
		configTitle("Home directory", 3, 5);
		String homePath = homeConfiguration(Stdio.parseInt(BuildGet.getString("default.directory")), scn);
		configTitle("User informations", 3, 5);
		String username = getUserInfo("username", scn);
		while(exitsUser(username)) {
			out.println("User already exists.");
			username = getUserInfo("username", scn);
		}
		String password = getUserInfo("password", scn);
		configTitle("Other informations", 4, 5);
		boolean forceDebug = getBoolean("Force debug output mode?", 
				BuildGet.stringToBoolean(BuildGet.getString("default.force.debug")), true,scn);
		boolean forceVerbose = getBoolean("Force verbose output mode?", 
				BuildGet.stringToBoolean(BuildGet.getString("default.force.verbose")), true, scn);
		boolean autostartHttp = getBoolean("Autostart Http server?", 
				BuildGet.stringToBoolean(BuildGet.getString("default.start.http")), true,scn);
		boolean eula = getBoolean("Do you accept EULA?", 
				BuildGet.stringToBoolean(BuildGet.getString("default.accept.eula")), false,scn);
		if(!eula) {
			out.println("Without accepting eula you can't continue. Press ENTER to exit.");
			scn.nextLine();
			System.exit(0);
		}
		configTitle("Saving configuration", 5, 5);
		preferences.putInt("telnet_port", telnet);
		preferences.putInt("http_port", http);
		preferences.put("home_path", homePath);
		preferences.putBoolean("force_debug", forceDebug);
		preferences.putBoolean("force_verbose", forceVerbose);
		preferences.putBoolean("autostart_http", autostartHttp);
		this.username = username;
		this.password = password;
		configTitle("Setup complete", 5, 5);
		out.println("Thank you for choosing us!");
		out.println("Enjoy using " + Core.SHELLNAME);
		out.println("Press ENTER to start system");
		scn.nextLine();
		scn.close();
	}

	private boolean exitsUser(String username) {
		for(String user : defaultUsers)
			if(user.equalsIgnoreCase(username))
				return true;
		return false;
	}

	private void configTitle(String hint, int step, int max) {
		Core.clear(System.out);
		String title = "KowaX " + Core.VERSION + " setup"; 
		out.println(title);
		for(int i=0; i<title.length()+5; i++) {
			out.printf("-");
		}
		out.println();
		out.println(hint + " (" + step + " of " + max + ")");
		out.println();
	}
	
	private int portConfiguration(String portName, int defaultValue, Scanner scn) {
		String inBuffer;
		out.printf("Insert port for " + portName + " server[" + defaultValue +"]: ");
		while((inBuffer = scn.nextLine())==null) {
			inBuffer = scn.nextLine();
			while(!((Stdio.isNumber(inBuffer))||(inBuffer.equals(""))
					||(Stdio.parseInt(inBuffer)>65535)||(Stdio.parseInt(inBuffer)<1))) {
				out.println("Invalid port");
				out.printf("Insert port for " + portName + " server[" + defaultValue +"]: ");
				while((inBuffer = scn.nextLine())==null) {
					inBuffer = scn.nextLine();
				}
			}
		}
		if(inBuffer.equals(""))
			inBuffer = defaultValue + "";
		int value = Stdio.parseInt(inBuffer);
		return value;
	}
	
	private String homeConfiguration(int def, Scanner scn) {
		String inBuffer;
		out.println("Select home directory path: ");
		out.println("1) User home directory");
		out.println("2) " + Core.SHELLNAME + " directory");
		out.println("3) Custom path (NA)");
		out.println();
		out.printf("Your choice [1]: ");
		while((inBuffer = scn.nextLine())==null) {
			inBuffer = scn.nextLine();
			while((!Stdio.isNumber(inBuffer))&&(!inBuffer.equals(""))&&(Stdio.parseInt(inBuffer)<=3)&&(Stdio.parseInt(inBuffer)>=1)) {
				out.println("Invalid choice");
				out.printf("Your choice [1]: ");
				while((inBuffer = scn.nextLine())==null) {
					inBuffer = scn.nextLine();
				}
			}
		}
		if(inBuffer.equals("")||inBuffer.startsWith("1")) {
			File home = new File(new File(System.getProperty("user.home")), "KowaX");
			home.mkdirs();
			return home.getAbsolutePath();
		} else if(inBuffer.startsWith("2")) {
			File home = new File(new File(""), "KowaX");
			home.mkdirs();
			return home.getAbsolutePath();
		} else {
			return new File("").getAbsolutePath();
		}
	}

	private String getUserInfo(String quest, Scanner scn) {
		String inBuffer;
		out.printf("Enter your " + quest + ": ");
		while((inBuffer = scn.nextLine()).equals("")) {
			out.println("Invalid " + quest);
			out.printf("Enter your " + quest + ": ");
		}
		return inBuffer;
	}
	
	private boolean getBoolean(String quest, boolean defValue, boolean skippable, Scanner scn) {
		String inBuffer;
		out.printf(quest + " [" + (defValue  ? "yes" : "no") + "]: ");
		inBuffer = scn.nextLine();
		if(skippable)
			while(!(inBuffer.equalsIgnoreCase("yes")||inBuffer.equalsIgnoreCase("y")||inBuffer.equals("")
					||inBuffer.equalsIgnoreCase("no")||inBuffer.equalsIgnoreCase("n"))) {
				out.println("Invalid answer");
				out.printf(quest + " [" + (defValue  ? "yes" : "no") + "]: ");
				inBuffer = scn.nextLine();
			}
		else
			while(!(inBuffer.equalsIgnoreCase("yes")||inBuffer.equalsIgnoreCase("y")||inBuffer.equalsIgnoreCase("no")||inBuffer.equalsIgnoreCase("n"))) {
				out.println("Invalid answer");
				out.printf(quest + " [" + (defValue  ? "yes" : "no") + "]: ");
				inBuffer = scn.nextLine();
			}
		if(skippable&&inBuffer.equals(""))
			inBuffer = (defValue  ? "yes" : "no");
		if(inBuffer.startsWith("y"))
			return true;
		else
			return false;
	}
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}