package com.xspacesoft.kowax.kernel.io;

public interface InputReader {

	public String readString();
	
	public boolean hasNextLine();
	
	public Integer readInt();
	
	public Float readFloat();
	
	public Long readLong();
	
	public Double readDouble();
	
	public Character readCharacter();
}
