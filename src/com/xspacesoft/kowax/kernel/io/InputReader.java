package com.xspacesoft.kowax.kernel.io;

public interface InputReader {

	public String next();
	
	public String nextLine();
	
	public boolean hasNextLine();
	
	public Integer nextInt();
	
	public Float nextFloat();
	
	public Long nextLong();
	
	public Double nextDouble();
	
	public Character nextCharacter();
}
