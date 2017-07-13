package com.xspacesoft.kowax.kernel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.SystemFolder;

public final class UsersManager implements Serializable {

	private static final long serialVersionUID = 4900265260339132855L;
	private static final Object defaultUsers[][] = new Object[][] {
		{"admin", "password", null, false},
		{"kowalski", "4b4adc30da0eadda11b1a1212e49dc96", null, true}
	};

	public class ExistingUserException extends Exception {

		public ExistingUserException(String username) {
			super(username);
		}

		private static final long serialVersionUID = -6436734724155448151L;
		
	}
	
	public class InvalidUserException extends Exception {

		private static final long serialVersionUID = -2766701834016750572L;

		public InvalidUserException(String username) {
			super(username);
		}

	}

	public class User implements Serializable {

		private static final long serialVersionUID = 9120226704038657463L;
		private String username;
		private String password;
		private Date creation;
		private String comment;
		
		public User(String username, String password, String comment) {
			this.username = username;
			this.password = password;
			this.creation = new Date();
			this.comment = comment;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getUsername() {
			return username;
		}

		public Date getCreation() {
			return creation;
		}
		
	}
	
	private List<User> users;
	private File usersFile;
	
	public UsersManager() {
		users = new ArrayList<User>();
		if(!(usersFile = new File(Core.getSystemFolder(SystemFolder.ETC, null, null), "UserManager")).exists())
			usersFile.mkdirs();
		if(!(usersFile = new File(usersFile, "users.dat")).exists())
			try {
				usersFile.createNewFile();
			} catch (IOException e) {
				Core.getLogwolf().e("[UsersManager] - Can't create users file.");
			}
	}
	
	public UsersManager(ArrayList<User> users) {
		if(users!=null)
			this.users = users;
		else
			users = new ArrayList<User>();
		if(!(usersFile = new File(Core.getSystemFolder(SystemFolder.ETC, null, null), "UserManager")).exists())
			usersFile.mkdirs();
		usersFile = new File(usersFile, "users.dat");
	}
	
	public void addUser(String username, String password, String comment) throws ExistingUserException {
		addUser(username, password, comment, false);
		saveToFile();
	}
	
	public void addUser(String username, String password, String comment, boolean hashedPassword)
			throws ExistingUserException {
		User user;
		if(hashedPassword)
			user = new User(username, password, comment);
		else
			user = new User(username, md5(password), comment);
		if(existsUser(username)) {
			throw new ExistingUserException(username);
		}
		users.add(user);
		saveToFile();
	}
	
	public void removeUser(String username) throws InvalidUserException {
		for (User user : users)
			if (user.username.equals(username)) {
				users.remove(user);
				saveToFile();
				return;
			}
		throw new InvalidUserException(username);
	}
	
	public boolean existsUser(String username) {
		for (User user : users)
			if (user.username.equals(username))
				return true;
		return false;
	}
	
	public boolean isPasswordValid(String username, String password) throws InvalidUserException {
		for (User user : users)
			if (user.username.equals(username)) {
				if(user.getPassword().equals(md5(password)))
					return true;
				return false;
			}
		throw new InvalidUserException(username);
	}
	
	public String md5(String original) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(original.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) { }
		return null;	
	}

	public void loadDefaults() throws ExistingUserException {
		for(int i=0; i<defaultUsers.length; i++) {
			addUser((String)defaultUsers[i][0], (String)defaultUsers[i][1], (String)defaultUsers[i][2], (Boolean) defaultUsers[i][3]);
		}
	}

	public static List<String> getDefaultUsers() {
		List<String> list = new ArrayList<String>();
		for(Object[] user : defaultUsers) {
			list.add((String)user[0]);
		}
		return list;
	}
	
	public int getLoadedUsers() {
		return users.size();
	}

	public void loadFromFile() {
		try (BufferedReader br = new BufferedReader(
				new FileReader(usersFile))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				String[] cUserData = sCurrentLine.split(";");
				if(cUserData.length==3) {
					try {
						addUser(cUserData[0], cUserData[1], cUserData[2], true);
					} catch (ExistingUserException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			Core.getLogwolf().e("Users file read error: " + e);
		}
	}

	public void saveToFile() {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(usersFile), "utf-8"))) {
			writer.write("");
			for(User cUser : users) {
				writer.append(cUser.username + ";" + cUser.password + ";" + cUser.comment);
				writer.newLine();
			}
		} catch (IOException ex) {
			Core.getLogwolf().e("Can't save users");
		} 
	}

	public String[] getUsersName() {
		String[] allusers = new String[users.size()];
		int i=0;
		for (User user : users) {
			allusers[i++] = user.getUsername();
		}
		return allusers;
	}
}
