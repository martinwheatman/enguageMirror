package org.enguage.util.tag;

public class Token {

	private final String string;
	private final String space;
	
	public Token( String wp, String chs ) {space=wp; string=chs;}
	
	public String space() {return space;}
	public String string() {return string;}
	
	public boolean isEmpty() {
		return space.equals("") && string.equals("");
	}

	@Override
	public String toString() {return ""+ space + string;}
}
