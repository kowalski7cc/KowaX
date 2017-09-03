package com.xspacesoft.kowax.plugins;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.xspacesoft.kowax.PomParser;
import com.xspacesoft.kowax.engine.PluginBase;
import com.xspacesoft.kowax.engine.io.Stdio;
import com.xspacesoft.kowax.engine.shell.CommandRunner;

public class Screenfetch extends PluginBase {
	
	private Properties systemProperties = PomParser.load();
	private String logo[] = {
			"               .++++++.               ",
			"         N./++:/++++++/:++/+N         ",
			"      N.++++...........+++//.+.N      ",
			"    N+.++.............+++++++++.+N    ",
			"   +`/..++++..........+++++++..+/`+   ",
			"  +`...+++++++........++++.+.....+`+  ",
			" . +...+++++++.......++:........... . ",
			" `+......++++.+..++++++............+` ",
			"+`.............+......+............+`+",
			":.............+........+.....+++.....:",
			":....++.....+.+........+.+++++++++...:",
			"+`.++++++++++..+......+.....++++++.+`+",
			" `+++++++........++++.........++...+` ",
			" . .++++...........++.............. . ",
			"  +`+..............++++..........+`.  ",
			"   +`/+...........++++++.......+/`+   ",
			"    N+.++.........++++++.....+/.+N    ",
			"      N.+.++.........+....++.+.N      ",
			"         N++++:/++++++/:++++N         ",
			"              .++++++.                "
	};

	public Screenfetch() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getAppletName() {
		return "Screenfetch";
	}

	@Override
	public String getAppletVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAppletAuthor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void runApplet(String[] command, Stdio stdio, CommandRunner commandRunner) {
		List<String> data = getData();
		Iterator<String> idata = data.iterator();
		for(String string : logo) {
			stdio.println(string + " " + (((idata!= null)&&(idata.hasNext()))?idata.next():""));
		}
		stdio.println();
	}
	
	private List<String> getData() {
		List<String> data = new LinkedList<String>();
		data.add("OS: " + systemProperties.getProperty("artifactId","KowaX") + " " 
				+ systemProperties.getProperty("version","test build"));
		data.add("Host OS: " + System.getProperty("os.name"));
		data.add("Java version: " + System.getProperty("java.version"));
		data.add("Maximum memory: " + Runtime.getRuntime().maxMemory());
		data.add("CPU: " + System.getProperty("sun.cpu.isalist")
				+ ", "
				+ Runtime.getRuntime().availableProcessors()
				+ " cores");
		data.add(System.getenv("PROCESSOR_IDENTIFIER"));
		return data;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHint() {
		// TODO Auto-generated method stub
		return null;
	}

}
