package com.xspacesoft.kowax.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.xspacesoft.kowax.Initrfs;
import com.xspacesoft.kowax.SystemFolder;
import com.xspacesoft.kowax.apis.KWindow;
import com.xspacesoft.kowax.apis.KernelAccess;
import com.xspacesoft.kowax.kernel.ExplorerInterface;
import com.xspacesoft.kowax.kernel.PluginBase;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.kernel.TokenKey;
import com.xspacesoft.kowax.kernel.UsersManager.User;
import com.xspacesoft.kowax.shell.CommandRunner;
import com.xspacesoft.kowax.windowsystem.Window;

public class KowaxExplorer extends PluginBase implements KernelAccess, ExplorerInterface, KWindow {

	public KowaxExplorer() {
		explorers = new ArrayList<UserExplorer>();
	}
	
	private TokenKey tokenKey;
	
	public class UserExplorer {
		File userHome;
		File currentPath;
		String currentUser;
	}

	protected List<UserExplorer> explorers;
	
	@Override
	public String getAppletName() {
		return "Explorer";
	}

	@Override
	public String getAppletVersion() {
		return "1.0a";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski";
	}

	@Override
	protected void runApplet(String command, Stdio stdio,
			CommandRunner commandRunner) {
		String user = commandRunner.getUsername();
		UserExplorer userExpl = getCorretUser(user);
		if(userExpl.currentPath==null) {
			userExpl.currentPath = userExpl.userHome;
		}
		if (command==null) {
			
		}
		String commands[] = command.split(" ");
		switch(commands[0]) {
		case "info":
			break;
		case "ls":
			int nfiles=0, dirs=0, tokens=0, maxLength=9, space=7;
			File[] files = userExpl.currentPath.listFiles();
			for(File cFile : files) {
				if(maxLength<cFile.getName().length())
					maxLength=cFile.getName().length();
			}
			tokens=floatToInt(maxLength/space)+1;
			if(maxLength%space==0)
				tokens++;
			stdio.println("Current directory: " + userExpl.currentPath.getName());
			stdio.print("File name");
			for(int i=0;i<tokens-floatToInt((float)("File name".length())/(float)space)+1;i++)
				stdio.print("\t");
			stdio.println("Size\tOther informations");
			stdio.println();
			if(userExpl.currentPath.isDirectory()) {
				stdio.print(".");
				for(int i=0;i<tokens-(floatToInt((float)".".length()/(float)space))+1;i++)
					stdio.print("\t");
				stdio.println("-" + "\t" + "DIRECTORY:Current directory");
			}
			if(userExpl.currentPath.getParentFile().isDirectory()) {
				stdio.print("..");
				for(int i=0;i<tokens-(floatToInt((float)"..".length()/(float)space))+1;i++)
					stdio.print("\t");
				stdio.println("-" + "\t" + "DIRECTORY:Upper directory");
				dirs++;
			}
			for(File cFile : files) {
				stdio.print(cFile.getName());
//				stdio.println(" " + floatToInt(cFile.getName().length()/space) + " tokens");
				for(int i=0;i<tokens-(floatToInt((float)cFile.getName().length()/(float)space))+1;i++)
					stdio.print("\t");
				stdio.println((cFile.isFile()?cFile.length():"-") +"\t"+
						(cFile.isDirectory()?"DIRECOTRY":(cFile.isFile()?"FILE\t ":"O"))+":"+
						(cFile.canRead()?"R":"-")+(cFile.canWrite()?"W":"-")+(cFile.canExecute()?"X":"-"));
				if(cFile.isDirectory())
					dirs++;
				else if(cFile.isFile())
					nfiles++;
			}
			stdio.println();
			stdio.println("There are " + nfiles + ((nfiles==1)?" file":" files") + " and " + dirs + ((dirs==1)?" directory":" direcotories"));
			break;
		case "mk":
			if(commands.length==1) {
				stdio.println("Usage: [Exporer] mk (file|dir) <name>");
			}
			switch(commands[1]) {
			case "file":
				if(commands.length==2) {
					stdio.println("Usage: [Exporer] mk (file|dir) <name>");
					return;
				}
				String filename = "";
				for(int i=2;i<commands.length;i++) {
					filename+=commands[i];
				}
				if(new File(userExpl.currentPath, filename).exists()) {
					stdio.println("File already exists");
				} else {
					try {
						new File(userExpl.currentPath, filename).createNewFile();
						stdio.println("Ok");
					} catch (IOException e) {
						stdio.println("Error creating file: " + e);
					}
				}
				break;
			case "dir":
				if(commands.length==2) {
					stdio.println("Usage: [Exporer] mk (file|dir) <name>");
					return;
				}
				String dirname = "";
				for(int i=2;i<commands.length;i++) {
					dirname+=commands[i];
				}
				if(new File(userExpl.currentPath, dirname).exists()) {
					stdio.println("File already exists");
				} else {
					new File(userExpl.currentPath, dirname).mkdirs();
					stdio.println("Ok");
				}
				break;
			default:
				stdio.println("Usage: [Exporer] mk (file|dir) <name>");
				break;
			}
			break;
		case "rm":
			if(commands.length==2) {
				stdio.println("Usage: [Exporer] rm <name>");
				return;
			}
			String fname = "";
			for(int i=2;i<commands.length;i++) {
				fname+=commands[i];
			}
			if(new File(userExpl.currentPath, fname).exists()) {
				new File(userExpl.currentPath, fname).delete();
				stdio.println("Ok");
			} else {
				stdio.println("Can't find file " + fname);
			}
			break;
		case "cd":
			if(commands.length==2) {
				stdio.println("Usage: [Exporer] cd <directory>");
				return;
			}
			break;
		default:
			stdio.println(getHint());
			break;
		}
	}

	@Override
	public String getDescription() {
		return "KowaX File Explorer";
	}

	@Override
	public String getHint() {
		return "Usage: [Explorer] (ls|mk|rm|cd)";
	}

	@Override
	public File getCurrentPath(User user) {
		return getCorretUser(user.getUsername()).currentPath;
	}

	@Override
	public UserExplorer getCurrentUser(User user) {
		return getCorretUser(user.getUsername());
	}

	@Override
	public void setUserHome(User user, File file) {
		UserExplorer userExp = getCorretUser(user.getUsername());
		userExp.userHome = file;
		
	}

	@Override
	public void setTokenKey(TokenKey tokenKey) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreateWindow(Window window) {
		window.setTitle("KowaX File Explorer");
		window.getContent().append("Work in progress...");
	}

	@Override
	public void onDestroyWindow(Window window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWindowHidden(Window window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWindowResume(Window window) {
		// TODO Auto-generated method stub
		
	}

	private UserExplorer getCorretUser(String username) {
		for(UserExplorer userExp : explorers) {
			if(userExp.currentUser.equals(username)) {
				return userExp;
			}
		}
		UserExplorer newExplorer = new UserExplorer();
		newExplorer.currentUser = username;
		if(username.equals("root")) {
			newExplorer.userHome = Initrfs.getSystemFolder(SystemFolder.ROOT, null, tokenKey);
		} else {
			newExplorer.userHome = Initrfs.getSystemFolder(SystemFolder.USER_HOME, username, tokenKey);
		}
		newExplorer.currentPath = newExplorer.userHome;
		return newExplorer;
	}

	public int floatToInt(float f) {
		int i = (int) f;
		if(i<f)
			i++;
		return i;
	}
}
