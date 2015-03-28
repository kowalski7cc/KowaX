package com.xspacesoft.kowax.shell;

import java.util.Locale;

import com.xspacesoft.kowax.kernel.Stdio;

public class Session {

	private Stdio stdio;
	private String username;
	private boolean authenticated;
	private boolean sudo;
	private Locale locale;
	private boolean sessionActive;
	
	public Session(Stdio stdio) {
		this.stdio = stdio;
		authenticated = false;
		sudo = false;
		sessionActive = true;
		locale = Locale.ENGLISH;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		if(sudo)
			return "root";
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the authenticated
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * @param authenticated the authenticated to set
	 */
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	/**
	 * @return the sudo
	 */
	public boolean isSudo() {
		return sudo;
	}

	/**
	 * @param sudo the sudo to set
	 */
	public void setSudo(boolean sudo) {
		this.sudo = sudo;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the sockethelper
	 */
	public Stdio getSockethelper() {
		return stdio;
	}

	/**
	 * @return the sessionActive
	 */
	public boolean isSessionActive() {
		return sessionActive;
	}

	/**
	 * @param sessionActive the sessionActive to set
	 */
	public void setSessionActive(boolean sessionActive) {
		this.sessionActive = sessionActive;
	}
	
}
