package com.xspacesoft.kowax.kernel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.xspacesoft.kowax.Core;
import com.xspacesoft.kowax.SystemFolder;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public final class UsersManager implements Serializable{

	private static final long serialVersionUID = 4900265260339132855L;

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

	public class User implements Serializable, Comparable<User> {

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

		@Override
		public boolean equals(Object obj) {
			if(this==obj)
				return true;
			if(obj instanceof User)
				if(((User)obj).getUsername().equals(this.username))
					return true;
			return false;
		}

		@Override
		public int compareTo(User o) {
			if(this.username.equals(o))
				return 0;
			return 1;
		}

	}

	private List<User> users;
	private File usersFile;

	public UsersManager() {
		users = new ArrayList<User>();
		if(!(usersFile = new File(Core.getSystemFolder(SystemFolder.CONFIGURATIONS, null, null), "UserManager")).exists())
			usersFile.mkdirs();
		if(!(usersFile = new File(usersFile, "users.dat")).exists())
			try {
				usersFile.createNewFile();
				saveToFile();
			} catch (IOException e) {
				Core.getLogwolf().e("[UsersManager] - Can't create users file.");
				throw new RuntimeException("[UsersManager] - Can't create users file.");
			}
	}

	//	public UsersManager(ArrayList<User> users) {
	//		if(users!=null)
	//			this.users = users;
	//		else
	//			users = new ArrayList<User>();
	//		if(!(usersFile = new File(Core.getSystemFolder(SystemFolder.ETC, null, null), "UserManager")).exists())
	//			usersFile.mkdirs();
	//		usersFile = new File(usersFile, "users.dat");
	//	}


	public void addUser(String username, String password, String comment)
			throws ExistingUserException {
		User user;
		user = new User(username, password, comment);
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

	public static String md5(String original) {
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

	public int getLoadedUsers() {
		return users.size();
	}

	public boolean loadFromFile()  {
		StringBuilder input = new StringBuilder();
		try (Scanner scanner = new Scanner(usersFile)){
			scanner.forEachRemaining(s -> input.append(s));
			scanner.close();
		} catch (FileNotFoundException e1) {
			Core.getLogwolf().e("Can't laod file: " + e1.getMessage());
			return false;
		}
		
		JSONArray usersArray;
		try {
			usersArray = new JSONArray(input.toString());
			for(int i = 0; i < usersArray.length(); i++) {
				JSONObject user = usersArray.getJSONObject(i);
				if(user.has("username")&&user.has("password")) {
					users.add(new User(user.getString("username"),
							user.getString("password").toLowerCase(),
							user.has("comment")?user.getString("comment"):""));
				}
			}
		} catch (JSONException e) {
			Core.getLogwolf().e("[UsersManager] - Can't load users: " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean saveToFile() {
		JSONArray usersArray = new JSONArray();
		boolean success = true;
		for (User user : users) {
			JSONObject thisUser = new JSONObject();

			try {
				thisUser.put("username", user.username);
				thisUser.put("password", user.password);
				thisUser.put("comment", user.comment);
			} catch (JSONException e) {
				Core.getLogwolf().e("[UsersManager] - Can't save user " + user.username);
				success = false;
			}
			usersArray.put(thisUser);
		}
		try (PrintWriter printWriter = new PrintWriter(usersFile);) {
			printWriter.println(usersArray.toString());
		} catch (FileNotFoundException e) {
			Core.getLogwolf().e("[UsersManager] - Error on save: " + e.getMessage());
			return false;
		}
		
		return success;
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
