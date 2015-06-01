package com.xspacesoft.kowax.kernel;

import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	
	public UsersManager() {
		users = new ArrayList<User>();
	}
	
	public UsersManager(ArrayList<User> users) {
		this.users = users;
	}
	
	public void addUser(String username, String password, String comment) throws ExistingUserException {
		addUser(username, password, comment, false);
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
	}
	
	public void removeUser(String username) throws InvalidUserException {
		for (User user : users)
			if (user.username.equals(username)) {
				users.remove(user);
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

	public void loadFromFile(File usersFile) {
		// TODO Auto-generated method stub
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
