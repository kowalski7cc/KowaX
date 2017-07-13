package com.xspacesoft.kowax.kernel;

import com.xspacesoft.kowax.apis.RegistrableComponent;
import com.xspacesoft.kowax.kernel.io.Stdio;

public class ApiManager {
	
	private PluginBase displayServer;
	private PluginBase keyring;
	private PluginBase cron;
	
	public ApiManager() {
		registerComponent(Component.Keyring, new ApiManager());
	}
	
	public enum Component {
		DisplayServer,
		Keyring,
		Cron,
	}
	
	public class ComponentAlreadyRegistredException extends IllegalStateException {
		private static final long serialVersionUID = 8011103576443923009L;
	}
	
	public void registerComponent(Component c, Object p) {
		registerComponent(c, p, false);
	}
	
	private void registerComponent(Component c, Object p, boolean force) {
		if(!((p instanceof PluginBase)&&(p instanceof RegistrableComponent)))
			throw new IllegalArgumentException(
					(p instanceof PluginBase) ? "Object is not instanceof PluginBase()" : 
						"p is not istanceof RegistrableComponent()");
		switch (c) {
		case Cron:
			if((cron==null)||(force))
				cron = (PluginBase) p;
			else
				throw new ComponentAlreadyRegistredException();
			break;
		case DisplayServer:
			if((displayServer==null)||(force))
				displayServer = (PluginBase) p;
			else
				throw new ComponentAlreadyRegistredException();
			break;
		case Keyring:
			if((keyring==null)||(force))
				keyring = (PluginBase) p;
			else
				throw new ComponentAlreadyRegistredException();
			break;
		default:
			break;
		}
	}

	public void askComponentRegistration(Component c, Object p, Stdio s) {
		if(s == null)
			throw new IllegalArgumentException("Stdio must not be null");
		registerComponent(c, p, false);
	}
	
	public Object getComponent(Component c) {
		switch(c) {
		case Cron: return cron;
		case DisplayServer: return displayServer;
		case Keyring: return keyring;
		default: return null;
		}
	}
	
}
