package com.xspacesoft.kowax.kernel;

import java.util.Random;

public class TokenKey {
	
	public static class InvalidTokenException extends IllegalArgumentException {

		private static final long serialVersionUID = -9025894429847657351L;

		public InvalidTokenException() {
			super();
		}

		public InvalidTokenException(String s) {
			super(s);
		}		
		
	}
	
	private Integer token;
	
	public TokenKey() {
		token = null;
	}
	
	public void newKey() {
		Random random = new Random();
		this.token = Math.abs(random.nextInt());
	}
	
	public Integer getKey() {
		return token;
	}

	public boolean equals(TokenKey tokenKey) {
		if(this.token.intValue()==tokenKey.token.intValue())
			return true;
		return false;
	}
}