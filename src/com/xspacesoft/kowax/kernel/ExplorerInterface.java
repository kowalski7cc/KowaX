package com.xspacesoft.kowax.kernel;

import java.io.File;

import com.xspacesoft.kowax.kernel.UsersManager.User;
import com.xspacesoft.kowax.plugins.KowaxExplorer.UserExplorer;

public interface ExplorerInterface {

	public File getCurrentPath(User user);
	
	public UserExplorer getCurrentUser(User User);
	
	public void setUserHome(User user, File file);
}
