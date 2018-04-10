package com.xspacesoft.kowax.engine;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.engine.io.Stdio;
import com.xspacesoft.kowax.engine.shell.CommandRunner;

public abstract class PluginBase {
	
	/** Indicates applet name, called by CommandRUnner */
	public abstract String getAppletName();
	/** Indicates applet version */
	public abstract String getAppletVersion();
	/** Indicates applet author */
	public abstract String getAppletAuthor();

	/** Method called to start task */
	public void start(String[] strings, Stdio stdio, CommandRunner commandRunner) {
		try {
			runApplet(strings, stdio, commandRunner);
		} catch (Exception e) {
			if(Core.getLogwolf().isDebug())
				stdio.println(e.toString());
			else
				throw new RuntimeException("Error during code execution");
		}
	}
	
	/** Code to be run */
	protected abstract void runApplet(String[] command, Stdio stdio, CommandRunner commandRunner);
	
	/** Indicates applet description */
	public abstract String getDescription();
	
	/** Indicates applet hint when no command is given (return null to disable) */
	public abstract String getHint();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if(arg0==this)
			return true;
		if(arg0 instanceof PluginBase) {
			boolean a = arg0.getClass().getName().equals(this.getClass().getName());
			boolean b = this.getAppletVersion()!=null
					?this.getAppletVersion().equals(((PluginBase) arg0).getAppletVersion())
					:((PluginBase) arg0).getAppletVersion()==null?true:false;
			boolean c = this.getAppletName().equals(((PluginBase) arg0).getAppletName());
			boolean d = this.getAppletAuthor()!=null
					?this.getAppletAuthor().equals(((PluginBase) arg0).getAppletAuthor())
					:((PluginBase) arg0).getAppletAuthor()==null?true:false;
			return a && b && c && d;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getAppletAuthor().hashCode()
				* getAppletName().hashCode()
				* getAppletVersion().hashCode()
				* getClass().getPackage().hashCode();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getName();
	}
	
	
	
}
