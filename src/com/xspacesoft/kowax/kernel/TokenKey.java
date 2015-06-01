package com.xspacesoft.kowax.kernel;

import java.util.Random;

public final class TokenKey {
	
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
	
	private TokenKey(Integer token) {
		this.token = token;
	}
	
	public static TokenKey newKey() {
		Random random = new Random();
		Integer token;
		while((token = Math.abs(random.nextInt()))<1000000000);
		return new TokenKey(token);
	}
	
	public void changeKey() {
		Random random = new Random();
		while((this.token = Math.abs(random.nextInt()))<1000000000);
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