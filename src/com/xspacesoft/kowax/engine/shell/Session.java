package com.xspacesoft.kowax.engine.shell;

import java.util.Locale;

import com.xspacesoft.kowax.engine.io.Stdio;

public class Session {

	private Stdio stdio;
	private String username;
	private boolean authenticated;
	private boolean sudo;
	private Locale locale;
	private boolean sessionActive;
	private Long lastSudo;
	
	public Session(Stdio stdio) {
		this.stdio = stdio;
		authenticated = false;
		sudo = false;
		sessionActive = true;
		locale = Locale.ENGLISH;
		lastSudo = null;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
//		if(sudo)
//			return "root";
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
		if(sudo)
			lastSudo = System.currentTimeMillis();
	}
	
	public boolean isSudoExpired() {
		if(lastSudo==null)
			return true;
		if(System.currentTimeMillis()-lastSudo>30000)
			return true;
		return false;
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
	public Stdio getStdio() {
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
