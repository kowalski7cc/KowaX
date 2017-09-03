package com.xspacesoft.kowax.engine;

import java.io.File;

import com.xspacesoft.kowax.engine.UsersManager.User;
import com.xspacesoft.kowax.plugins.KowaxExplorer.UserExplorer;

public interface ExplorerInterface {

	public File getCurrentPath(User user);
	
	public UserExplorer getCurrentUser(User User);
	
	public void setUserHome(User user, File file);
}
